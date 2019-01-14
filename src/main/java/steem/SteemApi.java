package steem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import steem.model.DiscussionComment;
import steem.model.TrendingTag;
import steem.model.Vote;

@JsType(namespace = "steem", name = "api", isNative = true)
public class SteemApi {

	@JsMethod(name = "getTrendingTags")
	private static native void _getTrendingTags(String afterTags, int limit, JsCallback jscb);
	
	public static interface ListTrendingTagMapper extends ObjectMapper<List<TrendingTag>> {
	}
	
	@JsOverlay
	public static CompletableFuture<List<TrendingTag>> getTrendingTags(int limit) {
		return getTrendingTags("", limit);
	}
	
	@JsOverlay
	public static CompletableFuture<List<TrendingTag>> getTrendingTags(String afterTag, int limit) {
		SteemCallbackAsFuture<List<TrendingTag>> future = new SteemCallbackAsFuture<>();
		SteemTypedCallback<List<TrendingTag>, ObjectMapper<List<TrendingTag>>> typed = new SteemTypedCallback<List<TrendingTag>, ObjectMapper<List<TrendingTag>>>() {
			@Override
			public void onResult(String error, List<TrendingTag> result) {
				if (error != null && !error.trim().isEmpty()) {
					future.getFuture().completeExceptionally(new RuntimeException(error));
					return;
				}
				future.getFuture().complete(result);
			}

			@Override
			public ObjectMapper<List<TrendingTag>> mapper() {
				return GWT.create(ListTrendingTagMapper.class);
			}
		};
		_getTrendingTags(afterTag, limit, typed.call());
		return future.getFuture();
	}

	/**
	 * steem api only responds with an empty array
	 * 
	 * @param author
	 * @param jscb
	 */
	@JsMethod(name = "getTagsUsedByAuthor")
	private static native void _getTagsUsedByAuthor(String author, JsCallback jscb);

	@JsMethod(name = "getContent")
	private static native void _getContent(String author, String permlink, JsCallback jscb);

	public static interface DiscussionCommentMapper extends ObjectMapper<DiscussionComment> {
	}

	@JsOverlay
	public static CompletableFuture<DiscussionComment> getContent(String username, String permlink) {
		SteemCallbackAsFuture<DiscussionComment> future = new SteemCallbackAsFuture<>();
		SteemTypedCallback<DiscussionComment, ObjectMapper<DiscussionComment>> typed = new SteemTypedCallback<DiscussionComment, ObjectMapper<DiscussionComment>>() {
			@Override
			public void onResult(String error, DiscussionComment result) {
				if (error != null && !error.trim().isEmpty()) {
					future.getFuture().completeExceptionally(new RuntimeException(error));
					DomGlobal.console.log("Steem API Error: @"+username+"/"+permlink);
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

	@JsMethod(name = "getDiscussionsByBlog")
	private static native void _getDiscussionsByBlog(JavaScriptObject query, JsCallback jscb);

	public static interface ListDiscussionCommentMapper extends ObjectMapper<List<DiscussionComment>> {
	}

	@JsOverlay
	public static CompletableFuture<List<DiscussionComment>> getDiscussionsByBlog(String username) {
		return getDiscussionsByBlog(username, 30);
	}
	
	@JsOverlay
	public static CompletableFuture<List<DiscussionComment>> getDiscussionsByBlog(String username, int limit) {
		if (limit < 1) {
			limit = 1;
		}
		SteemCallbackAsFuture<List<DiscussionComment>> future = new SteemCallbackAsFuture<>();
		SteemTypedCallback<List<DiscussionComment>, ObjectMapper<List<DiscussionComment>>> typed = new SteemTypedCallback<List<DiscussionComment>, ObjectMapper<List<DiscussionComment>>>() {
			@Override
			public void onResult(String error, List<DiscussionComment> result) {
				if (error != null && !error.trim().isEmpty()) {
					GWT.log("SteemApi#getDiscussionsByBlog: "+error);
					future.getFuture().completeExceptionally(new RuntimeException(error));
					return;
				}
				future.getFuture().complete(result);
			}

			@Override
			public ObjectMapper<List<DiscussionComment>> mapper() {
				return GWT.create(ListDiscussionCommentMapper.class);
			}
		};
		JSONObject query = new JSONObject();
		query.put("tag", new JSONString(username));
		query.put("limit", new JSONNumber(limit));
		_getDiscussionsByBlog(query.getJavaScriptObject(), typed.call());
		return future.getFuture();
	}

	@JsMethod(name = "getActiveVotes")
	private static native void _getActiveVotes(String username, String permlink, JsCallback jscb);

	public static interface VoteMapper extends ObjectMapper<Vote> {
	}

	public static interface VoteListMapper extends ObjectMapper<List<Vote>> {
	};

	@JsOverlay
	public static CompletableFuture<List<Vote>> getActiveVotes(String username, String permlink) {
		SteemCallbackAsFuture<List<Vote>> future = new SteemCallbackAsFuture<>();
		SteemTypedCallback<List<Vote>, ObjectMapper<List<Vote>>> typed = new SteemTypedCallback<List<Vote>, ObjectMapper<List<Vote>>>() {
			@Override
			public void onResult(String error, List<Vote> result) {
				if (error != null && !error.trim().isEmpty()) {
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