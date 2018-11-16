package co.dporn.gmd.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.PassiveExpiringMap;

public class ServerSteemConnect {
	private static final long MINUTE_ms = 1000l * 60;
	private static final String STEEMCONNECT_API = "https://steemconnect.com/api/";
	private static final Map<String, String> USERNAME_CACHE = Collections.synchronizedMap(new PassiveExpiringMap<>(30*MINUTE_ms, new LRUMap<>(256)));
	public static String username(String authorization) {
		String cached = USERNAME_CACHE.get(authorization);
		if (cached!=null) {
			return cached;
		}
		Map<String, String> queryParams = new LinkedHashMap<>();
		queryParams.put("access_token", authorization);
		String result = ServerRestClient.get(STEEMCONNECT_API + "me", queryParams);
		MeAuthorizationCheck me;
		try {
			me = Mapper.get().readValue(result, MeAuthorizationCheck.class);
		} catch (IOException e) {
			return "";
		}
		String user = me.getUser();
		USERNAME_CACHE.put(authorization, user);
		return user;
	}
}
