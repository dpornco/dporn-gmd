package co.dporn.gmd.client.presenters;

import java.util.Arrays;
import java.util.HashMap;
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
import co.dporn.gmd.client.utils.SteemDataUtil;
import co.dporn.gmd.client.views.BlogCardUi;
import co.dporn.gmd.client.views.VideoCardUi;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntryListResponse;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLImageElement;
import gwt.material.design.addins.client.scrollfire.MaterialScrollfire;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;
import jsinterop.base.Js;
import steem.model.CommentMetadata;

public class ChannelPresenter implements ContentPresenter, ScheduledCommand {

	private ContentView view;
	private AppControllerModel model;
	private String username;

	// TODO: Load and display full account info in banner header like
	// dtube/busy/steem does.
	public ChannelPresenter(String username, AppControllerModel model, ContentView view) {
		this.view = view;
		this.model = model;
		this.username = username;
	}

	@Override
	public void setView(ContentView view) {
		this.view = view;
	}

	private void activateScrollfire(IsWidget widget) {
		MaterialScrollfire scrollfire = new MaterialScrollfire();
		scrollfire.setCallback(() -> {
			loadBlogEntriesFor();
		});
		scrollfire.setOffset(0);
		scrollfire.setElement(widget.asWidget().getElement());
		scrollfire.apply();
	}

	private String lastRecentId = null;
	/**
	 * See same variable in {@link MainContentPresenter}
	 */
	private String pendingRecentId = null;

	private void loadBlogEntriesFor() {
		showLoading(true);
		Timer[] timer = { null };
		CompletableFuture<BlogEntryListResponse> listBlogEntries;
		if (lastRecentId == null) {
			listBlogEntries = model.listBlogEntriesFor(username);
			getContentView().getRecentPosts().clear();
		} else {
			if (pendingRecentId==lastRecentId || (pendingRecentId!=null && pendingRecentId.equals(lastRecentId))){
				return;
			} else {
				listBlogEntries = model.listBlogEntriesFor(username, lastRecentId, 9);
				pendingRecentId = lastRecentId;
			}
		}
		listBlogEntries.thenAccept((l) -> {
			if (l.getBlogEntries().size() < 2 && lastRecentId != null) {
				showLoading(false);
				return;
			}
			int[] showDelay = { 0 };
			Map<String, AccountInfo> infoMap = l.getInfoMap();
			l.getBlogEntries().forEach(p -> {
				if (p.getId().getOid().equals(lastRecentId)) {
					return;
				}
				lastRecentId = p.getId().getOid();
				deferred(() -> {
					String entryUsername = p.getUsername();
					AccountInfo i = infoMap.get(entryUsername);
					if (i == null) {
						return;
					}
					String entryDisplayName = i.getDisplayName();
					BlogCardView card;
					if (p.getVideoPath()!=null) {
						card = new VideoCardUi();
						((VideoCardUi) card).setVideoEmbedUrl(Routes.embedVideo(entryUsername, p.getPermlink()));
						//((VideoCardUi) card).setVideoEmbedUrl(p.getVideoPath());
					} else {
						boolean hasCardImage=false;
						card = new BlogCardUi();
						if (p.getGalleryImageThumbPaths()!=null && !p.getGalleryImageThumbPaths().isEmpty()) {
							card.setImageUrl(p.getGalleryImageThumbPaths().get(0));
							hasCardImage=true;
						}
						if (p.getPosterImagePath()!=null && !p.getPosterImagePath().trim().isEmpty()) {
							card.setImageUrl(p.getPosterImagePath());
							hasCardImage=true;
						}
						CommentMetadata metadata = CommentMetadata.fromJson(p.getCommentJsonMetadata());
						metadataImage: if (metadata!=null && metadata.getImage()!=null) {
							for (String imgUrl: metadata.getImage()) {
								String lcImgUrl = imgUrl.toLowerCase();
								if (lcImgUrl.endsWith("jpg")||lcImgUrl.endsWith("jpeg")||lcImgUrl.endsWith("png")||lcImgUrl.endsWith("gif")) {
									card.setImageUrl(imgUrl);
									hasCardImage=true;
									break metadataImage;
								}
							}
						}
						extractImage: if (!hasCardImage) {
							// extract image links via JQuery operation on HTML fragment
							if (p.getContent().toLowerCase().contains("<img")) {
								HTMLDivElement div = Js.cast(DomGlobal.document.createElement("div"));
								div.innerHTML=p.getContent();
								JQueryElement imgs = JQuery.$(div).find("img");
								if (imgs != null) {
									// use non-async code!
									for (int ix = 0; ix < imgs.length(); ix++) {
										HTMLImageElement img = Js.cast(imgs.get(ix));
										if (img.src!=null && !img.src.trim().isEmpty()) {
											GWT.log("img src="+img.src);
											card.setImageUrl(img.src);
											hasCardImage=true;
											break extractImage;
										}
									}
								}
							}
						}
					}
					//card.setChannelLink(Routes.channel(entryUsername));
					card.setDisplayName(entryUsername);
					card.setShowDelay(showDelay[0]);
					showDelay[0] += 150; // 75
					String displayName = entryDisplayName == null ? entryUsername : entryDisplayName;
					card.setDisplayName(displayName);
					card.setAvatarUrl(Routes.avatarImage(entryUsername));
					card.setTitle(p.getTitle());
//					if (videoPath == null || !videoPath.startsWith("/ipfs/")) {
//						return;
//					}
					card.setViewLink(Routes.blogEntry(entryUsername, p.getPermlink()));
					getContentView().getRecentPosts().add(card.asWidget());
					SteemDataUtil.enableAndUpdateCardVoting(model, p.getUsername(), p.getPermlink(), card);
					if (timer[0] != null) {
						timer[0].cancel();
					}
					timer[0] = new Timer() {
						@Override
						public void run() {
							if (Document.get().getBody().getScrollHeight() <= Window.getClientHeight()) {
								loadBlogEntriesFor();
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
		loadBlogEntriesFor();
		model.getBlogInfo(username).thenAccept(this::setupHeader).exceptionally(ex->{
			ActiveBlogsResponse response = new ActiveBlogsResponse();
			response.setAuthors(Arrays.asList("User Not Found"));
			Map<String, AccountInfo> infoMap=new HashMap<>();
			AccountInfo accountInfo=new AccountInfo();
			accountInfo.setAbout("User Not Found");
			accountInfo.setCoverImage(null);
			accountInfo.setDisplayName("This user does not exist");
			accountInfo.setProfileImage(null);
			infoMap.put("User Not Found", accountInfo);
			response.setInfoMap(infoMap);
			setupHeader(response);
			return null;
		});
	}

	void setupHeader(ActiveBlogsResponse infoMap) {
		if (!(view instanceof ChannelView)) {
			return;
		}
		ChannelView channelView = (ChannelView) view;
		if (!infoMap.getInfoMap().containsKey(username)) {
			channelView.showUserNotFound(username);
			return;
		}
		AccountInfo info = infoMap.getInfoMap().get(username);
		BlogHeader blogHeader = channelView.getBlogHeader();
		blogHeader.setAvatarUrl(Routes.avatarImage(username));
		blogHeader.setDisplayName(info.getDisplayName());
		String coverImage = info.getCoverImage();
		blogHeader.setImageUrl(coverImage);
		blogHeader.setAbout(info.getAbout());
		blogHeader.setFollowing(false);
		blogHeader.setBusyLink(username);
		blogHeader.setSteemitLink(username);
	}

	private int posX = 0;
	private int posY = 0;

	@Override
	public void saveScrollPosition() {
		posX = Window.getScrollLeft();
		posY = Window.getScrollTop();
	}

	@Override
	public void restoreScrollPosition() {
		Window.scrollTo(posX, posY);
	}
	@Override
	public void scrollToTop() {
		Window.scrollTo(0, 0);
	}
}
