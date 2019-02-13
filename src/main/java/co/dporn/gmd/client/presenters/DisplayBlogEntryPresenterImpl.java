package co.dporn.gmd.client.presenters;

import com.google.gwt.user.client.Window;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.app.Routes;
import co.dporn.gmd.client.utils.SteemDataUtil;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntryType;

public class DisplayBlogEntryPresenterImpl implements DisplayBlogEntryPresenter {

	private String author;
	private String permlink;
	private AppControllerModel model;
	private DisplayBlogEntryView view;

	public DisplayBlogEntryPresenterImpl(String author, String permlink, AppControllerModel model,
			DisplayBlogEntryView displayBlogEntryView) {
		this.author = author;
		this.permlink = permlink;
		this.model = model;
		view = displayBlogEntryView;
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
		model.getBlogInfo(author).thenAccept(this::setupHeader).thenRun(() -> loadAndDisplayBlogEntry());
	}

	void setupHeader(ActiveBlogsResponse infoMap) {
		if (!(view instanceof DisplayBlogEntryView)) {
			return;
		}
		DisplayBlogEntryView channelView = view;
		if (!infoMap.getInfoMap().containsKey(author)) {
			channelView.showUserNotFound(author);
			return;
		}
		AccountInfo info = infoMap.getInfoMap().get(author);
		BlogHeader blogHeader = channelView.getBlogHeader();
		blogHeader.setAvatarUrl(Routes.avatarImage(author));
		blogHeader.setDisplayName(info.getDisplayName());
		String coverImage = info.getCoverImage();
		blogHeader.setImageUrl(coverImage);
	}

	protected void loadAndDisplayBlogEntry() {
		model.getBlogEntry(author, permlink).thenAccept((entry) -> {
			if (!(view instanceof DisplayBlogEntryView)) {
				return;
			}
			DisplayBlogEntryView blogEntryView = view;
			SteemDataUtil.enableAndUpdateCardVoting(model, author, permlink, view);
			deferred(()->{
				BlogHeader blogHeader = blogEntryView.getBlogHeader();
				blogHeader.setAbout(entry.getTitle());
				blogHeader.setChannelRoute(Routes.channel(author));
			});
			view.showLoading(false);
			if (entry.getEntryType()==BlogEntryType.VIDEO) {
				blogEntryView.setEmbedUrl(Routes.embedVideo(author, permlink));
			} else {
				blogEntryView.setBodyMessage(entry.getContent());
			}
		});
	}

	@Override
	public void report(String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reply(String reply) {
		// TODO Auto-generated method stub

	}
	@Override
	public void scrollToTop() {
		Window.scrollTo(0, 0);
	}
}
