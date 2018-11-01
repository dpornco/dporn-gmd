package co.dporn.gmd.client.presenters;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.HasWidgets;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.ContentUi;

public class AppPresenterImpl implements AppPresenter, ScheduledCommand {
	private AppLayoutView view;
	private HasWidgets rootDisplay;
	private AppControllerModel model;
	
	public AppPresenterImpl() {
	}
	
	public AppPresenterImpl(AppControllerModel model, HasWidgets rootDisplay, AppLayoutView appLayoutView) {
		this.rootDisplay=rootDisplay;
		this.view=appLayoutView;
		this.model=model;
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
		GWT.log(this.getClass().getSimpleName()+"#execute");
		rootDisplay.clear();
		rootDisplay.add(view.asWidget());
		ContentPresenterImpl childPresenter = new ContentPresenterImpl(model, new ContentUi());
		view.replaceContentPresenter(childPresenter);
		deferred(childPresenter);
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model=model;		
	}
}
