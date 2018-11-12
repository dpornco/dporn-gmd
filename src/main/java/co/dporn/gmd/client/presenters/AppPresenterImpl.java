package co.dporn.gmd.client.presenters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.app.Routes;
import co.dporn.gmd.client.views.ChannelUi;
import co.dporn.gmd.client.views.ContentUi;
import co.dporn.gmd.client.views.DisplayBlogPostUi;

public class AppPresenterImpl implements AppPresenter, ScheduledCommand, RoutePresenter {
	private AppLayoutView view;
	private HasWidgets rootDisplay;
	public void setRootDisplay(HasWidgets rootDisplay) {
		this.rootDisplay = rootDisplay;
	}

	private AppControllerModel model;

	public AppPresenterImpl() {
	}
	
	@Override
	public void account() {
		if (model.isLoggedIn()) {
			model.logout();
		} else {
			model.login();
		}
	}

	private final Map<String, ContentPresenter> presenters = new HashMap<>();
	private ContentPresenter activeChildPresenter;

	@Override
	public void loadRoutePresenter(String route) {
		GWT.log("=== routeEvent: " + route);
		//auth is a special non-display route
		if (route.startsWith("auth/")) {
			return;
		}
		
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
				GWT.log("Route: Landing Page");
				deferred(() -> {
					MainContentPresenter childPresenter = new MainContentPresenter(model, new ContentUi());
					presenters.put("", childPresenter);
					activeChildPresenter = childPresenter;
					view.setContentPresenter(childPresenter);
					resetScrollPosition();
					deferred(childPresenter);
				});
				return;
			}
			if (route.startsWith("@") && !route.contains("/") && route.length()>1) {
				GWT.log("Route: Channel");
				deferred(() -> {
					ChannelPresenter childPresenter = new ChannelPresenter(route.substring(1), model, new ChannelUi());
					presenters.put(route, childPresenter);
					activeChildPresenter = childPresenter;
					view.setContentPresenter(childPresenter);
					resetScrollPosition();
					deferred(childPresenter);
				});
				return;
			}
			if (route.equals("verified")) {
				GWT.log("List All Verified Only Channels");
				return;
			}
			if (route.startsWith("@") && route.contains("/")) {
				GWT.log("Route: Display Post");
				//DisplayBlogPostPresenter
				deferred(() -> {
					String username = StringUtils.substringBefore(route, "/").substring(1);
					String permlink = StringUtils.substringAfter(route, "/");
					GWT.log(" - username: "+username);
					GWT.log(" - permlink: "+permlink);
					DisplayBlogPostPresenter childPresenter = new DisplayBlogPostPresenterImpl(username, permlink, model, new DisplayBlogPostUi());
					GWT.log("presenters.put(route, childPresenter);");
					presenters.put(route, childPresenter);
					GWT.log("activeChildPresenter = childPresenter;");
					activeChildPresenter = childPresenter;
					GWT.log("view.setContentPresenter(childPresenter);");
					view.setContentPresenter(childPresenter);
					GWT.log("resetScrollPosition();");
					resetScrollPosition();
					GWT.log("deferred(childPresenter);");
					deferred(childPresenter);
				});
				return;
			}
			if (route.equals("search")) {
				GWT.log("Route: Search");
				return;
			}
			if (route.equals("upload/video")) {
				GWT.log("Upload: Video");
				return;
			}
			if (route.equals("upload/photos")) {
				GWT.log("Upload: Photogallery");
				return;
			}
			if (route.equals("settings")) {
				GWT.log("Settings");
				return;
			}
		}
	}

	private void resetScrollPosition() {
		Window.scrollTo(0, 0);
	}

	public AppPresenterImpl(AppControllerModel model, HasWidgets rootDisplay,
			AppLayoutView appLayoutView) {
		setRootDisplay(rootDisplay);
		setModel(model);
		setView(appLayoutView);
	}
	
	@Override
	public void setView(AppLayoutView view) {
		this.view = view;
		this.view.bindPresenter(this);
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
			model.fireRouteState();
		});
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model = model;
		this.model.setRoutePresenter(this);
	}

	@Override
	public void setUserInfo(ActiveUserInfo info) {
		if (info==null) {
			GWT.log("setUserInfo: not logged in");
			view.setAvatar(Routes.avatarImageNotLoggedIn());
			view.setDisplayname("Not Logged In");
			view.setUsername(null);
			view.enableContentCreatorRoles(false);
			return;
		}
		GWT.log("setUserInfo: "+info.getUsername()+" => "+info.getDisplayname());
		view.setAvatar(Routes.avatarImage(info.getUsername()));
		view.setDisplayname(info.getDisplayname());
		//TODO: FUTURE: view.setUsername("@"+info.getUsername());
		view.setUsername("Logout");
		view.enableContentCreatorRoles(true);
	}
}
