package co.dporn.gmd.client.presenters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SuggestOracle;

import co.dporn.gmd.client.app.AppControllerModel;
import co.dporn.gmd.client.app.Routes;
import co.dporn.gmd.client.app.TagSuggestion;
import co.dporn.gmd.client.presenters.UploadErotica.UploadEroticaView;
import co.dporn.gmd.client.presenters.UploadVideo.UploadVideoView;
import co.dporn.gmd.client.views.ChannelUi;
import co.dporn.gmd.client.views.ContentUi;
import co.dporn.gmd.client.views.DisplayBlogEntryUi;
import co.dporn.gmd.client.views.IsView;
import co.dporn.gmd.client.views.UploadEroticaUi;
import co.dporn.gmd.client.views.UploadVideoUi;
import co.dporn.gmd.shared.DpornConsts;
import gwt.material.design.client.ui.MaterialToast;

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

	private final Map<String, IsChildPresenter<? extends IsView<?>>> presenters = new HashMap<>();
	private IsChildPresenter<? extends IsView<?>> activeChildPresenter;
	private SuggestOracle tagOracle = new SuggestOracle() {
		@Override
		public void requestSuggestions(Request request, Callback callback) {
			CompletableFuture<List<String>> ftags = model.tagsOracle(request.getQuery(), request.getLimit());
			ftags.thenAccept(tags -> {
				List<TagSuggestion> suggestions = new ArrayList<>();
				for (String tag : tags) {
					suggestions.add(new TagSuggestion(tag));
				}
				Response response = new Response(suggestions);
				callback.onSuggestionsReady(request, response);
			});
		}
		@Override
		public void requestDefaultSuggestions(Request request, Callback callback) {
			CompletableFuture<List<String>> ftags = model.tagsOracle("porn", request.getLimit());
			ftags.thenAccept(tags -> {
				List<TagSuggestion> suggestions = new ArrayList<>();
				for (String tag : tags) {
					suggestions.add(new TagSuggestion(tag));
				}
				Response response = new Response(suggestions);
				callback.onSuggestionsReady(request, response);
			});
		};
	};

	private String route = "";

	@Override
	public void loadRoutePresenter(String route) {
		this.route = route;
		GWT.log("=== routeEvent: " + route);
		// auth is a special non-display route
		if (route.startsWith("auth/")) {
			return;
		}

		// content creator controls display toggle
		toggleMyChannelLinks(route);

		if (presenters.containsKey(route)) {
			deferred(() -> {
				if (activeChildPresenter != presenters.get(route)) {
					if (activeChildPresenter != null) {
						activeChildPresenter.saveScrollPosition();
					}
					activeChildPresenter = presenters.get(route);
					view.setChildPresenter(presenters.get(route));
					deferred(() -> activeChildPresenter.restoreScrollPosition());
				} else {
					activeChildPresenter.scrollToTop();
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
					view.setChildPresenter(childPresenter);
					resetScrollPosition();
					deferred(childPresenter);
				});
				return;
			}
			if (route.startsWith("@") && !route.contains("/") && route.length() > 1) {
				GWT.log("Route: Channel");
				deferred(() -> {
					String channelUsername = route.substring(1);
					ChannelPresenter childPresenter = new ChannelPresenter(channelUsername, model, new ChannelUi());
					presenters.put(route, childPresenter);
					activeChildPresenter = childPresenter;
					view.setChildPresenter(childPresenter);
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
				// DisplayBlogEntryPresenter
				deferred(() -> {
					String username = StringUtils.substringBefore(route, "/").substring(1);
					String permlink = StringUtils.substringAfter(route, "/");
					GWT.log(" - username: " + username);
					GWT.log(" - permlink: " + permlink);
					DisplayBlogEntryPresenter childPresenter = new DisplayBlogEntryPresenterImpl(username, permlink,
							model, new DisplayBlogEntryUi());
					GWT.log("presenters.put(route, childPresenter);");
					presenters.put(route, childPresenter);
					GWT.log("activeChildPresenter = childPresenter;");
					activeChildPresenter = childPresenter;
					GWT.log("view.setContentPresenter(childPresenter);");
					view.setChildPresenter(childPresenter);
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
				deferred(() -> {
					UploadVideoView childView = new UploadVideoUi(tagOracle,
							new TreeSet<>(DpornConsts.MANDATORY_VIDEO_TAGS));
					UploadVideo childPresenter = new UploadVideoImpl(model, childView);
					presenters.put(route, childPresenter);
					GWT.log("activeChildPresenter = childPresenter;");
					activeChildPresenter = childPresenter;
					GWT.log("view.setContentPresenter(childPresenter);");
					view.setChildPresenter(childPresenter);
					GWT.log("resetScrollPosition();");
					resetScrollPosition();
					GWT.log("deferred(childPresenter);");
					deferred(childPresenter);
				});
				return;
			}
			if (route.equals("upload/photos")) {
				GWT.log("Upload: Photogallery");
				return;
			}
			if (route.equals("upload/erotica")) {
				GWT.log("Upload: Erotica");
				deferred(() -> {
					UploadEroticaView childView = new UploadEroticaUi(tagOracle,
							new TreeSet<>(DpornConsts.MANDATORY_BLOG_TAGS));
					UploadErotica childPresenter = new UploadEroticaImpl(model, childView);
					presenters.put(route, childPresenter);
					GWT.log("activeChildPresenter = childPresenter;");
					activeChildPresenter = childPresenter;
					GWT.log("view.setContentPresenter(childPresenter);");
					view.setChildPresenter(childPresenter);
					GWT.log("resetScrollPosition();");
					resetScrollPosition();
					GWT.log("deferred(childPresenter);");
					deferred(childPresenter);
				});
				return;
			}
			if (route.equals("settings")) {
				GWT.log("Settings");
				return;
			}
		}
	}

	private void toggleMyChannelLinks(String route) {
		deferred(() -> {
			if (route.startsWith("@") && !route.contains("/")) {
				String channelUsername = route.substring(1);
				if (username != null && !username.trim().isEmpty()) {
					GWT.log("Content Creator Roles Enable: " + channelUsername.equals(username));
					view.enableContentCreatorRoles(channelUsername.equals(username));
					disableUnimplementedFeatures();
				} else {
					view.enableContentCreatorRoles(false);
					disableUnimplementedFeatures();
					GWT.log("Content Creator Roles Enable: " + false);
				}
			} else {
				view.enableContentCreatorRoles(false);
				disableUnimplementedFeatures();
				GWT.log("Content Creator Roles Enable: " + false);
			}
		});
	}

	private void resetScrollPosition() {
		Window.scrollTo(0, 0);
	}

	public AppPresenterImpl(AppControllerModel model, HasWidgets rootDisplay, AppLayoutView appLayoutView) {
		setRootDisplay(rootDisplay);
		setModel(model);
		setView(appLayoutView);
		disableUnimplementedFeatures();
	}

	private void disableUnimplementedFeatures() {
		String hostName = Location.getHostName();
		if (hostName.startsWith("localhost") || hostName.startsWith("dev")) {
			view.enableUnimplementedFeatures(true);
		} else {
			view.enableUnimplementedFeatures(false);
		}
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
		deferred(() -> model.fireRouteState());
		deferred(this::notificationsWatch);
		model.isVerified().thenAccept(verified->view.setVerified(verified));
	}
	
	private Timer notificationsWatch=null;
	private void notificationsWatch() {
		if (notificationsWatch!=null) {
			notificationsWatch.cancel();
		}
		notificationsWatch = new Timer() {
			@Override
			public void run() {
				model.getNotifications().thenAccept(response->{
					if (response==null || response.getNotifications()==null) {
						return;
					}
					for (String notice: response.getNotifications()) {
						MaterialToast.fireToast(notice);
					}
				}).exceptionally(ex->{
					return null;
				});
			}
		};
		notificationsWatch.scheduleRepeating(3000);
	}

	@Override
	public void setModel(AppControllerModel model) {
		this.model = model;
		this.model.setRoutePresenter(this);
	}

	private String username;

	@Override
	public void setUserInfo(ActiveUserInfo info) {
		if (info == null) {
			GWT.log("setUserInfo: not logged in");
			username = "";
			view.setAvatar(Routes.avatarImageNotLoggedIn());
			view.setDisplayname("Not Logged In");
			view.setUsername(null);
			view.enableContentCreatorRoles(false);
			return;
		}
		username = info.getUsername();
		String displayname = info.getDisplayname();
		if (displayname == null || displayname.trim().isEmpty()) {
			displayname = username;
		}
		GWT.log("setUserInfo: " + username + " => " + displayname);
		view.setAvatar(Routes.avatarImage(username));
		view.setDisplayname(displayname);
		view.setUsername(username);
		toggleMyChannelLinks(route);
	}

	private int posX = 0;
	private int posY = 0;

	@Override
	public void saveScrollPosition() {
		posX = Window.getScrollLeft();
		posY = Window.getScrollTop();
	}

	@Override
	public void restoreScrollPosition() {
		Window.scrollTo(posX, posY);
	}

	@Override
	public void toast(String message) {
		view.toast(message);
	}

	@Override
	public void scrollToTop() {
		Window.scrollTo(0, 0);
	}
}
