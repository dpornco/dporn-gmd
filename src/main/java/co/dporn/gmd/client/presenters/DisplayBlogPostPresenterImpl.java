package co.dporn.gmd.client.presenters;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.app.Routes;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import gwt.material.design.client.ui.MaterialVideo;

public class DisplayBlogPostPresenterImpl implements DisplayBlogPostPresenter {

	private String username;
	private String permlink;
	private AppControllerModel model;
	private DisplayBlogPostView view;

	public DisplayBlogPostPresenterImpl(String username, String permlink, AppControllerModel model,
			DisplayBlogPostView displayBlogPostView) {
		this.username = username;
		this.permlink = permlink;
		this.model = model;
		this.view = displayBlogPostView;
	}

	@Override
	public ContentView getContentView() {
		return view;
	}

	@Override
	public void saveScrollPosition() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreScrollPosition() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModel(AppControllerModel model) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setView(ContentView view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() {
		model.blogInfo(username).thenAccept(this::setupHeader).thenRun(() -> loadAndDisplayPost());
	}

	void setupHeader(ActiveBlogsResponse infoMap) {
		if (!(view instanceof DisplayBlogPostView)) {
			return;
		}
		DisplayBlogPostView channelView = (DisplayBlogPostView) view;
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
	}

	protected void loadAndDisplayPost() {
		model.getDiscussionComment(username, permlink).thenAccept((comment) -> {
//			view.setTitle(comment.getTitle());
			DisplayBlogPostView channelView = (DisplayBlogPostView) view;
			BlogHeader blogHeader = channelView.getBlogHeader();
			blogHeader.setAbout(comment.getTitle());
			blogHeader.setChannelRoute(Routes.channel(username));
		}).thenRun(() -> {
			view.showLoading(false);
			if (!(view instanceof DisplayBlogPostView)) {
				return;
			}
			DisplayBlogPostView channelView = (DisplayBlogPostView) view;
			MaterialVideo video = new MaterialVideo(Routes.embedVideo(username, permlink));
			video.setFullscreen(true);
			channelView.getPostView().add(video);
		});
	}

	@Override
	public void upvote(int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downvote(int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void report(String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reply(String reply) {
		// TODO Auto-generated method stub

	}

}
