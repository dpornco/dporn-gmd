package co.dporn.gmd.client.presenters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.BlogCardUi;
import co.dporn.gmd.client.views.VideoCardUi;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.PostListResponse;
import gwt.material.design.addins.client.scrollfire.MaterialScrollfire;

public class ContentPresenterImpl implements ContentPresenter, ScheduledCommand {

	private ContentView view;
	private AppControllerModel model;

	public ContentPresenterImpl(AppControllerModel model, ContentView view) {
		this.view = view;
		this.model = model;
	}

	@Override
	public void setView(ContentView view) {
		this.view = view;
	}

	/*
	 * TODO: for Featured Posts, take most recent 14 mongoid desc, divide vote count
	 * by (list position+1), use as sort value desc, select first 4.
	 */

	protected void loadNextRecentPosts() {

	}

	private void activateScrollfire() {
		GWT.log("activateScrollfire");
		MaterialScrollfire scrollfire = new MaterialScrollfire();
		scrollfire.setCallback(()->{
			GWT.log("activateScrollfire#callback");
			activateScrollfire();
			loadRecentPosts();			
		});
		scrollfire.setOffset(0);
		scrollfire.setElement(view.getBottomMarker().asWidget().getElement());
		scrollfire.apply();
	}

	private String lastRecentId = null;

	private void loadRecentPosts() {
		
		CompletableFuture<PostListResponse> listPosts;
		if (lastRecentId == null) {
			listPosts = model.listPosts(4);
			getContentView().getRecentPosts().clear();
		} else {
			listPosts = model.listPosts(lastRecentId, 5);
		}
		listPosts.thenAccept((l) -> {
			GWT.log("Recent posts: " + l.getPosts().size());
			int[] showDelay = { 0 };
			Map<String, AccountInfo> infoMap = l.getInfoMap();
			l.getPosts().forEach(p -> {
				if (p.getId().equals(lastRecentId)) {
					return;
				}
				lastRecentId = p.getId();
				deferred(() -> {
					AccountInfo i = infoMap.get(p.getAuthor());
					if (i == null) {
						return;
					}
					VideoCardUi card = new VideoCardUi();
					card.setAuthorName(p.getAuthor());
					card.setShowDelay(showDelay[0]);
					showDelay[0] += 150; //75
					String displayName = i.getDisplayName()==null?p.getAuthor():i.getDisplayName();
					card.setAuthorName(displayName);
					String profileImage = i.getProfileImage();
					if (profileImage != null && !profileImage.isEmpty()) {
						if (!profileImage.toLowerCase().startsWith("https://steemitimages.com/")) {
							profileImage = "https://steemitimages.com/150x150/" + profileImage;
						}
						card.setAvatarUrl(profileImage);
					}
					card.setTitle(p.getTitle());
					String videoIpfs = p.getVideoIpfs();
					if (videoIpfs == null || videoIpfs.trim().isEmpty() || videoIpfs.length() != 46) {
						return;
					}
					String embedUrl = GWT.getHostPageBaseURL() + "embed/@" + p.getAuthor() + "/" + p.getPermlink();
					card.setVideoEmbedUrl(embedUrl);
					getContentView().getRecentPosts().add(card);
				});
			});
		}).thenRun(this::activateScrollfire);

	}

	private void loadFeaturedBlogs() {
		model.listFeatured().thenAccept((f) -> {
			GWT.log("Featured blogs.");
			getContentView().getFeaturedChannels().clear();
			List<BlogCardUi> cards = new ArrayList<>();
			int[] showDelay = { 0 };
			f.getAuthors().forEach((a) -> {
				BlogCardUi card = new BlogCardUi();
				card.setAuthorName(a);
				card.setShowDelay(showDelay[0]);
				showDelay[0] += 75;
				AccountInfo i = f.getInfoMap().get(a);
				if (i == null) {
					return;
				}
				card.setAuthorName(i.getDisplayName());
				String profileImage = i.getProfileImage();
				if (profileImage == null) {
					return;
					// profileImage=GWT.getHostPageBaseURL()+"images/avatarImagePlaceholder.png";
				} else {
					if (!profileImage.startsWith("https://steemitimages.com/")) {
						profileImage = "https://steemitimages.com/150x150/" + profileImage;
					}
				}
				card.setAvatarUrl(profileImage);
				card.setTitle(i.getAbout());
				String coverImage = i.getCoverImage();
				if (coverImage == null) {
					return;
					// coverImage=GWT.getHostPageBaseURL()+"images/coverImagePlaceholder.png";
				} else {
					if (!coverImage.startsWith("https://steemitimages.com/")) {
						coverImage = "https://steemitimages.com/500x500/" + coverImage;
					}
				}
				card.setImageUrl(coverImage);
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
		loadRecentPosts();
		loadFeaturedBlogs();
	}
}
