package steem;

import java.util.concurrent.CompletableFuture;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;

import jsinterop.annotations.JsFunction;

public class JsCallbackFuture {
	@FunctionalInterface
	@JsFunction
	private static interface JsBiCallback {
		void onResult(JavaScriptObject error, JavaScriptObject result);
	}

	private final CompletableFuture<JSONObject> future;
	private final JsBiCallback callback;

	public JsCallbackFuture() {
		future = new CompletableFuture<>();
		callback = new JsBiCallback() {
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

	public JsBiCallback getCallback() {
		return callback;
	}

	public CompletableFuture<JSONObject> getFuture() {
		return future;
	}
}
