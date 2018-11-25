package co.dporn.gmd.client.presenters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.app.Routes;
import co.dporn.gmd.client.views.BlogCardUi;
import co.dporn.gmd.client.views.VideoCardUi;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.PostListResponse;
import gwt.material.design.addins.client.scrollfire.MaterialScrollfire;

public class MainContentPresenter implements ContentPresenter, ScheduledCommand {

	private static final int FEATURED_VIDEO_COUNT = 4;
	private ContentView view;
	private AppControllerModel model;

	public MainContentPresenter(AppControllerModel model, ContentView view) {
		this.view = view;
		this.model = model;
	}

	@Override
	public void setView(ContentView view) {
		this.view = view;
	}

	/*
	 * For Featured Videos, take several recent mongoid desc, use scaled vote count
	 * based on list index, use as sort value desc, select best subset.
	 */

	protected void loadFeaturedVideos() {
		model.featuredPosts(FEATURED_VIDEO_COUNT).thenAccept(posts -> {
			GWT.log("HAVE "+posts.getPosts().size()+" FEATURED VIDEOS");
			getContentView().getFeaturedPosts().clear();
			int[] showDelay = { 0 };
			Map<String, AccountInfo> infoMap = posts.getInfoMap();
			posts.getPosts().forEach(p -> {
				String videoPath = p.getVideoPath();
				if (videoPath == null || !videoPath.startsWith("/ipfs/")) {
					return;
				}
				showDelay[0] += 500;
				AccountInfo i = infoMap.get(p.getUsername());
				if (i == null) {
					GWT.log("NO AUTHOR FOR FEATURED POST!");
					return;
				}
				VideoCardUi card = new VideoCardUi();
				card.setDisplayName(p.getUsername());
				card.setShowDelay(showDelay[0]);
				String displayName = i.getDisplayName() == null ? p.getUsername() : i.getDisplayName();
				card.setDisplayName(displayName);
				card.setAvatarUrl(Routes.avatarImage(p.getUsername()));
				card.setTitle(p.getTitle());
				card.setVideoEmbedUrl(Routes.embedVideo(p.getUsername(), p.getPermlink()));
				card.setViewLink(Routes.post(p.getUsername(), p.getPermlink()));
				getContentView().getFeaturedPosts().add(card);
			});
		}).exceptionally(ex->{
			GWT.log(ex.getMessage(), ex);
			return null;
		});
	}

	private void activateScrollfire(IsWidget widget) {
		GWT.log("activateScrollfire");
		MaterialScrollfire scrollfire = new MaterialScrollfire();
		scrollfire.setCallback(() -> {
			GWT.log("activateScrollfire#callback");
			loadRecentVideos();
		});
		scrollfire.setOffset(0);
		scrollfire.setElement(widget.asWidget().getElement());
		scrollfire.apply();
	}

	private String lastRecentId = null;
	private int _recentVideosCounter = 0;
	private synchronized int incRecentVideoCount() {
		return ++_recentVideosCounter;
	}
	private synchronized int getRecentVideoCount() {
		return _recentVideosCounter;
	}
	private void loadRecentVideos() {
		loadRecentVideos(4);
	}
	private void loadRecentVideos(int count) {
		showLoading(true);
		Timer[] timer = { null };
		CompletableFuture<PostListResponse> listPosts;
		if (lastRecentId == null) {
			listPosts = model.listPosts(count);
			getContentView().getRecentPosts().clear();
		} else {
			listPosts = model.listPosts(lastRecentId, count+1);
		}
		listPosts.thenAccept((l) -> {
			GWT.log("Recent posts: " + l.getPosts().size());
			if (l.getPosts().size()<2 && lastRecentId!=null) {
				showLoading(false);
				return;
			}
			Iterator<BlogEntry> ilist = l.getPosts().iterator();
			String newLastRecentId = lastRecentId;
			while (ilist.hasNext()) {
				BlogEntry next = ilist.next();
				if (next.getId().getOid().equals(lastRecentId)) {
					ilist.remove();
					continue;
				}
				newLastRecentId = next.getId().getOid();
				if (BlogEntryType.VIDEO != next.getEntryType()) {
					GWT.log("SKIPPING RECENT POST: "+next.getUsername()+" | "+next.getPermlink());
					ilist.remove();
					continue;
				}
			}
			int[] showDelay = { 0 };
			Map<String, AccountInfo> infoMap = l.getInfoMap();
			l.getPosts().forEach(p -> {
				if (p.getId().getOid().equals(lastRecentId)) {
					return;
				}
				String videoPath = p.getVideoPath();
				if (videoPath == null || !videoPath.startsWith("/ipfs/")) {
					return;
				}
				incRecentVideoCount();
				deferred(() -> {
					AccountInfo i = infoMap.get(p.getUsername());
					if (i == null) {
						return;
					}
					VideoCardUi card = new VideoCardUi();
					card.setDisplayName(p.getUsername());
					card.setShowDelay(showDelay[0]);
					showDelay[0] += 150; // 75
					String displayName = i.getDisplayName() == null ? p.getUsername() : i.getDisplayName();
					card.setDisplayName(displayName);
					card.setAvatarUrl(Routes.avatarImage(p.getUsername()));
					card.setTitle(p.getTitle());
					card.setViewLink(Routes.post(p.getUsername(), p.getPermlink()));
					card.setVideoEmbedUrl(Routes.embedVideo(p.getUsername(), p.getPermlink()));
					getContentView().getRecentPosts().add(card);
					if (timer[0] != null) {
						timer[0].cancel();
					}
					timer[0] = new Timer() {
						@Override
						public void run() {
							if (Document.get().getBody().getScrollHeight()<=Window.getClientHeight()) {
								loadRecentVideos();
							} else {
								activateScrollfire(card);
								showLoading(false);
							}
						}
					};
					timer[0].schedule(500);
				});
			});
			lastRecentId=newLastRecentId;
			GWT.log("Have "+getRecentVideoCount()+" recent videos.");
			if (getRecentVideoCount()%4 != 0) {
				GWT.log("Loading extra recent videos: "+(4-getRecentVideoCount()%4));
				loadRecentVideos(4-getRecentVideoCount()%4);
			}
		});
	}

	private void showLoading(boolean loading) {
		getContentView().showLoading(loading);
	}

	private void loadFeaturedBlogs() {
		model.listFeatured().thenAccept((f) -> {
			GWT.log("Featured channels.");
			getContentView().getFeaturedChannels().clear();
			List<BlogCardUi> cards = new ArrayList<>();
			int[] showDelay = { 0 };
			f.getAuthors().forEach((username) -> {
				BlogCardUi card = new BlogCardUi();
				card.setDisplayName(username);
				card.setShowDelay(showDelay[0]);
				showDelay[0] += 75;
				AccountInfo i = f.getInfoMap().get(username);
				if (i == null) {
					return;
				}
				String displayName = i.getDisplayName();
				if (displayName != null && !displayName.trim().isEmpty()) {
					card.setDisplayName(displayName);
				}
				card.setAvatarUrl(Routes.avatarImage(username));
				card.setTitle(i.getAbout());
				String coverImage = i.getCoverImage();
				if (coverImage == null) {
					return;
				} else {
					if (!coverImage.startsWith("https://steemitimages.com/")) {
						coverImage = "https://steemitimages.com/500x500/" + coverImage;
					}
				}
				card.setImageUrl(coverImage);
				card.setViewLink(Routes.channel(username));
				cards.add(card);
			});
			cards.subList(0, Math.min(4, cards.size())).forEach((w) -> {
				deferred(() -> getContentView().getFeaturedChannels().add(w));
			});
		});
	}

	@Override
	public ContentView getContentView() {
		return view;
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model = model;
	}
	
	@Override
	public void execute() {
		GWT.log(this.getClass().getSimpleName() + "#execute");
		loadRecentVideos();
		loadFeaturedBlogs();
		loadFeaturedVideos();
	}

	private int posX=0;
	private int posY=0;
	@Override
	public void saveScrollPosition() {
		posX=Window.getScrollLeft();
		posY=Window.getScrollTop();
	}

	@Override
	public void restoreScrollPosition() {
		Window.scrollTo(posX, posY);
	}}
