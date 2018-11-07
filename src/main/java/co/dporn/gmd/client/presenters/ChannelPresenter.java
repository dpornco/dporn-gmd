package co.dporn.gmd.client.presenters;

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
import co.dporn.gmd.client.views.VideoCardUi;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.PostListResponse;
import gwt.material.design.addins.client.scrollfire.MaterialScrollfire;

public class ChannelPresenter implements ContentPresenter, ScheduledCommand {

	private ContentView view;
	private AppControllerModel model;
	private String username;

	//TODO: Load and display full account info in banner header like dtube/busy/steem does.
	public ChannelPresenter(String username, AppControllerModel model, ContentView view) {
		this.view = view;
		this.model = model;
		this.username=username;
	}

	@Override
	public void setView(ContentView view) {
		this.view = view;
	}

	private void activateScrollfire(IsWidget widget) {
		GWT.log("activateScrollfire");
		MaterialScrollfire scrollfire = new MaterialScrollfire();
		scrollfire.setCallback(() -> {
			GWT.log("activateScrollfire#callback");
			loadPostsFor();
		});
		scrollfire.setOffset(0);
		scrollfire.setElement(widget.asWidget().getElement());
		scrollfire.apply();
	}

	private String lastRecentId = null;

	private void loadPostsFor() {
		showLoading(true);
		Timer[] timer = { null };
		CompletableFuture<PostListResponse> listPosts;
		if (lastRecentId == null) {
			listPosts = model.postsFor(username);
			getContentView().getRecentPosts().clear();
		} else {
			listPosts = model.postsFor(username, lastRecentId, 9);
		}
		listPosts.thenAccept((l) -> {
			GWT.log("Channel Recent posts: " + l.getPosts().size());
			if (l.getPosts().size()<2 && lastRecentId!=null) {
				showLoading(false);
				return;
			}
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
					card.setDisplayName(p.getAuthor());
					card.setShowDelay(showDelay[0]);
					showDelay[0] += 150; // 75
					String displayName = i.getDisplayName() == null ? p.getAuthor() : i.getDisplayName();
					card.setDisplayName(displayName);
					card.setAvatarUrl(Routes.avatarImage(p.getAuthor()));
					card.setTitle(p.getTitle());
					String videoIpfs = p.getVideoIpfs();
					if (videoIpfs == null || videoIpfs.trim().isEmpty() || videoIpfs.length() != 46) {
						return;
					}
					card.setViewLink(Routes.post(p.getAuthor(), p.getPermlink()));
					card.setVideoEmbedUrl(Routes.embedVideo(p.getAuthor(), p.getPermlink()));
					getContentView().getRecentPosts().add(card);
					if (timer[0] != null) {
						timer[0].cancel();
					}
					timer[0] = new Timer() {
						@Override
						public void run() {
							if (Document.get().getBody().getScrollHeight()<=Window.getClientHeight()) {
								loadPostsFor();
							} else {
								activateScrollfire(card);
								showLoading(false);
							}
						}
					};
					timer[0].schedule(500);
				});
			});
		});
	}

	private void showLoading(boolean loading) {
		getContentView().showLoading(loading);
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
		loadPostsFor();
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
