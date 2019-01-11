package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.IsView;

public interface IsPresenter<V extends IsView<?>> {

	void setModel(AppControllerModel model);
	void setView(V view);
	void saveScrollPosition();
	void restoreScrollPosition();
	void scrollToTop();
	
	default void deferred(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
	}
}
