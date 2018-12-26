package co.dporn.gmd.servlet.utils.steemj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import co.dporn.gmd.servlet.utils.Mapper;
import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.AccountMetadata;
import co.dporn.gmd.shared.AccountMetadata.AccountProfile;
import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;
import eu.bittrade.libs.steemj.apis.follow.enums.FollowType;
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry;
import eu.bittrade.libs.steemj.apis.follow.model.FollowApiObject;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;
import eu.bittrade.libs.steemj.base.models.Permlink;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;

public class SteemJInstance {
	private static final AccountName ACCOUNT_DPORNCO = new AccountName("dpornco");
	private static final AccountName ACCOUNT_DPORN = new AccountName("dporn");
	private static final long MINUTE_ms = 1000l * 60;

	private static final String VERIFIED_FULL_LIST_KEY = "dporn-verified-list";
	private static final String VERIFIED_ACTIVE_LIST_KEY = "dporn-active-verified-list";
	private static final String BLACKLIST_KEY = "dporn-blacklist";
	private static SteemJInstance _instance;

	private final Map<String, List<String>> cachedStringLists;
	private final Map<String, Map<String, AccountInfo>> cachedBlogDetailMaps;

	protected SteemJInstance() {
		try {
			_steemJ = new SteemJ();
		} catch (SteemCommunicationException | SteemResponseException e) {
			_steemJ = null;
		}
		cachedStringLists = Collections.synchronizedMap(new PassiveExpiringMap<>(30*MINUTE_ms, new LRUMap<>(16)));
		cachedBlogDetailMaps = Collections.synchronizedMap(new PassiveExpiringMap<>(10*MINUTE_ms, new LRUMap<>(16)));
	}
	
	public Discussion getContent(String username, String permlink) {
		for (int retries=0; retries<3; retries++) {
			try {
				return _steemJ.getContent(new AccountName(username), new Permlink(permlink));
			} catch (SteemCommunicationException | SteemResponseException e) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e1) {
				}
			}
		}
		return null;
	}

	public static SteemJInstance get() {
		if (_instance == null) {
			_instance = new SteemJInstance();
		}
		return _instance;
	}

	private SteemJ _steemJ;

	private SteemJ steemJ() {
		return _steemJ;
	}

	public synchronized Map<String, AccountInfo> getBlogDetails(Collection<String> active) {
		List<AccountName> activeAccounts = new ArrayList<>();
		TreeSet<String> sortedActive = new TreeSet<>(active);
		Map<String, AccountInfo> cached = cachedBlogDetailMaps.get(sortedActive.toString());
		if (cached != null) {
			System.out.println("getBlogDetail[multi]:cached");
			return new HashMap<>(cached);
		}
		for (String a : sortedActive) {
			activeAccounts.add(new AccountName(a));
		}
		Map<String, AccountInfo> map = new HashMap<>();
		try {
			for (ExtendedAccount extendedInfo : steemJ().getAccounts(activeAccounts)) {
				AccountInfo info = new AccountInfo();
				info.setCreated(extendedInfo.getCreated().getDateTimeAsDate());
				String jsonMetadata = extendedInfo.getJsonMetadata();
				try {
					AccountMetadata meta = Mapper.get().readValue(jsonMetadata, AccountMetadata.class);
					if (meta != null) {
						AccountProfile profile = meta.getProfile();
						if (profile != null) {
							info.setAbout(profile.getAbout());
							info.setCoverImage(profile.getCoverImage());
							info.setLocation(profile.getLocation());
							info.setDisplayName(profile.getName());
							info.setProfileImage(profile.getProfileImage());
							info.setWebsite(profile.getWebsite());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				info.setLastRootPost(extendedInfo.getLastRootPost().getDateTimeAsDate());
				map.put(extendedInfo.getName().getName(), info);
			}
		} catch (SteemCommunicationException | SteemResponseException e) {
			return new HashMap<>(map);
		}
		cachedBlogDetailMaps.put(sortedActive.toString(), map);
		return new HashMap<>(map);
	}

	public synchronized List<String> getActiveDpornVerifiedList() {
		List<String> cached = cachedStringLists.get(VERIFIED_ACTIVE_LIST_KEY);
		if (cached != null) {
			System.out.println("getActiveDpornVerifiedList:cached");
			return new ArrayList<>(cached);
		}
		List<String> active = new ArrayList<>();
		Set<String> verified = new HashSet<>(getDpornVerifiedSet());
		Set<String> already = new HashSet<>();
		List<BlogEntry> entries;
		try {
			entries = steemJ().getBlogEntries(ACCOUNT_DPORN, 0, (short) 100);
		} catch (SteemCommunicationException | SteemResponseException e) {
			return new ArrayList<>(verified);
		}
		for (BlogEntry entry : entries) {
			if (entry.getAuthor().equals(ACCOUNT_DPORNCO)) {
				continue;
			}
			String name = entry.getAuthor().getName();
			if (!verified.contains(name)) {
				continue;
			}
			if (already.contains(name)) {
				continue;
			}
			active.add(name);
			already.add(name);
		}
		cachedStringLists.put(VERIFIED_ACTIVE_LIST_KEY, active);
		return new ArrayList<>(active);
	}

	public synchronized Set<String> getDpornVerifiedSet() {
		List<String> cached = cachedStringLists.get(VERIFIED_FULL_LIST_KEY);
		if (cached != null) {
			System.out.println("getDpornVerifiedSet:cached");
			return new TreeSet<>(cached);
		}
		Set<String> set = new TreeSet<>();
		List<FollowApiObject> following;
		AccountName startAccount = new AccountName("");
		do {
			try {
				following = steemJ().getFollowing(new AccountName("verifiednsfw"), startAccount, FollowType.BLOG,
						(short) 10);
			} catch (SteemCommunicationException | SteemResponseException e) {
				following = new ArrayList<>();
			}
			for (FollowApiObject followed : following) {
				set.add(followed.getFollowing().getName());
			}
			try {
				following = steemJ().getFollowing(new AccountName("dporn"), startAccount, FollowType.BLOG, (short) 10);
			} catch (SteemCommunicationException | SteemResponseException e) {
				following = new ArrayList<>();
			}
			for (FollowApiObject followed : following) {
				set.add(followed.getFollowing().getName());
			}
			if (!following.isEmpty()) {
				startAccount = following.get(following.size() - 1).getFollowing();
			}
		} while (following.size() > 1);
		cachedStringLists.put(VERIFIED_FULL_LIST_KEY, new ArrayList<>(set));
		return new TreeSet<>(set);
	}

	public synchronized Set<String> getBlacklist() {
		List<String> cached = cachedStringLists.get(BLACKLIST_KEY);
		if (cached != null) {
			return new TreeSet<>(cached);
		}
		Set<String> set = new TreeSet<>();
		List<FollowApiObject> following;
		AccountName startAccount = new AccountName("");
		do {
			try {
				following = steemJ().getFollowing(new AccountName("verifiednsfw"), startAccount, FollowType.IGNORE,
						(short) 10);
			} catch (SteemCommunicationException | SteemResponseException e) {
				following = new ArrayList<>();
			}
			for (FollowApiObject followed : following) {
				set.add(followed.getFollowing().getName());
			}
			try {
				following = steemJ().getFollowing(new AccountName("dporn"), startAccount, FollowType.IGNORE,
						(short) 10);
			} catch (SteemCommunicationException | SteemResponseException e) {
				following = new ArrayList<>();
			}
			for (FollowApiObject followed : following) {
				set.add(followed.getFollowing().getName());
			}
			if (!following.isEmpty()) {
				startAccount = following.get(following.size() - 1).getFollowing();
			}
		} while (following.size() > 1);
		cachedStringLists.put(BLACKLIST_KEY, new ArrayList<>(set));
		return new TreeSet<>(set);
	}
}
