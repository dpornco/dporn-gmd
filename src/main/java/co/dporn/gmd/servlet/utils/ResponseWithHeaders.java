package co.dporn.gmd.servlet.utils;

import java.util.List;
import java.util.Map;

import com.google.gwt.dev.util.collect.HashMap;

public class ResponseWithHeaders {
	private String body;
	private final Map<String, List<String>> headers = new HashMap<>();
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
}