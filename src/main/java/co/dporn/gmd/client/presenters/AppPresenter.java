package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HasWidgets;

import co.dporn.gmd.client.views.AppLayoutView;

public interface AppPresenter extends IsPresenter<AppLayoutView>, ScheduledCommand {
	void setDisplay(HasWidgets rootView);
}
