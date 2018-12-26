package co.dporn.gmd.servlet.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.PassiveExpiringMap;

public class Notifications {
	private static final int MINUTE_ms = 1000*60;
	private static Map<String, List<String>> notifications = new PassiveExpiringMap<>(5*MINUTE_ms, new LRUMap<>(256));
	
	public static int getPendingNotificationCount(String username) {
		synchronized (notifications) {
			List<String> notices = notifications.get(username);
			if (notices==null) {
				return 0;
			}
			notifications.put(username, notices);
			return notices.size();
		}
	}
	
	public static void notify(String username, String notice) {
		synchronized (notifications) {
			List<String> notices = notifications.get(username);
			if (notices==null) {
				notices=new ArrayList<>();
			}
			notices.add(notice);
			notifications.put(username, notices);
		}
	}
	
	public static List<String> getNotifications(String username) {
		synchronized (notifications) {
			List<String> notices = notifications.remove(username);
			if (notices==null) {
				return new ArrayList<>();
			}
			return notices;
		}
	}
}
