package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.IsView;

public interface IsPresenter<V extends IsView<?>> {

	void setModel(AppControllerModel model);
	
	void setView(V view);

	default void deferred(ScheduledCommand cmd) {
		GWT.log(" -> deferred: "+cmd.getClass().getSimpleName());
		Scheduler.get().scheduleDeferred(cmd);
	}
}
