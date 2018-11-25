package co.dporn.gmd.servlet.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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