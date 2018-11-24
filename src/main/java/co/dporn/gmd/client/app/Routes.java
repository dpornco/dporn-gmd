package co.dporn.gmd.client.app;

import java.util.Collection;
import java.util.Iterator;

public class Routes {
	protected Routes() {
	}

	public static String avatarImageNotLoggedIn() {
		return avatarImage("null");
	}
	public static String avatarImage(String username) {
		return "https://steemitimages.com/u/" + username + "/avatar/medium";
	}

	public static String embedVideo(String username, String permlink) {
		return "/embed/@" + username + "/" + permlink;
	}

	public static String channel(String username) {
		return "/@" + username;
	}

	public static String post(String username, String permlink) {
		return "/@" + username + "/" + permlink;
	}

	public static String searchTags(Collection<String> includeTags, Collection<String> excludeTags) {
		StringBuilder sb = new StringBuilder();
		if (includeTags != null) {
			Iterator<String> iter = includeTags.iterator();
			while (iter.hasNext()) {
				sb.append(iter.next());
				if (iter.hasNext()) {
					sb.append(" ");
				}
			}
		}
		if (excludeTags != null) {
			Iterator<String> iter = excludeTags.iterator();
			if (iter.hasNext() && sb.length() > 0) {
				sb.append("/");
			}
			while (iter.hasNext()) {
				sb.append("-");
				sb.append(iter.next());
				if (iter.hasNext()) {
					sb.append(" ");
				}
			}
		}
		return search() + "#" + sb.toString();
	}

	public static String search() {
		return "/search";
	}

	public static String uploadPhotogallery() {
		return "/upload/photos";
	}

	public static String uploadVideo() {
		return "/upload/video";
	}

	public static String verifiedChannels() {
		return "/verified";
	}

	public static String settings() {
		return "/settings";
	}

	public static String busyorg(String username) {
		if (username==null || username.trim().isEmpty()) {
			return "https://busy.org/@dpornco";
		}
		return "https://busy.org/@"+username.trim();
	}
	
	public static String steemit(String username) {
		if (username==null || username.trim().isEmpty()) {
			return "https://steemit.com/@dpornco";
		}
		return "https://steemit.com/@"+username.trim();
	}
}
