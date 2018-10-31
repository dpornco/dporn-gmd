package co.dporn.gmd.client.presenters;

import com.google.gwt.user.client.ui.HasWidgets;
import co.dporn.gmd.client.presenters.IsPresenter.IsView;

import co.dporn.gmd.client.presenters.ListPostsPresenter.ListPostsView;

public interface ListPostsPresenter extends IsPresenter<ListPostsView> {

	public interface ListPostsView extends IsView<ListPostsPresenter> {
	}

	void setDisplay(HasWidgets rootView);
}
