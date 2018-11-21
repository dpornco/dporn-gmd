package co.dporn.gmd.client.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.Window.Location;
import com.wallissoftware.pushstate.client.PushStateHistorian;

import co.dporn.gmd.client.ClientRestClient;
import co.dporn.gmd.client.presenters.RoutePresenter;
import co.dporn.gmd.client.presenters.RoutePresenter.ActiveUserInfo;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.IpfsHashResponse;
import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.PostListResponse;
import co.dporn.gmd.shared.TagSet;
import elemental2.dom.Blob;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;
import steem.SteemApi;
import steem.connect.SteemConnectInit;
import steem.connect.SteemConnectV2;
import steem.connect.model.SteemAccountMetadata;
import steem.connect.model.SteemAccountMetadata.AccountProfile;
import steem.connect.model.SteemConnectMe;
import steem.model.CommentMetadata;
import steem.model.DiscussionComment;
import steem.model.Vote;

public class AppControllerModelImpl implements AppControllerModel {
	private static final String STEEMCONNECT_KEY = "steemconnectv2";
	private static final String STEEM_USERNAME_KEY = "steem-username";
	private static final int FEATURED_POST_POOL_SIZE = 16;
	private static final int CHANNEL_POSTS_INITIAL_SIZE = 8;
	private SteemConnectV2 sc2api;
	private boolean loggedIn;

	private PushStateHistorian historian;
	private RoutePresenter routePresenter;

	private Map<String, String> appModelCache;

	public AppControllerModelImpl(PushStateHistorian historian) {
		initAppModelCache();
		initSteemConnect();
		this.historian = historian;
		this.historian.addValueChangeHandler(this::onRouteChange);
	}

	private void initSteemConnect() {
		SteemConnectInit initializeParam = new SteemConnectInit();
		initializeParam.setApp("dpornco.app");
		initializeParam.setCallbackUrl(Location.getProtocol() + "//" + Location.getHost() + "/auth/");
		initializeParam.setScopes("login", "vote", "comment", "delete_comment", "comment_options", "custom_json",
				"claim_reward_balance");
		String accessToken = appModelCache.getOrDefault(STEEMCONNECT_KEY, "");
		initializeParam.setAccessToken(accessToken);
		sc2api = SteemConnectV2.initialize(initializeParam);
		if (!accessToken.trim().isEmpty()) {
			autoLogin();
		}
	}

	private void initAppModelCache() {
		Storage localStorageIfSupported = Storage.getLocalStorageIfSupported();
		if (localStorageIfSupported != null) {
			appModelCache = new StorageMap(localStorageIfSupported);
		} else {
			appModelCache = new HashMap<>();
		}
	}

	public void onRouteChange(ValueChangeEvent<String> routeEvent) {
		onRouteChange(routeEvent.getValue());
	}

	private void onRouteChange(String route) {
		// "auth/" is a special non-presenter route
		if (route.startsWith("auth/")) {
			route = StringUtils.substringAfter(route, "auth/");
			if (route.startsWith("?")) {
				route = route.substring(1);
			}
			String parts[] = route.split("&");
			if (parts == null || parts.length == 0) {
				// something wrong, panic navigate to "HOME"
				this.routePresenter.loadRoutePresenter("");
				return;
			}
			String accessToken = Location.getParameter("access_token");
			String state = Location.getParameter("state");
			appModelCache.put(STEEMCONNECT_KEY, accessToken == null ? "" : accessToken);
			sc2api.setAccessToken(accessToken);
			if (accessToken != null && !accessToken.trim().isEmpty()) {
				sc2api.me().thenAccept(this::processMeResponse).exceptionally(this::logout);
			}
			deferred(() -> PushStateHistorian.replaceItem(state == null ? "" : state, true));
			return;
		}

		this.routePresenter.loadRoutePresenter(route);
	}

	private Void logout(Throwable e) {
		GWT.log("=== logout: " + e.getMessage());
		deferred(() -> logout());
		return null;
	}

	private void processMeResponse(JSONObject jsonObject) {
		String json = jsonObject.toString();
		deferred(() -> {
			SteemConnectMe me;
			String displayName;
			try {
				me = SteemConnectMe.deserialize(json);
			} catch (Exception e) {
				GWT.log(e.getMessage(), e);
				return;
			}
			displayName = null;
			SteemAccountMetadata metadata;
			try {
				metadata = me.getAccount().getMetadata();
			} catch (Exception e) {
				GWT.log(e.getMessage(), e);
				metadata = null;
			}
			if (metadata != null) {
				AccountProfile profile = metadata.getProfile();
				if (profile != null) {
					displayName = profile.getName();
				}
			}
			loggedIn = true;
			appModelCache.put(STEEM_USERNAME_KEY, me.getUser());
			routePresenter.setUserInfo(new ActiveUserInfo(me.getUser(), displayName == null ? "" : displayName.trim()));
		});
	}

	@Override
	public CompletableFuture<DiscussionComment> getDiscussionComment(String username, String permlink) {
		return SteemApi.getContent(username, permlink);
	}

	@Override
	public CompletableFuture<PostListResponse> postsFor(String username) {
		return ClientRestClient.get().postsFor(username, CHANNEL_POSTS_INITIAL_SIZE);
	}

	@Override
	public CompletableFuture<PostListResponse> postsFor(String username, String startId, int count) {
		return ClientRestClient.get().postsFor(username, startId, count);
	}

	@Override
	public CompletableFuture<PostListResponse> listPosts(int count) {
		return ClientRestClient.get().posts(count);
	}

	@Override
	public CompletableFuture<PostListResponse> listPosts(String startId, int count) {
		return ClientRestClient.get().posts(startId == null ? "" : startId, count);
	}

	@Override
	public CompletableFuture<ActiveBlogsResponse> listFeatured() {
		return ClientRestClient.get().listFeatured();
	}

	/**
	 * TODO: Move this into the servlet and cache for at least 30 minutes.
	 */
	@Override
	public CompletableFuture<PostListResponse> featuredPosts(int count) {
		CompletableFuture<PostListResponse> finalFuture = new CompletableFuture<>();
		listPosts(FEATURED_POST_POOL_SIZE).thenAccept((response) -> {
			List<CompletableFuture<List<Vote>>> voteFutures = new ArrayList<>();
			List<Post> list = new ArrayList<>();
			double mul = 1.0d;
			for (int ix = 0; ix < response.getPosts().size(); ix++) {
				mul = mul * .9d;
				final double weight = mul;
				Post post = response.getPosts().get(ix);
				CompletableFuture<List<Vote>> voteFuture = SteemApi.getActiveVotes(post.getAuthor(),
						post.getPermlink());
				voteFuture.thenAccept((v) -> {
					post.setScore((double) v.size() * weight);
					synchronized (list) {
						list.add(post);
					}
				});
				voteFutures.add(voteFuture);
			}
			CompletableFuture.allOf(voteFutures.toArray(new CompletableFuture<?>[0])).thenRun(() -> {
				// desc by score
				Collections.sort(response.getPosts(), (a, b) -> -Double.compare(a.getScore(), b.getScore()));
				int size = response.getPosts().size();
				response.getPosts().subList(Math.min(count, size), size).clear();
				deferred(() -> finalFuture.complete(response));
			});
		});
		return finalFuture;
	}

	private void deferred(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
	}

	@Override
	public CompletableFuture<ActiveBlogsResponse> blogInfo(String username) {
		return ClientRestClient.get().blogInfo(username);
	}

	@Override
	public CompletableFuture<Void> autoLogin() {
		return sc2api.me().thenAccept(this::processMeResponse).exceptionally(this::logout);
	}

	@Override
	public boolean isLoggedIn() {
		return loggedIn;
	}

	@Override
	public void login() {
		String token = new PushStateHistorian().getToken();
		String loginURL = sc2api.getLoginURL(token);
		Location.assign(loginURL);
	}

	@Override
	public void fireRouteState() {
		onRouteChange(historian.getToken());
	}

	@Override
	public void setRoutePresenter(RoutePresenter presenter) {
		this.routePresenter = presenter;
	}

	@Override
	public void showAccountSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() {
		GWT.log("logout");
		appModelCache.remove(STEEMCONNECT_KEY);
		appModelCache.remove(STEEM_USERNAME_KEY);
		sc2api.revokeToken();
		loggedIn = false;
		deferred(() -> {
			sc2api.removeAccessToken();
			routePresenter.setUserInfo(null);
		});
	}

	@Override
	public CompletableFuture<List<String>> tagsOracle(final String prefix, int limit) {
		GWT.log("suggest: " + prefix + " [" + limit + "]");
		final List<String> tags = new ArrayList<>();
		CompletableFuture<List<String>> future = new CompletableFuture<>();
		ClientRestClient.get().suggest(prefix == null ? "" : prefix.trim()).thenAccept(r -> {
			for (String tag : r.getTags()) {
				if (tag.trim().isEmpty()) {
					continue;
				}
				tags.add(tag);
				if (tags.size() >= limit) {
					break;
				}
			}
			if (!prefix.trim().isEmpty()) {
				tags.add(prefix);
			}
			future.complete(new ArrayList<>(new TreeSet<>(tags)));
		}).exceptionally((e) -> {
			future.completeExceptionally(e);
			return null;
		});
		return future;
	}

	protected interface IpfsHashResponseMapper extends ObjectMapper<IpfsHashResponse> {
		IpfsHashResponseMapper mapper = GWT.create(IpfsHashResponseMapper.class);
	}

	@Override
	public CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob, OnprogressFn onprogress) {
		CompletableFuture<String> future = new CompletableFuture<>();
		String authorization = appModelCache.getOrDefault(STEEMCONNECT_KEY, "");
		String username = appModelCache.getOrDefault(STEEM_USERNAME_KEY, "");
		if (authorization.trim().isEmpty()) {
			future.completeExceptionally(new RuntimeException("NOT AUTHORIZED"));
			routePresenter.toast("UPLOAD NOT AUTHORIZED");
			return future;
		}
		ClientRestClient.get().postBlobToIpfs(username, authorization, filename, blob, onprogress)
				.thenAccept((response) -> {
					try {
						IpfsHashResponse hash = IpfsHashResponseMapper.mapper.read(response);
						if (hash.getLocation()==null || hash.getLocation().trim().isEmpty()) {
							future.completeExceptionally(new RuntimeException("NO IPFS PATH RETURNED"));
							return;
						}
						future.complete(hash.getLocation());
					} catch (JsonDeserializationException e) {
						future.completeExceptionally(e);
						return;
					}
				}).exceptionally((ex) -> {
					future.completeExceptionally(ex);
					return null;
				});
		return future;
	}

	@Override
	public CompletableFuture<List<TagSet>> recentTagSets(String username, String mustHaveTag) {
		mustHaveTag = mustHaveTag.trim().toLowerCase();
		final String lookFor = mustHaveTag;
		CompletableFuture<List<TagSet>> future = new CompletableFuture<List<TagSet>>();
		steem.SteemApi.getDiscussionsByBlog(username).thenAccept(list -> {
			List<TagSet> tagsets = new ArrayList<>();
			List<TagSet> fallbacksets = new ArrayList<>();
			int tagsetsChoiceSize = 6;
			scanlist: for (DiscussionComment comment : list) {
				CommentMetadata metadata = CommentMetadata.fromJson(comment.getJsonMetadata());
				List<String> tags = metadata.getTags();
				if (tags == null) {
					continue;
				}
				// normalize tags
				TagSet ts = new TagSet();
				Iterator<String> iter = tags.iterator();
				while (iter.hasNext()) {
					String tag = iter.next();
					tag = tag.trim().toLowerCase().replace(" ", "-").replaceAll("-+", "-");
					if (tag.isEmpty()) {
						continue;
					}
					ts.getTags().add(tag);
				}
				if (!ts.getTags().contains(lookFor)) {
					if (!fallbacksets.contains(ts)) {
						fallbacksets.add(ts);
					}
					continue;
				}
				if (!tagsets.contains(ts)) {
					tagsets.add(ts);
					if (tagsets.size() >= tagsetsChoiceSize) {
						break scanlist;
					}
				}
			}
			if (tagsets.size() < tagsetsChoiceSize) {
				Iterator<TagSet> iterFallback = fallbacksets.iterator();
				while (iterFallback.hasNext() && tagsets.size() < tagsetsChoiceSize) {
					tagsets.add(iterFallback.next());
				}
			}
			future.complete(tagsets);
		}).exceptionally(ex -> {
			future.completeExceptionally(ex);
			return null;
		});
		return future;
	}
}
