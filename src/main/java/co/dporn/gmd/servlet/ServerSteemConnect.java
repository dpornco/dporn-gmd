package co.dporn.gmd.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServerSteemConnect {
	private static final String endPoint = "https://steemconnect.com/api/";

	public static String username(String authorization) {
		Map<String, String> queryParams = new LinkedHashMap<>();
		queryParams.put("access_token", authorization);
		String result = ServerRestClient.get(endPoint + "me", queryParams);
		MeAuthorizationCheck me;
		try {
			me = Mapper.get().readValue(result, MeAuthorizationCheck.class);
		} catch (IOException e) {
			return "";
		}
		return me.getUser();
	}
}
