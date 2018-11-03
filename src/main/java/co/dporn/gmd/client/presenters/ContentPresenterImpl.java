package co.dporn.gmd.client.presenters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.BlogCardUi;
import co.dporn.gmd.client.views.VideoCardUi;
import co.dporn.gmd.shared.AccountInfo;

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

	private void loadRecentPosts() {
		model.listPosts(4).thenAccept((l)->{
			GWT.log("Recent posts: "+l.getPosts().size());
			int[] showDelay = { 0 };
			getContentView().getRecentPosts().clear();
			Map<String, AccountInfo> infoMap = l.getInfoMap();
			l.getPosts().forEach(p->{
				AccountInfo i = infoMap.get(p.getAuthor());
				if (i == null) {
					return;
				}
				VideoCardUi card = new VideoCardUi();
				card.setAuthorName(p.getAuthor());
				card.setShowDelay(showDelay[0]);
				showDelay[0] += 75;
				card.setAuthorName(i.getDisplayName());
				String profileImage = i.getProfileImage();
				if (profileImage != null && !profileImage.isEmpty()) {
					if (!profileImage.toLowerCase().startsWith("https://steemitimages.com/")) {
						profileImage = "https://steemitimages.com/150x150/" + profileImage;
					}
					card.setAvatarUrl(profileImage);
				}
				card.setTitle(p.getTitle());
				String videoIpfs = p.getVideoIpfs();
				if (videoIpfs == null || videoIpfs.trim().isEmpty() || videoIpfs.length()!=46) {
					return;
				}
				String embedUrl = GWT.getHostPageBaseURL()+"embed/@"+p.getAuthor()+"/"+p.getPermlink();
				card.setVideoEmbedUrl(embedUrl);
				deferred(() -> getContentView().getRecentPosts().add(card));
			});
		});
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
