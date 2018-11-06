package co.dporn.gmd.client.presenters;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.views.ChannelUi;
import co.dporn.gmd.client.views.ContentUi;

public class AppPresenterImpl implements AppPresenter, ScheduledCommand {
	private AppLayoutView view;
	private HasWidgets rootDisplay;
	private AppControllerModel model;
	private Historian historian;

	public AppPresenterImpl() {
	}

	private final Map<String, ContentPresenter> presenters = new HashMap<>();
	private ContentPresenter activeChildPresenter;

	public void onRouteChange(ValueChangeEvent<String> routeEvent) {
		String route = routeEvent.getValue();
		loadRoutePresenter(route);
	}

	private void loadRoutePresenter(String route) {
		GWT.log("=== routeEvent: " + route);
		if (presenters.containsKey(route)) {
			deferred(() -> {
				if (activeChildPresenter != presenters.get(route)) {
					if (activeChildPresenter != null) {
						activeChildPresenter.saveScrollPosition();
					}
					activeChildPresenter = presenters.get(route);
					view.setContentPresenter(presenters.get(route));
					deferred(() -> activeChildPresenter.restoreScrollPosition());
				}
			});
		} else {
			if (activeChildPresenter != null) {
				activeChildPresenter.saveScrollPosition();
			}
			if (route.isEmpty()) {
				GWT.log("Present: Landing Page");
				deferred(() -> {
					MainContentPresenter childPresenter = new MainContentPresenter(model, new ContentUi());
					presenters.put("", childPresenter);
					activeChildPresenter = childPresenter;
					view.setContentPresenter(childPresenter);
					resetScrollPosition();
					deferred(childPresenter);
				});
			}
			if (route.startsWith("@") && !route.contains("/")) {
				GWT.log("Channel Listing: Verified Only");
			}
			if (route.startsWith("@") && !route.contains("/") && route.length()>1) {
				GWT.log("Present: Channel");
				deferred(() -> {
					ChannelPresenter childPresenter = new ChannelPresenter(route.substring(1), model, new ChannelUi());
					presenters.put(route, childPresenter);
					activeChildPresenter = childPresenter;
					view.setContentPresenter(childPresenter);
					resetScrollPosition();
					deferred(childPresenter);
				});
			}
			if (route.startsWith("@") && route.contains("/")) {
				GWT.log("Present: Post");
			}
			if (route.equals("search")) {
				GWT.log("Present: Search");
			}
			if (route.equals("upload/video")) {
				GWT.log("Upload: Video");
			}
			if (route.equals("upload/photos")) {
				GWT.log("Upload: Photogallery");
			}
			if (route.equals("settings")) {
				GWT.log("Settings");
			}
		}
	}

	private void resetScrollPosition() {
		Window.scrollTo(0, 0);
	}

	public AppPresenterImpl(Historian historian, AppControllerModel model, HasWidgets rootDisplay,
			AppLayoutView appLayoutView) {
		this.historian = historian;
		this.rootDisplay = rootDisplay;
		this.view = appLayoutView;
		this.model = model;
		this.historian.addValueChangeHandler(this::onRouteChange);
	}

	@Override
	public void setView(AppLayoutView view) {
		this.view = view;
	}

	@Override
	public void setDisplay(HasWidgets rootDisplay) {
		this.rootDisplay = rootDisplay;
	}

	@Override
	public void execute() {
		GWT.log(this.getClass().getSimpleName() + "#execute");
		rootDisplay.clear();
		rootDisplay.add(view.asWidget());
		deferred(() -> {
			loadRoutePresenter(historian.getToken());
		});
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model = model;
	}
}
