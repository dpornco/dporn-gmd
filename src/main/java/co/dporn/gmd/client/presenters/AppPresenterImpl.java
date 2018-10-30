package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.HasWidgets;

import co.dporn.gmd.client.views.AppLayoutView;
import gwt.material.design.client.ui.MaterialLoader;

public class AppPresenterImpl implements AppPresenter, ScheduledCommand {
	private AppLayoutView view;
	private HasWidgets rootDisplay;
	
	public AppPresenterImpl() {
	}
	
	public AppPresenterImpl(HasWidgets rootDisplay, AppLayoutView appLayoutView) {
		this.rootDisplay=rootDisplay;
		this.view=appLayoutView;
	}
	
	@Override
	public void setView(AppLayoutView view) {
		this.view=view;
	}

	@Override
	public void setDisplay(HasWidgets rootDisplay) {
		this.rootDisplay = rootDisplay;
	}

	@Override
	public void execute() {
		rootDisplay.clear();
		rootDisplay.add(view.asWidget());
		MaterialLoader.loading(false);
	}
}
