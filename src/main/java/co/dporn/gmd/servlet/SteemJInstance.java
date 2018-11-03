package co.dporn.gmd.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import co.dporn.gmd.shared.AccountInfo;
import co.dporn.gmd.shared.AccountMetadata;
import co.dporn.gmd.shared.AccountMetadata.AccountProfile;
import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.apis.follow.enums.FollowType;
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry;
import eu.bittrade.libs.steemj.apis.follow.model.FollowApiObject;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;

public class SteemJInstance {
	private static final AccountName ACCOUNT_DPORNCO = new AccountName("dpornco");
	private static final AccountName ACCOUNT_DPORN = new AccountName("dporn");
	private static final long _30_MINUTES = 1000l*60*30l;

	protected SteemJInstance() {
	}

	private static SteemJ _steemJ;
	private static List<String> cachedActiveNsfwVerifiedList=new ArrayList<>();
	private static long cachedActiveNsfwVerifiedListExpires = 0l;

	static SteemJ steemJ() {
		try {
			return _steemJ == null ? _steemJ = new SteemJ() : _steemJ;
		} catch (SteemCommunicationException | SteemResponseException e) {
			return null;
		}
	}
	
	public static synchronized Map<String, AccountInfo> getBlogDetails(Collection<String> active) {
		List<AccountName> activeAccounts = new ArrayList<>();
		for (String a: active) {
			activeAccounts.add(new AccountName(a));
		}
		Map<String, AccountInfo> map = new HashMap<>();
		try {
			for (ExtendedAccount extendedInfo: steemJ().getAccounts(activeAccounts)) {
				AccountInfo info = new AccountInfo();
				info.setCreated(extendedInfo.getCreated().getDateTimeAsDate());
				String jsonMetadata = extendedInfo.getJsonMetadata();
				try {
					AccountMetadata meta = Mapper.get().readValue(jsonMetadata, AccountMetadata.class);
					if (meta!=null) {
						AccountProfile profile = meta.getProfile();
						if (profile!=null) {
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
				//extendedInfo.getPostCount();
				//extendedInfo.getTagsUsage();
				map.put(extendedInfo.getName().getName(), info);
			}
		} catch (SteemCommunicationException | SteemResponseException e) {
			return map;
		}
		return map;
	}
	
	public static synchronized List<String> getActiveNsfwVerifiedList() {
		List<String> active = new ArrayList<>();
		Set<String> verified = new HashSet<>(getNsfwVerifiedList());
		Set<String> already = new HashSet<>();
		List<BlogEntry> entries;
		try {
			entries = steemJ().getBlogEntries(ACCOUNT_DPORN, 0, (short) 100);
		} catch (SteemCommunicationException | SteemResponseException e) {
			return cachedActiveNsfwVerifiedList;
		}
		for (BlogEntry entry: entries) {
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
		return active;
	}

	private static List<String> cachedNsfwVerifiedList = new ArrayList<>();
	private static long cachedNsfwVerifiedSetExpires = 0l;
	public static synchronized List<String> getNsfwVerifiedList() {
		if (!cachedNsfwVerifiedList.isEmpty() && cachedNsfwVerifiedSetExpires > System.currentTimeMillis()) {
			return new ArrayList<>(cachedNsfwVerifiedList);
		}
		Set<String> set = new TreeSet<>();
		List<FollowApiObject> following;
		AccountName startAccount = new AccountName("");
		do {
			try {
				following = steemJ().getFollowing(new AccountName("verifiednsfw"), startAccount, FollowType.BLOG, (short) 10);
			} catch (SteemCommunicationException | SteemResponseException e) {
				following = new ArrayList<>();
			}
			for (FollowApiObject followed : following) {
				set.add(followed.getFollowing().getName());
			}
			if (!following.isEmpty()) {
				startAccount = following.get(following.size()-1).getFollowing();
			}
		} while (following.size() > 1);
		cachedNsfwVerifiedList.clear();
		cachedNsfwVerifiedList.addAll(set);
		cachedNsfwVerifiedSetExpires=System.currentTimeMillis()+_30_MINUTES;
		return new ArrayList<>(cachedNsfwVerifiedList);
	}
}
