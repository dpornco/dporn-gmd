package steem.connect;

import java.util.concurrent.CompletableFuture;

import com.google.gwt.json.client.JSONObject;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import steem.connect.Sc2CallbackFuture.Sc2BiCallback;

@JsType(namespace = JsPackage.GLOBAL, name = "?", isNative = true)
public class SteemConnectV2Api {
	public native String getLoginURL(String state);

	@JsOverlay
	public CompletableFuture<JSONObject> me() {
		Sc2CallbackFuture future = new Sc2CallbackFuture();
		_me(future.getCallback());
		return future.getFuture();
	}

	@JsMethod(name = "me")
	private native void _me(Sc2BiCallback callback);

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
			String jsonMetadata, Sc2BiCallback callback);

	@JsMethod(name = "deleteComment")
	private native void _deleteComment(String author, String permlink, Sc2BiCallback callback);

	@JsMethod
	public native String sign(HotSigningLinkInit transferInit, String redirectUri);

	@JsMethod(name = "revokeToken")
	private native void _revokeToken(Sc2BiCallback callback);

	@JsMethod(name = "reblog")
	private native void _reblog(String account, String author, String permlink, Sc2BiCallback callback);

	@JsMethod(name = "follow")
	private native void _follow(String follower, String following, Sc2BiCallback callback);

	@JsMethod(name = "unfollow")
	private native void _unfollow(String follower, String following, Sc2BiCallback callback);

	@JsMethod(name = "ignore")
	private native void _ignore(String follower, String following, Sc2BiCallback callback);

	@JsMethod(name = "claimRewardBalance")
	private native void _claimRewardBalance(String account, String rewardSteem, String rewardSbd, String rewardVests,
			Sc2BiCallback callback);

	@JsMethod(name = "updateUserMetadata")
	private native void _updateUserMetadata(JSONObject metadata, Sc2BiCallback callback);

}