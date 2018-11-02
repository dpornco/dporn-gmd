package co.dporn.gmd.client.presenters;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.ContentPresenter.ContentView;
import co.dporn.gmd.client.views.IsView;
import gwt.material.design.client.constants.ImageType;

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
		void setBlurb(String blurb);
		void setType(ImageType type);
	}
	public ContentView getContentView();
}
