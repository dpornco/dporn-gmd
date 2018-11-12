package steem;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;

@FunctionalInterface
public interface SteemJsonCallback {
	default void onResult(JavaScriptObject error, JavaScriptObject result) {
		jsonResult(error == null ? null : JsonUtils.stringify(error), //
				result == null ? null : JsonUtils.stringify(result));
	}
	default JsCallback call() {
		return new JsCallback() {
			@Override
			public void onResult(JavaScriptObject error, JavaScriptObject result) {
				SteemJsonCallback.this.onResult(error, result);
			}
		};
	}
	void jsonResult(String error, String result);
	
}
