package co.dporn.gmd.client.presenters;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.ContentPresenter.ContentView;
import co.dporn.gmd.client.views.IsView;

public interface ContentPresenter extends IsPresenter<ContentView> {
	public static interface ContentView extends IsView<ContentPresenter> {
		IsWidget getContainer();
		void clear();
		HasWidgets getFeatured();
		HasWidgets getPosts();
		void animateFeatured();
		void hideFeatured();
	}
	public static interface BlogCardView extends IsView<ContentPresenter> {
		void setImageUrl(String url);
		void setAvatarUrl(String url);
		void setAuthorName(String name);
		void setTitle(String title);
		void setShowDelay(int showDelay);
	}
	public static interface VideoCardView extends BlogCardView {
		void setHlsVideoUrl(String url);
		void setIpfsVideoUrl(String url);
	}
	public static interface PhotoGalleryCardView extends BlogCardView {
		void setGallerySourceUrl(String url);
	}
	public ContentView getContentView();
}
