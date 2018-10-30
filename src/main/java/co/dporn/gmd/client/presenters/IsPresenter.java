package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.IsWidget;

import co.dporn.gmd.client.presenters.IsPresenter.IsView;

public interface IsPresenter<V extends IsView<?>> {

	interface IsView<P extends IsPresenter<?>> extends IsWidget {
		void bindPresenter(P presenter);

		void unbindPresenter();
	}

	void setView(V view);

	default void deferred(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
	}
}
