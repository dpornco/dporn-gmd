package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HasWidgets;

import co.dporn.gmd.client.presenters.AppPresenter.AppLayoutView;
import co.dporn.gmd.client.views.IsView;

/**
 * Talks to views via interfaces. Talks to the App Model via interfaces.
 * Implements logic to respond to user requests via views and perform operations
 * via the AppControlerModel. Only talks to View Interfaces and App Controller
 * Model interfaces. Implements "app" logic.
 */
public interface AppPresenter extends IsPresenter<AppLayoutView>, ScheduledCommand {

	public interface AppLayoutView extends IsView<AppPresenter> {
		void setContentPresenter(ContentPresenter childPresenter);
		void setUsername(String username);
		void setDisplayname(String displayname);
		void setAvatar(String avatarUrl);
	}
	void setDisplay(HasWidgets rootView);
	void account();
}
