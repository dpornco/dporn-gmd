package steem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import steem.model.Vote;

@JsType(namespace = "steem", name = "api", isNative = true)
public class SteemApi {

	@JsMethod(name = "getActiveVotes")
	private static native void _getActiveVotes(String username, String permlink,
			SteemJsCallback jscb);

	@JsOverlay
	public static CompletableFuture<List<Vote>> getActiveVotes(String username, String permlink) {
		GWT.log("SteemApi#getActiveVotes");
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
	public static interface VoteMapper extends ObjectMapper<Vote>{}
	public static interface VoteListMapper extends ObjectMapper<List<Vote>>{};

	public static class SteemCallbackAsFuture<T> {
		private CompletableFuture<T> future = new CompletableFuture<>();

		public CompletableFuture<T> getFuture() {
			return future;
		}
	}
}