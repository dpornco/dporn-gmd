package steem.connect;

import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import steem.connect.Sc2CallbackFuture.Sc2BiCallback;

@JsType(namespace = JsPackage.GLOBAL, name = "sc2", isNative = true)
public class SteemConnectV2 {
	@JsOverlay
	public static final SteemConnectV2 initialize(SteemConnectInit initializeParam) {
		return _initialize(initializeParam.getJavaScriptObject());
	}
	@JsMethod(name = "Initialize")
	private static native SteemConnectV2 _initialize(JavaScriptObject initializeParam);
	

	@JsType(isNative = true)
	private static interface SteemConnectV2Options {

		void setBaseURL(String baseURL);

		void setApp(String app);

		void setCallbackURL(String callbackURL);

		void setAccessToken(String accessToken);

		void removeAccessToken(String accessToken);

		void setScope(String[] scope);

		@JsOverlay
		default void setScope(String scope) {
			setScope(new String[] { scope });
		}
	}

	@JsMethod
	private native SteemConnectV2Options getOptions();

	@JsMethod
	public native void setBaseURL(String baseURL);

	@JsMethod
	public native void setApp(String app);

	@JsMethod
	public native void setCallbackURL(String callbackURL);

	@JsMethod
	public native void setAccessToken(String accessToken);

	@JsMethod
	public native void removeAccessToken();

	@JsMethod
	public native void setScope(String[] scopes);

	@JsOverlay
	public final void setScope(String scope) {
		setScope(new String[] { scope });
	}

	@JsMethod
	public native String getLoginURL(String state);

	@JsMethod(name = "send")
	private native void _send(String route, String method, JSONObject body, Sc2BiCallback callback);

	@JsMethod(name = "broadcast")
	private native void _broadcast(JavaScriptObject javaScriptObject, Sc2BiCallback callback);

	@JsOverlay
	public final CompletableFuture<JSONObject> broadcast(JSONArray... operations) {
		Sc2CallbackFuture future = new Sc2CallbackFuture();
		if (operations==null || operations.length==0) {
			future.getFuture().completeExceptionally(new IllegalStateException("No broadcast operations supplied"));
			return future.getFuture();
		}
		JSONArray tmp = new JSONArray();
		for (int ix=0; ix<operations.length; ix++) {
			tmp.set(ix, operations[ix]);
		}
		JavaScriptObject javaScriptObject = tmp.getJavaScriptObject();
		DomGlobal.console.log(javaScriptObject);
		_broadcast(javaScriptObject, future.getCallback());
		return future.getFuture();
	}

	@JsMethod(name = "me")
	private native void _me(Sc2BiCallback callback);

	@JsOverlay
	public final CompletableFuture<JSONObject> me() {
		Sc2CallbackFuture future = new Sc2CallbackFuture();
		_me(future.getCallback());
		return future.getFuture();
	}

	/**
	 * @param voter
	 *            The Steem username of the current user.
	 * @param author
	 *            The Steem username of the author of the post or comment.
	 * @param permlink
	 *            The link to the post or comment on which to vote. This is the
	 *            portion of the URL after the last "/". For example the "permlink"
	 *            for this post:
	 *            https://steemit.com/steem/@ned/announcing-smart-media-tokens-smts
	 *            would be "announcing-smart-media-tokens-smts".
	 * @param weight
	 *            The weight of the vote. 10000 equale a 100% vote.
	 * @param callback
	 *            function that is called once the vote is submitted and included in
	 *            a block. If successful the "res" variable will be a JSON object
	 *            containing the details of the block and the vote operation.
	 */
	@JsMethod(name = "vote")
	private native void _vote(String voter, String author, String permlink, int weight, Sc2BiCallback callback);

	@JsMethod(name = "comment")
	private native void _comment(String parentAuthor, String parentPermlink, String author, String title, String body,
			JSONObject jsonMetadata, Sc2BiCallback callback);

	@JsMethod(name = "deleteComment")
	private native void _deleteComment(String author, String permlink, Sc2BiCallback callback);

	@JsMethod(name = "reblog")
	private native void _reblog(String account, String author, String permlink, Sc2BiCallback callback);

	@JsMethod(name = "follow")
	private native void _follow(String follower, String following, Sc2BiCallback callback);

	@JsMethod(name = "unfollow")
	private native void _unfollow(String follower, String following, Sc2BiCallback callback);

	@JsMethod(name = "ignore")
	private native void _ignore(String follower, String following, Sc2BiCallback callback);

	@JsMethod(name = "revokeToken")
	private native void _revokeToken(Sc2BiCallback callback);
	@JsOverlay
	public final CompletableFuture<JSONObject> revokeToken() {
		Sc2CallbackFuture sc2CallbackFuture = new Sc2CallbackFuture();
		_revokeToken(sc2CallbackFuture.getCallback());
		return sc2CallbackFuture.getFuture();
	}

	@JsMethod(name = "updateUserMetadata")
	private native void _updateUserMetadata(JSONObject metadata, Sc2BiCallback callback);

	@JsMethod
	public native String sign(String name, JSONObject transferInit, String redirectUri);

	@JsMethod(name = "claimRewardBalance")
	private native void _claimRewardBalance(String account, String rewardSteem, String rewardSbd, String rewardVests,
			Sc2BiCallback callback);

	
}