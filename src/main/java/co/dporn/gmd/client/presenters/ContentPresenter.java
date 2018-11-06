package co.dporn.gmd.client.presenters;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.ContentPresenter.ContentView;
import co.dporn.gmd.client.views.IsView;

public interface ContentPresenter extends IsPresenter<ContentView> {
	interface ContentView extends IsView<ContentPresenter> {
		IsWidget getContainer();
		void clear();
		HasWidgets getFeaturedChannels();
		HasWidgets getFeaturedPosts();
		HasWidgets getRecentPosts();
		void showLoading(boolean loading);
	}
	interface BlogCardView extends IsView<ContentPresenter> {
		void setImageUrl(String url);
		void setAvatarUrl(String url);
		void setDisplayName(String name);
		void setTitle(String title);
		void setShowDelay(int showDelay);
		void setViewLink(String linkUrl);
	}
	interface VideoCardView extends BlogCardView {
		void setVideoEmbedUrl(String url);
	}
	public static interface PhotoGalleryCardView extends BlogCardView {
		void setGallerySourceUrl(String url);
	}
	ContentView getContentView();
	void saveScrollPosition();
	void restoreScrollPosition();
}
