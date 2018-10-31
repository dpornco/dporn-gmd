package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.AppPresenter.AppLayoutView;
import co.dporn.gmd.client.presenters.IsPresenter.IsView;

public interface AppPresenter extends IsPresenter<AppLayoutView>, ScheduledCommand {

	public interface AppLayoutView extends IsView<AppPresenter> {
		IsWidget getMainContainerView(); 
	}

	void setDisplay(HasWidgets rootView);
}
