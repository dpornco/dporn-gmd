package steem;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;

public interface SteemTypedCallback<T, U extends ObjectMapper<T>> {
	default void onResult(JavaScriptObject error, JavaScriptObject result) {
		String jsonError = error == null ? null : JsonUtils.stringify(error);
		T jsonResult;
		try {
			jsonResult = result == null ? null : mapper().read(JsonUtils.stringify(result));
		} catch (JsonDeserializationException e) {
			GWT.log(e.getMessage(), e);
			GWT.log(JsonUtils.stringify(result, "\t"));
			jsonResult = null;
		}
		onResult(jsonError, jsonResult);
	}

	default JsCallback call() {
		return new JsCallback() {
			@Override
			public void onResult(JavaScriptObject error, JavaScriptObject result) {
				SteemTypedCallback.this.onResult(error, result);
			}
		};
	}

	void onResult(String error, T result);

	U mapper();
}
