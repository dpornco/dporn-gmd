package steem.connect;

import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;

import jsinterop.annotations.JsFunction;

public class Sc2CallbackFuture {
	@FunctionalInterface
	@JsFunction
	public static interface Sc2BiCallback {
		void onResult(JavaScriptObject error, JavaScriptObject result);
	}

	private final CompletableFuture<JSONObject> future;
	private final Sc2BiCallback callback;

	public Sc2CallbackFuture() {
		future = new CompletableFuture<>();
		callback = new Sc2BiCallback() {
			@Override
			public void onResult(JavaScriptObject error, JavaScriptObject result) {
				if (error != null) {
					future.completeExceptionally(new RuntimeException(JsonUtils.stringify(error)));
					return;
				}
				try {
					future.complete(new JSONObject(result));
				} catch (Exception e) {
					future.completeExceptionally(e);
				}
			}
		};
	}

	public Sc2BiCallback getCallback() {
		return callback;
	}

	public CompletableFuture<JSONObject> getFuture() {
		return future;
	}
}
