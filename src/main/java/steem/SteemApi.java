package steem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import steem.model.DiscussionComment;
import steem.model.Vote;

@JsType(namespace = "steem", name = "api", isNative = true)
public class SteemApi {

	@JsMethod(name = "getContent")
	private static native void _getContent(String author, String permlink, JsCallback jscb);
	public static interface DiscussionCommentMapper extends ObjectMapper<DiscussionComment>{}
	@JsOverlay
	public static CompletableFuture<DiscussionComment> getContent(String username, String permlink) {
		GWT.log("SteemApi#getContent: @"+username+"/"+permlink);
		SteemCallbackAsFuture<DiscussionComment> future = new SteemCallbackAsFuture<>(); 
		SteemTypedCallback<DiscussionComment, ObjectMapper<DiscussionComment>> typed=new SteemTypedCallback<DiscussionComment, ObjectMapper<DiscussionComment>>() {
			@Override
			public void onResult(String error, DiscussionComment result) {
				if (error!=null && !error.trim().isEmpty()) {
					future.getFuture().completeExceptionally(new RuntimeException(error));
					return;
				}
				future.getFuture().complete(result);
			}

			@Override
			public ObjectMapper<DiscussionComment> mapper() {
				return GWT.create(DiscussionCommentMapper.class);
			}
		};
		_getContent(username, permlink, typed.call());
		return future.getFuture();
	}

	@JsMethod(name = "getActiveVotes")
	private static native void _getActiveVotes(String username, String permlink,
			JsCallback jscb);
	public static interface VoteMapper extends ObjectMapper<Vote>{}
	public static interface VoteListMapper extends ObjectMapper<List<Vote>>{};
	@JsOverlay
	public static CompletableFuture<List<Vote>> getActiveVotes(String username, String permlink) {
		GWT.log("SteemApi#getActiveVotes: @"+username+"/"+permlink);
		SteemCallbackAsFuture<List<Vote>> future = new SteemCallbackAsFuture<>(); 
		SteemTypedCallback<List<Vote>, ObjectMapper<List<Vote>>> typed=new SteemTypedCallback<List<Vote>, ObjectMapper<List<Vote>>>() {
			@Override
			public void onResult(String error, List<Vote> result) {
				if (error!=null && !error.trim().isEmpty()) {
					future.getFuture().completeExceptionally(new RuntimeException(error));
					return;
				}
				future.getFuture().complete(result);
			}

			@Override
			public ObjectMapper<List<Vote>> mapper() {
				return GWT.create(VoteListMapper.class);
			}
		};
		_getActiveVotes(username, permlink, typed.call());
		return future.getFuture();
	}

	public static class SteemCallbackAsFuture<T> {
		private CompletableFuture<T> future = new CompletableFuture<>();

		public CompletableFuture<T> getFuture() {
			return future;
		}
	}
}