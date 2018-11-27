package co.dporn.gmd.client.presenters;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.AppPresenter.IsChildPresenter;
import co.dporn.gmd.client.presenters.ContentPresenter.ContentView;
import co.dporn.gmd.client.views.IsView;

public interface ContentPresenter extends IsChildPresenter<ContentView> {
	interface ContentView extends IsView<ContentPresenter> {
		IsWidget getContainer();

		void clear();

		HasWidgets getFeaturedChannels();

		HasWidgets getFeaturedPosts();

		HasWidgets getRecentPosts();

		void showLoading(boolean loading);
	}

	interface ChannelView extends ContentView {
		BlogHeader getBlogHeader();
		void showUserNotFound(String username);
	}

	interface BlogHeader extends BlogCardView {
		void setFollowing(boolean following);
		void setChannelRoute(String route);
		void setBusyLink(String username);
		void setSteemitLink(String username);
		void setDisplayName(String displayName);
		void setAbout(String about);
	}

	interface BlogCardView extends IsView<ContentPresenter> {
		void setImageUrl(String url);

		void setAvatarUrl(String url);

		void setDisplayName(String name);
		
		void setChannelLink(String linkUrl);

		void setTitle(String title);

		void setShowDelay(int showDelay);

		void setViewLink(String linkUrl);

		void setChannelLinkVisible(boolean visible);

		void setViewLinkVisible(boolean visible);
	}

	interface VideoCardView extends BlogCardView {
		void setVideoEmbedUrl(String url);
	}

	public static interface PhotoGalleryCardView extends BlogCardView {
		void setGallerySourceUrl(String url);
	}
	ContentView getContentView();
}
