package co.dporn.gmd.client.app;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.Window.Location;
import com.wallissoftware.pushstate.client.PushStateHistorian;

import co.dporn.gmd.client.ClientRestClient;
import co.dporn.gmd.client.presenters.RoutePresenter;
import co.dporn.gmd.client.presenters.RoutePresenter.ActiveUserInfo;
import co.dporn.gmd.client.utils.HtmlReformatter;
import co.dporn.gmd.shared.ActiveBlogsResponse;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.DpornConsts;
import co.dporn.gmd.shared.IpfsHashResponse;
import co.dporn.gmd.shared.Post;
import co.dporn.gmd.shared.PostListResponse;
import co.dporn.gmd.shared.TagSet;
import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;
import gwt.material.design.client.ui.MaterialToast;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;
import jsinterop.base.Js;
import steem.SteemApi;
import steem.connect.SteemConnectInit;
import steem.connect.SteemConnectV2;
import steem.connect.model.SteemAccountMetadata;
import steem.connect.model.SteemAccountMetadata.AccountProfile;
import steem.connect.model.SteemConnectMe;
import steem.model.Beneficiary;
import steem.model.CommentMetadata;
import steem.model.DiscussionComment;
import steem.model.TrendingTag;
import steem.model.Vote;

public class AppControllerModelImpl implements AppControllerModel {
	private static final String USERNAME_PATTERN = "(@[a-z0-9][a-z0-9\\-\\.]*?[a-z0-9])";

	protected interface IpfsHashResponseMapper extends ObjectMapper<IpfsHashResponse> {
		IpfsHashResponseMapper mapper = GWT.create(IpfsHashResponseMapper.class);
	}

	private static final int CHANNEL_POSTS_INITIAL_SIZE = 8;
	private static final int FEATURED_POST_POOL_MULTIPLIER_SIZE = 4;
	private static final String STEEM_USERNAME_KEY = "steem-username";
	private static final String STEEMCONNECT_KEY = "steemconnectv2";
	private static final int TAGSETS_MAX_SIZE = 6;

	private Map<String, String> appModelCache;
	private PushStateHistorian historian;

	private boolean loggedIn;

	private RoutePresenter routePresenter;

	private SteemConnectV2 sc2api;

	public AppControllerModelImpl(PushStateHistorian historian) {
		initAppModelCache();
		initSteemConnect();
		this.historian = historian;
		this.historian.addValueChangeHandler(this::onRouteChange);
	}

	@Override
	public CompletableFuture<Void> autoLogin() {
		return sc2api.me().thenAccept(this::processMeResponse).exceptionally(this::logout);
	}

	@Override
	public CompletableFuture<ActiveBlogsResponse> blogInfo(String username) {
		return ClientRestClient.get().blogInfo(username);
	}

	private void deferred(ScheduledCommand cmd) {
		Scheduler.get().scheduleDeferred(cmd);
	}

	/**
	 * TODO: Move this into the servlet and cache for at least 30 minutes.
	 */
	@Override
	public CompletableFuture<PostListResponse> featuredPosts(int count) {
		CompletableFuture<PostListResponse> finalFuture = new CompletableFuture<>();
		listPosts(FEATURED_POST_POOL_MULTIPLIER_SIZE * count).thenAccept((response) -> {
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
				}).exceptionally(ex -> {
					GWT.log(ex.getMessage(), ex);
					return null;
				});
				voteFutures.add(voteFuture);
			}
			CompletableFuture.allOf(voteFutures.toArray(new CompletableFuture<?>[0])).thenRun(() -> {
				// desc by score
				Collections.sort(response.getPosts(), (a, b) -> -Double.compare(a.getScore(), b.getScore()));
				int size = response.getPosts().size();
				response.getPosts().subList(Math.min(count, size), size).clear();
				deferred(() -> finalFuture.complete(response));
			}).exceptionally(ex -> {
				GWT.log(ex.getMessage(), ex);
				// desc by score
				Collections.sort(response.getPosts(), (a, b) -> -Double.compare(a.getScore(), b.getScore()));
				int size = response.getPosts().size();
				response.getPosts().subList(Math.min(count, size), size).clear();
				deferred(() -> finalFuture.complete(response));
				return null;
			});
		}).exceptionally(ex -> {
			Throwable cause = ex.getCause();
			if (cause != null) {
				DomGlobal.console.log(cause);
			} else {
				DomGlobal.console.log(ex);
			}
			return null;
		});
		return finalFuture;
	}

	@Override
	public void fireRouteState() {
		onRouteChange(historian.getToken());
	}

	@Override
	public CompletableFuture<DiscussionComment> getDiscussionComment(String username, String permlink) {
		return SteemApi.getContent(username, permlink);
	}

	private void initAppModelCache() {
		Storage localStorageIfSupported = Storage.getLocalStorageIfSupported();
		if (localStorageIfSupported != null) {
			appModelCache = new StorageMap(localStorageIfSupported);
		} else {
			appModelCache = new HashMap<>();
		}
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

	@Override
	public boolean isLoggedIn() {
		return loggedIn;
	}

	@Override
	public CompletableFuture<ActiveBlogsResponse> listFeatured() {
		return ClientRestClient.get().listFeatured();
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
	public void login() {
		String token = new PushStateHistorian().getToken();
		String loginURL = sc2api.getLoginURL(token);
		Location.assign(loginURL);
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

	private Void logout(Throwable e) {
		GWT.log("=== logout: " + e.getMessage());
		deferred(() -> logout());
		return null;
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

	public void onRouteChange(ValueChangeEvent<String> routeEvent) {
		onRouteChange(routeEvent.getValue());
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
						if (hash.getLocation() == null || hash.getLocation().trim().isEmpty()) {
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
	public CompletableFuture<PostListResponse> postsFor(String username) {
		return ClientRestClient.get().postsFor(username, CHANNEL_POSTS_INITIAL_SIZE);
	}

	@Override
	public CompletableFuture<PostListResponse> postsFor(String username, String startId, int count) {
		return ClientRestClient.get().postsFor(username, startId, count);
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
	public CompletableFuture<List<TagSet>> recentTagSets(String mustHaveTag) {
		mustHaveTag = mustHaveTag == null ? "" : mustHaveTag.trim().toLowerCase();
		final String lookFor = mustHaveTag;
		final CompletableFuture<List<TagSet>> future = new CompletableFuture<List<TagSet>>();
		String username = appModelCache.getOrDefault(STEEM_USERNAME_KEY, "");
		steem.SteemApi.getDiscussionsByBlog(username).thenAccept(list -> {
			List<TagSet> tagsets = new ArrayList<>();
			List<TagSet> dpornsets = new ArrayList<>();
			List<TagSet> fallbacksets = new ArrayList<>();
			scanlist: for (DiscussionComment comment : list) {
				if (!comment.getAuthor().equals(username)) {
					// skip reblogs
					continue;
				}
				if (tagsets.size() >= TAGSETS_MAX_SIZE) {
					break scanlist;
				}
				String jsonMetadata = comment.getJsonMetadata();
				if (jsonMetadata == null) {
					GWT.log("Missing comment json metadata");
					continue;
				}
				CommentMetadata metadata = CommentMetadata.fromJson(jsonMetadata);
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
				boolean isDpornSet = ts.getTags().contains("dporn") || ts.getTags().contains("dpornco")
						|| ts.getTags().contains("dporncovideo");
				// remove dporn/nsfw specific tags for display purposes
				ts.getTags().remove("dporn");
				ts.getTags().remove("dpornco");
				ts.getTags().remove("dporncovideo");
				ts.getTags().remove("nsfw");
				if (!lookFor.isEmpty() && ts.getTags().contains(lookFor)) {
					if (!tagsets.contains(ts)) {
						tagsets.add(ts);
					}
					continue;
				}
				if (isDpornSet) {
					if (!dpornsets.contains(ts)) {
						dpornsets.add(ts);
					}
					continue;
				}
				if (!fallbacksets.contains(ts)) {
					fallbacksets.add(ts);
				}
				continue;
			}
			if (tagsets.size() < TAGSETS_MAX_SIZE) {
				Iterator<TagSet> iterFallback = dpornsets.iterator();
				while (iterFallback.hasNext() && tagsets.size() < TAGSETS_MAX_SIZE) {
					tagsets.add(iterFallback.next());
				}
			}
			if (tagsets.size() < TAGSETS_MAX_SIZE) {
				Iterator<TagSet> iterFallback = fallbacksets.iterator();
				while (iterFallback.hasNext() && tagsets.size() < TAGSETS_MAX_SIZE) {
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

	@Override
	public void setRoutePresenter(RoutePresenter presenter) {
		this.routePresenter = presenter;
	}

	@Override
	public void showAccountSettings() {
		// TODO Auto-generated method stub

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

	@Override
	public CompletableFuture<List<String>> sortTagsByNetVoteDesc(List<String> tags) {
		CompletableFuture<List<String>> future = new CompletableFuture<>();
		if (tags == null) {
			future.completeExceptionally(new NullPointerException("Tags to be sorted cannot be null"));
			return future;
		}

		// always make sure we have "dporn" and "nsfw" in the tag list
		if (!tags.contains("dporn")) {
			tags.add("dporn");
		}
		if (!tags.contains("nsfw")) {
			tags.add("nsfw");
		}

		List<TrendingTag> trendingList = new ArrayList<>();
		List<CompletableFuture<List<TrendingTag>>> futures = new ArrayList<>();
		for (String tag : tags) {
			if (tag == null || tag.trim().isEmpty()) {
				continue;
			}
			tag = tag.trim();
			CompletableFuture<List<TrendingTag>> futureTrendingTags = SteemApi.getTrendingTags(tag, 1);
			futureTrendingTags.thenAccept(list -> {
				if (list == null || list.isEmpty()) {
					return;
				}
				for (TrendingTag t : list) {
					GWT.log(String.valueOf(t.getName()) + ": " + String.valueOf(t.getNetVotes()));
				}
				synchronized (trendingList) {
					trendingList.addAll(list);
				}
			}).exceptionally(ex -> {
				if (ex == null) {
					GWT.log("NULL EXCEPTION: futureTrendingTags in sortTagsByNetVoteDesc");
					return null;
				}
				GWT.log(ex.getMessage(), ex);
				return null;
			});
			futures.add(futureTrendingTags);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).thenRun(() -> {
			BigInteger totalNetVotes = BigInteger.ZERO;
			// get sum of weights and then pre-weight several special tags like "dporn" and
			// "nsfw" to always be first in sorted list
			for (TrendingTag t : trendingList) {
				totalNetVotes = totalNetVotes.add(t.getNetVotes());
			}
			for (TrendingTag t : trendingList) {
				if (t.getName().equals("dporn")) {
					t.setNetVotes(totalNetVotes.add(BigInteger.valueOf(5)));
					continue;
				}
				if (t.getName().equals("nsfw")) {
					t.setNetVotes(totalNetVotes.add(BigInteger.valueOf(4)));
					continue;
				}
				if (DpornConsts.MANDATORY_VIDEO_TAGS.contains(t.getName())) {
					t.setNetVotes(totalNetVotes.add(BigInteger.valueOf(3)));
					continue;
				}
				if (DpornConsts.MANDATORY_PHOTO_GALLERY_TAGS.contains(t.getName())) {
					t.setNetVotes(totalNetVotes.add(BigInteger.valueOf(2)));
					continue;
				}
				if (DpornConsts.MANDATORY_EROTICA_TAGS.contains(t.getName())) {
					t.setNetVotes(totalNetVotes.add(BigInteger.valueOf(1)));
					continue;
				}
			}
			// sort, build simple string list, then sort desc and return list
			Collections.sort(trendingList, (a, b) -> b.getNetVotes().compareTo(a.getNetVotes()));
			List<String> result = new ArrayList<>();
			for (TrendingTag t : trendingList) {
				result.add(t.getName());
			}
			future.complete(result);
		}).exceptionally(ex -> {
			if (ex == null) {
				GWT.log("NULL EXCEPTION: CompletableFuture.allOf(...) in sortTagsByNetVoteDesc");
				return null;
			}
			GWT.log(ex.getMessage(), ex);
			return null;
		});
		return future;
	}

	private String getTimestampedPermlink(String title) {
		if (title == null || title.trim().isEmpty()) {
			title = "dporn";
		}
		DateTimeFormat formatter = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String utcdatetime = formatter.format(new Date(), TimeZone.createTimeZone(0));

		utcdatetime = utcdatetime.toLowerCase();
		utcdatetime = utcdatetime.replaceAll("[^a-z0-9\\-]", "-");
		utcdatetime = utcdatetime.replaceAll("-+", "-");
		utcdatetime = "dporn-" + utcdatetime;

		title = title.toLowerCase();
		title = title.replaceAll("[^a-z0-9\\-]", "-");
		title = title.replaceAll("-+", "-");

		while (utcdatetime.length() + title.length() > 63 && title.length() > 0) {
			title = title.substring(0, title.length() - 1);
		}
		while (title.startsWith("-")) {
			title = title.substring(1);
		}
		while (title.endsWith("-")) {
			title = title.substring(0, title.length() - 1);
		}
		return title + "-" + utcdatetime;
	}

	@Override
	public CompletableFuture<String> postBlogEntry(BlogEntryType blogEntryType, double width, String title,
			List<String> tags, String content) {

		HtmlReformatter reformatter = new HtmlReformatter(width);
		content = "<html>" + reformatter.reformat(content) + "</html>";

		CompletableFuture<String> future = new CompletableFuture<String>();

		String username = appModelCache.getOrDefault(STEEM_USERNAME_KEY, "");
		if (username.trim().isEmpty()) {
			future.completeExceptionally(new IllegalStateException("You must be logged in to post"));
			return future;
		}
		String permlink = getTimestampedPermlink(title);

		JSONArray comment = new JSONArray();
		JSONObject commentData = new JSONObject();
		comment.set(0, new JSONString("comment"));
		comment.set(1, commentData);

		commentData.put("parent_author", new JSONString(""));
		commentData.put("parent_permlink", new JSONString(tags.get(0)));
		commentData.put("author", new JSONString(username));
		commentData.put("permlink", new JSONString(permlink));
		commentData.put("title", new JSONString(title));
		commentData.put("body", new JSONString(content));

		JSONObject commentJsonMetadata = new JSONObject();
		JSONArray jsonTagsArray = new JSONArray();
		for (String tag : tags) {
			jsonTagsArray.set(jsonTagsArray.size(), new JSONString(tag));
		}
		commentJsonMetadata.put("tags", jsonTagsArray);
		// extract image links via JQuery operation on HTML fragment
		JSONArray jsonImageArray = new JSONArray();
		if (content.toLowerCase().contains("<img")) {
			JQueryElement imgs = JQuery.$(content).find("img");
			if (imgs != null) {
				// use non-async code!
				for (int ix = 0; ix < imgs.length(); ix++) {
					HTMLImageElement img = Js.cast(imgs.get(ix));
					jsonImageArray.set(jsonImageArray.size(), new JSONString(img.src));
				}
			}
		}
		JSONArray linksArray = new JSONArray();
		if (content.toLowerCase().contains("<a")) {
			JQueryElement anchors = JQuery.$(content).find("a");
			if (anchors != null) {
				// use non-async code!
				for (int ix = 0; ix < anchors.length(); ix++) {
					HTMLAnchorElement anchor = Js.cast(anchors.get(ix));
					String href = anchor.href;
					if (href != null && !href.isEmpty() && !href.startsWith("#")) {
						jsonImageArray.set(linksArray.size(), new JSONString(href));
					}
				}
			}
		}
		JSONArray userArray = new JSONArray();
		if (content.contains("@")) {
			Set<String> usernames = new TreeSet<>();
			JQueryElement textNodes = JQuery.$(content).contents().filter((i, e) -> e.getNodeType() == Node.TEXT_NODE);
			for (int ix = 0; ix < textNodes.length(); ix++) {
				String text = textNodes.get(ix).getInnerText();
				if (text == null) {
					continue;
				}
				if (!text.contains("@")) {
					continue;
				}
				text = text.toLowerCase();
				if (!text.matches(".*?" + USERNAME_PATTERN + ".*?")) {
					continue;
				}
				text = text.replaceAll(USERNAME_PATTERN, "\n$1\n");
				String[] tmp = text.split("\n");
				if (tmp == null) {
					continue;
				}
				for (String user: tmp) {
					if (!user.startsWith("@")) {
						continue;
					}
					if (!user.matches("^"+USERNAME_PATTERN+"$")) {
						continue;
					}
					usernames.add(user);
				}
			}
			for (String user: usernames) {
				userArray.set(userArray.size(), new JSONString(user.substring(1)));
			}
		}
		commentJsonMetadata.put("app", new JSONString(DpornConsts.APP_ID_VERSION));
		commentJsonMetadata.put("image", jsonImageArray);
		commentJsonMetadata.put("community", new JSONString("dporn"));
		commentJsonMetadata.put("format", new JSONString("text/html"));
		commentJsonMetadata.put("users", userArray);
		commentJsonMetadata.put("links", linksArray);
		commentJsonMetadata.put("canonical", new JSONString("https://dporn.co" + Routes.post(username, permlink)));

		JSONObject dpornMetadata = new JSONObject();
		dpornMetadata.put("app", new JSONString(DpornConsts.APP_ID_VERSION));
		dpornMetadata.put("embed", new JSONString(Routes.embedVideo(username, permlink)));
		dpornMetadata.put("blogEntryType", new JSONString(blogEntryType.name()));
		dpornMetadata.put("videoPath", null);
		dpornMetadata.put("posterImagePath", null);
		dpornMetadata.put("photoGalleryImagePaths", new JSONArray());

		commentJsonMetadata.put("dpornMetadata", dpornMetadata);

		commentData.put("json_metadata", new JSONString(commentJsonMetadata.toString()));

		JSONArray commentOptions = new JSONArray();
		JSONObject optionData = new JSONObject();
		commentOptions.set(0, new JSONString("comment_options"));
		commentOptions.set(1, optionData);

		optionData.put("author", new JSONString(username));
		optionData.put("permlink", new JSONString(permlink));
		optionData.put("max_accepted_payout", new JSONString("1000000.000 SBD"));
		optionData.put("percent_steem_dollars", new JSONNumber(10000));
		optionData.put("allow_votes", JSONBoolean.getInstance(true));
		optionData.put("allow_curation_rewards", JSONBoolean.getInstance(true));

		JSONArray extensionsList = new JSONArray();
		optionData.put("extensions", extensionsList);
		JSONArray extensionBeneficiaries = new JSONArray();
		extensionsList.set(0, extensionBeneficiaries);

		JSONObject beneficiariesData = new JSONObject();
		// TODO: support user specified post splits
		JSONArray beneficiaryList = new JSONArray();
		for (Beneficiary ben : DpornConsts.BENEFICIARIES) {
			JSONObject beneficiary = new JSONObject();
			beneficiary.put("account", new JSONString(ben.getAccount()));
			beneficiary.put("weight", new JSONNumber(ben.getWeight()));
			beneficiaryList.set(beneficiaryList.size(), beneficiary);
		}
		beneficiariesData.put("beneficiaries", beneficiaryList);

		extensionBeneficiaries.set(0, new JSONNumber(0));
		extensionBeneficiaries.set(1, beneficiariesData);

		sc2api.broadcast(comment, commentOptions).thenAccept(result -> {
			String pnewToken = "@" + username + "/" + permlink;
			String authorization = appModelCache.getOrDefault(STEEMCONNECT_KEY, "");
			ClientRestClient.get().commentConfirm(username, authorization, permlink).thenRun(() -> {
				PushStateHistorian.replaceItem(pnewToken, true);
			}).exceptionally(ex -> {
				PushStateHistorian.replaceItem(pnewToken, true);
				return null;
			});
			future.complete(pnewToken);
		}).exceptionally(ex -> {
			if (ex.getCause() != null) {
				DomGlobal.console.log(ex.getCause());
				MaterialToast.fireToast(ex.getCause().getMessage(), 15000);
			} else {
				DomGlobal.console.log(ex);
			}
			return null;
		});

		return future;
	}

}
