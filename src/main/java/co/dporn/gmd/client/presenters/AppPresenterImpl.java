package co.dporn.gmd.client.presenters;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;
import com.google.gwt.user.client.ui.HasWidgets;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.ContentUi;

public class AppPresenterImpl implements AppPresenter, ScheduledCommand {
	private AppLayoutView view;
	private HasWidgets rootDisplay;
	private AppControllerModel model;
	private Historian historian;
	
	public AppPresenterImpl() {
	}
	
	private final Map<String, ContentPresenter> presenters=new HashMap<>();
	private ContentPresenter activeChildPresenter;
	public void onRouteChange(ValueChangeEvent<String> routeEvent) {
		String route = routeEvent.getValue();
		GWT.log("=== routeEvent: "+route);
		if (presenters.containsKey(route)) {
			deferred(()->{
				activeChildPresenter.saveScrollPosition();
				activeChildPresenter=presenters.get(route);
				view.setContentPresenter(presenters.get(route));
				deferred(()->activeChildPresenter.restoreScrollPosition());
			});
		} else {
			//TODO: do new presenter and views setup
		}
	}
	
	public AppPresenterImpl(Historian historian, AppControllerModel model, HasWidgets rootDisplay, AppLayoutView appLayoutView) {
		this.historian = historian;
		this.rootDisplay=rootDisplay;
		this.view=appLayoutView;
		this.model=model;
		this.historian.addValueChangeHandler(this::onRouteChange);
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
		MainContentPresenter childPresenter = new MainContentPresenter(model, new ContentUi());
		view.setContentPresenter(childPresenter);
		deferred(childPresenter);
		presenters.put("", childPresenter);
		activeChildPresenter = childPresenter;
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model=model;		
	}
}
