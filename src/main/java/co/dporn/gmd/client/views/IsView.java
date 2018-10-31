package co.dporn.gmd.client.views;

import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.IsPresenter;

public interface IsView<P extends IsPresenter<?>> extends IsWidget {
	void bindPresenter(P presenter);

	void unbindPresenter();
}