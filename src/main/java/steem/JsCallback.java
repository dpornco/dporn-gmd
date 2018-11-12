package steem;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsFunction;

@FunctionalInterface
@JsFunction
public interface JsCallback {
	void onResult(JavaScriptObject error, JavaScriptObject result);
}