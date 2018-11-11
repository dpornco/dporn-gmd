package steem.connect;

import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class SteemConnectInit extends JSONObject {
	public SteemConnectInit() {
		setAccessToken("access_token");
	}
	public void setApp(String app) {
		this.put("app", new JSONString(app));
	}
	public void setCallbackUrl(String callbackUrl) {
		this.put("callbackURL", new JSONString(callbackUrl));
	}
	public void setAccessToken(String accessToken) {
		this.put("accessToken", new JSONString(accessToken));
	}
	public void setScopes(List<String> scopes) {
		setScopes(scopes.toArray(new String[0]));
	}
	public void setScopes(String... scopes) {
		JSONArray jscopes = new JSONArray();
		int ix=0;
		for (String scope: scopes) {
			jscopes.set(ix++, new JSONString(scope));
		}
		this.put("scope", jscopes);
	}
}
