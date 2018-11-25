package co.dporn.gmd.servlet.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import co.dporn.gmd.servlet.utils.Mapper;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.MongoDate;
import co.dporn.gmd.shared.MongoId;

public class MongoDpornoCo {

	@SuppressWarnings("serial")
	protected static class ListOfStrings extends ArrayList<String> {
	}
	private static final Set<String> CACHED_TAGS = new TreeSet<>();
	private static long cachedTagsExpire = 0;

	private static Map<String, BlogEntry> ENTRY_CACHE = new LRUMap<>(128);

	private static final int MAX_TAG_SUGGEST = 20;
	private static final String[] NO_TAG_SUGGEST = { "dporn", "dpornvideo", "dpornco", "dporncovideo", "nsfw" };
	private static final String TABLE_BLOG_ENTRIES_V2 = "blogEntries_v2";

	private static final String TABLE_VIDEOS = "videos";

	/**
	 * 30 minute tag cache
	 */
	private static final long TAGS_EXPIRE_TIME = 1000l * 60l * 30l;

	static {
		Logger mongoLogger = Logger.getLogger("org.mongodb");
		mongoLogger.setLevel(Level.WARNING);
	}

	protected static synchronized List<BlogEntry> _listEntries(String startId, int count) {
		migrationCheck();
		if (count < 1) {
			count = 1;
		}
		if (count > 1000) {
			count = 1000;
		}
		List<BlogEntry> list = new ArrayList<>();
		try (MongoClient client = MongoClients.create()) {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection(TABLE_BLOG_ENTRIES_V2);
			MongoCursor<Document> find;
			if (startId != null && !startId.trim().isEmpty()) {
				find = collection.find(Filters.lte("_id", new ObjectId(startId)))//
						.sort(Sorts.descending("_id"))//
						.limit(count).iterator();
			} else {
				find = collection.find()//
						.sort(Sorts.descending("_id"))//
						.limit(count).iterator();
			}
			while (find.hasNext()) {
				Document item = find.next();
				try {
					list.add(deserializeAndSanitizeEntry(item));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			find.close();
		}
		System.out.println("_listEntries: " + startId + ", " + count + ", " + list.size());
		return list;
	}

	private static BlogEntry deserializeAndSanitizeEntry(Document item)
			throws IOException, JsonParseException, JsonMappingException {
		BlogEntry entry = MongoJsonMapper.get().readValue(item.toJson(), BlogEntry.class);
		
		return entry;
	}

	private static List<String> extractTags(String string) {
		if (string == null || string.trim().isEmpty()) {
			return new ArrayList<>();
		}
		string = string.trim();
		Set<String> tags = new LinkedHashSet<String>();
		// tags are either a) a json array of strings, b) a comma list of words
		if (!string.startsWith("[")) {
			// munge comma array of words into a json array list;
			string = StringEscapeUtils.escapeJson(string);
			string = string.replace(",", "\",\"");
			string = "[\"" + string + "\"]";
		}
		List<String> tmptags;
		try {
			tmptags = Mapper.get().readValue(string, ListOfStrings.class);
		} catch (IOException e) {
			tmptags = new ArrayList<>();
		}
		for (String tmp : tmptags) {
			tmp = tmp.trim().toLowerCase().replace(" ", "-");
			if (tmp.isEmpty() || StringUtils.countMatches(tmp, "-") > 1) {
				continue;
			}
			tags.add(tmp);
		}
		return new ArrayList<>(tags);
	}

	public static BlogEntry getEntry(String authorname, String permlink) {
		String key = authorname + "|" + permlink;
		BlogEntry cached = ENTRY_CACHE.get(key);
		if (cached != null) {
			return cached;
		}
		synchronized (ENTRY_CACHE) {
			try (MongoClient client = MongoClients.create()) {
				MongoDatabase db = client.getDatabase("dpdb");
				MongoCollection<Document> collection = db.getCollection(TABLE_BLOG_ENTRIES_V2);
				Document item = collection
						.find(Filters.and(Filters.eq("username", authorname), Filters.eq("permlink", permlink)))
						.first();
				if (item == null || item.isEmpty()) {
					return new BlogEntry();
				}
				try {
					BlogEntry entry = deserializeAndSanitizeEntry(item);
					ENTRY_CACHE.put(key, entry);
					return entry;
				} catch (IOException e) {
				}
			} finally {
				System.out.println("getEntry: " + authorname + ", " + permlink);
			}
			return new BlogEntry();
		}
	}

	public static synchronized List<String> getMatchingTags(String prefix) {

		if (CACHED_TAGS.isEmpty() || cachedTagsExpire < System.currentTimeMillis()) {
			initCachedTags();
		}

		prefix = prefix.trim().toLowerCase();
		if (prefix.isEmpty()) {
			return new ArrayList<>(CACHED_TAGS).subList(0, MAX_TAG_SUGGEST);
		}

		Set<String> tags = new TreeSet<>();
		for (String tag : CACHED_TAGS) {
			if (tag.startsWith(prefix)) {
				tags.add(tag);
				if (tags.size() >= MAX_TAG_SUGGEST) {
					break;
				}
			}
		}
		return new ArrayList<>(tags);
	};

	private static synchronized void initCachedTags() {
		CACHED_TAGS.clear();
		_listEntries("", 250).forEach(p -> {
			CACHED_TAGS.addAll(p.getCommunityTags());
		});
		CACHED_TAGS.removeAll(Arrays.asList(NO_TAG_SUGGEST));
		if (!CACHED_TAGS.isEmpty()) {
			cachedTagsExpire = System.currentTimeMillis() + TAGS_EXPIRE_TIME;
		}
	}

	public static boolean insertEntry(BlogEntry entry) {
		String json;
		try {
			json = MongoJsonMapper.get().writeValueAsString(entry);
			System.out.println("INSERT JSON: " + json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
		Document doc = Document.parse(json);
		System.out.println("INSERT DOC: " + doc.toJson());
		try (MongoClient client = MongoClients.create()) {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection(TABLE_BLOG_ENTRIES_V2);
			try {
				collection.insertOne(doc);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static synchronized List<BlogEntry> listEntries(String startId, int count) {
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		return _listEntries(startId, count);
	}

	public static synchronized List<BlogEntry> listEntriesFor(String username, String startId, int count) {
		migrationCheck();
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		List<BlogEntry> list = new ArrayList<>();
		try (MongoClient client = MongoClients.create()) {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection(TABLE_BLOG_ENTRIES_V2);
			MongoCursor<Document> find;
			Bson eqUsername = Filters.eq("username", username);
			if (startId != null && !startId.trim().isEmpty()) {
				Bson lteId = Filters.lte("_id", new ObjectId(startId));
				Bson and = Filters.and(eqUsername, lteId);
				find = collection.find(and) //
						.sort(Sorts.descending("_id"))//
						.limit(count).iterator();
			} else {
				find = collection.find(eqUsername) //
						.sort(Sorts.descending("_id"))//
						.limit(count).iterator();
			}
			while (find.hasNext()) {
				Document item = find.next();
				try {
					BlogEntry entry = deserializeAndSanitizeEntry(item);
					list.add(entry);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			find.close();
		}
		System.out.println("listEntriesFor: " + username + ", " + startId + ", " + count + ", " + list.size());
		return list;
	}

	private static synchronized void migrationCheck() {
		try (MongoClient client = MongoClients.create()) {
			MongoDatabase db = client.getDatabase("dpdb");
			try {
				db.createCollection(TABLE_BLOG_ENTRIES_V2);
			} catch (Exception e1) {
			}
			MongoCollection<Document> videos_old = db.getCollection(TABLE_VIDEOS);
			MongoCollection<Document> blogEntries = db.getCollection(TABLE_BLOG_ENTRIES_V2);

			try (MongoCursor<Document> forMigration = videos_old.find(Filters.exists("migrated", false))
					.sort(Sorts.ascending("_id")).batchSize(100).iterator()) {
				while (forMigration.hasNext()) {
					Document next = forMigration.next();
					BlogEntry entry = new BlogEntry();
					String id = next.getObjectId("_id").toHexString();
					String title = next.getString("title");
					String permlink = next.getString("permlink");
					String content = next.getString("content");
					String videoPath = "/ipfs/" + next.getString("originalHash");
					String posterImagePath = "/ipfs/" + next.getString("posterHash");
					String username = next.getString("username");
					Date postedDate = next.getDate("posteddate");
					Set<String> extractedTags = new LinkedHashSet<>(extractTags(next.getString("tags")));
					extractedTags.add("@" + username);

					entry.setId(new MongoId(id));
					entry.setTitle(title);
					entry.setPermlink(permlink);
					entry.setContent(content);
					entry.setPostTags(new ArrayList<>(extractedTags));
					entry.setCommunityTags(new ArrayList<>(extractedTags));
					entry.setVideoPath(videoPath);
					entry.setPosterImagePath(posterImagePath);
					entry.setUsername(username);

					entry.setCreated(new MongoDate(postedDate));
					entry.setModified(entry.getCreated());

					try {
						blogEntries.insertOne(Document.parse(MongoJsonMapper.get().writeValueAsString(entry)));
						next.put("migrated", true);
						videos_old.replaceOne(Filters.eq(next.getObjectId("_id")), next);
					} catch (com.mongodb.MongoWriteException | JsonProcessingException e) {
						if (!e.getMessage().contains("E11000 duplicate key error collection")) {
							throw new RuntimeException(e);
						} else {
							System.err.println(e.getMessage());
							next.put("migrated", true);
							videos_old.replaceOne(Filters.eq(next.getObjectId("_id")), next);
						}
					}
				}
			}
		}
	}

	public static synchronized void deleteEntry(String username, String permlink) {
		try (MongoClient client = MongoClients.create()) {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> blogEntries = db.getCollection(TABLE_BLOG_ENTRIES_V2);
			blogEntries.deleteOne(Filters.and(Filters.eq("username", username), Filters.eq("permlink", permlink)));
		}
	}
}
