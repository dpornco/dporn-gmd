package steem;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsFunction;

@FunctionalInterface
@JsFunction
public interface SteemJsonObjectCallbackFuture {
	void onResult(JavaScriptObject error, JavaScriptObject result);
}