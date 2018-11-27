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
import org.owasp.html.AttributePolicy;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableSet;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import co.dporn.gmd.servlet.utils.Mapper;
import co.dporn.gmd.shared.BlogEntry;
import co.dporn.gmd.shared.BlogEntryType;
import co.dporn.gmd.shared.MongoDate;
import co.dporn.gmd.shared.MongoId;

public class MongoDpornCo {

	@SuppressWarnings("serial")
	protected static class ListOfStrings extends ArrayList<String> {
	}

	private static final Set<String> CACHED_TAGS = new TreeSet<>();
	private static long cachedTagsExpire = 0;

	private static Map<String, BlogEntry> ENTRY_CACHE = new LRUMap<>(128);

	private static final int MAX_TAG_SUGGEST = 20;
	private static final String[] NO_TAG_SUGGEST = { "dporn", "dpornvideo", "dpornco", "dporncovideo", "nsfw" };
	private static final String TABLE_BLOG_ENTRIES_V2 = "blogEntries_v2";
	private static final String TABLE_BLOG_ENTRIES_V2_BACKUP = "blogEntries_v2_backup";

	private static final String TABLE_VIDEOS = "videos";

	/**
	 * 30 minute tag cache
	 */
	private static final long TAGS_EXPIRE_TIME = 1000l * 60l * 30l;

	static {
		Logger mongoLogger = Logger.getLogger("org.mongodb");
		mongoLogger.setLevel(Level.WARNING);
	}

	protected static synchronized List<BlogEntry> _listBlogEntries(BlogEntryType entryType, String startId, int count) {
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
			List<Bson> andFilters = new ArrayList<>();
			if (entryType != null && entryType != BlogEntryType.ANY) {
				andFilters.add(Filters.eq("entryType", entryType.name()));
			}
			if (startId != null && !startId.trim().isEmpty()) {
				andFilters.add(Filters.lte("_id", new ObjectId(startId)));
			}
			if (!andFilters.isEmpty()) {
				find = collection.find(Filters.and(andFilters.toArray(new Bson[0])))//
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
					list.add(deserializeAndSanitizeContent(item));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			find.close();
		}
		return list;
	}

	private static final ImmutableSet<String> WHITELIST = ImmutableSet.of("float", "clear", "-moz-border-radius",
			"-moz-border-radius-bottomleft", "-moz-border-radius-bottomright", "-moz-border-radius-topleft",
			"-moz-border-radius-topright", "-moz-box-shadow", "-moz-outline", "-moz-outline-color",
			"-moz-outline-style", "-moz-outline-width", "-o-text-overflow", "-webkit-border-bottom-left-radius",
			"-webkit-border-bottom-right-radius", "-webkit-border-radius", "-webkit-border-radius-bottom-left",
			"-webkit-border-radius-bottom-right", "-webkit-border-radius-top-left", "-webkit-border-radius-top-right",
			"-webkit-border-top-left-radius", "-webkit-border-top-right-radius", "-webkit-box-shadow", "azimuth",
			"background", "background-attachment", "background-color", "background-image", "background-position",
			"background-repeat", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius",
			"border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse",
			"border-color", "border-left", "border-left-color", "border-left-style", "border-left-width",
			"border-radius", "border-right", "border-right-color", "border-right-style", "border-right-width",
			"border-spacing", "border-style", "border-top", "border-top-color", "border-top-left-radius",
			"border-top-right-radius", "border-top-style", "border-top-width", "border-width", "box-shadow",
			"caption-side", "color", "cue", "cue-after", "cue-before", "direction", "elevation", "empty-cells", "font",
			"font-family", "font-size", "font-stretch", "font-style", "font-variant", "font-weight", "height",
			"image()", "letter-spacing", "line-height", "linear-gradient()", "list-style", "list-style-image",
			"list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left", "margin-right",
			"margin-top", "max-height", "max-width", "min-height", "min-width", "outline", "outline-color",
			"outline-style", "outline-width", "padding", "padding-bottom", "padding-left", "padding-right",
			"padding-top", "pause", "pause-after", "pause-before", "pitch", "pitch-range", "quotes",
			"radial-gradient()", "rect()", "repeating-linear-gradient()", "repeating-radial-gradient()", "rgb()",
			"rgba()", "richness", "speak", "speak-header", "speak-numeral", "speak-punctuation", "speech-rate",
			"stress", "table-layout", "text-align", "text-decoration", "text-indent", "text-overflow", "text-shadow",
			"text-transform", "text-wrap", "unicode-bidi", "vertical-align", "voice-family", "volume", "white-space",
			"width", "word-spacing", "word-wrap");

	private static CssSchema dpornAllowedStyles() {
		return CssSchema.withProperties(WHITELIST);
	}

	private static final PolicyFactory STYLES = new HtmlPolicyBuilder().allowStyling(dpornAllowedStyles()).toFactory();

	private static final AttributePolicy INTEGER = new AttributePolicy() {
		public String apply(String elementName, String attributeName, String value) {
			int n = value.length();
			if (n == 0) {
				return null;
			}
			for (int i = 0; i < n; ++i) {
				char ch = value.charAt(i);
				if (ch == '.') {
					if (i == 0) {
						return null;
					}
					return value.substring(0, i); // truncate to integer.
				} else if (!('0' <= ch && ch <= '9')) {
					return null;
				}
			}
			return value;
		}
	};

	/**
	 * Allows {@code <img>} elements from HTTP, HTTPS, and relative sources.
	 */
	public static final PolicyFactory IMAGES = new HtmlPolicyBuilder().allowUrlProtocols("http", "https")
			.allowElements("img").allowAttributes("alt", "src").onElements("img") //
			.allowAttributes("border", "height", "width").matching(INTEGER).onElements("img") //
			//.allowAttributes("srcset").onElements("img")// sanitizer mangles srcset urls
			.toFactory();

	private static PolicyFactory policy = Sanitizers.BLOCKS//
			.and(Sanitizers.FORMATTING)//
			.and(MongoDpornCo.IMAGES)//
			.and(Sanitizers.LINKS)//
			.and(MongoDpornCo.STYLES)//
			.and(Sanitizers.TABLES);

	private static BlogEntry deserializeAndSanitizeContent(Document item)
			throws IOException, JsonParseException, JsonMappingException {
		BlogEntry entry = MongoJsonMapper.get().readValue(item.toJson(), BlogEntry.class);
		String sanitized = policy.sanitize(entry.getContent());
		entry.setContent(sanitized);
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
					BlogEntry entry = deserializeAndSanitizeContent(item);
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
		_listBlogEntries(BlogEntryType.ANY, "", 250).forEach(p -> {
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

	public static boolean insertBackupEntry(BlogEntry entry) {
		String json;
		try {
			json = MongoJsonMapper.get().writeValueAsString(entry);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
		Document doc = Document.parse(json);
		try (MongoClient client = MongoClients.create()) {
			MongoDatabase db = client.getDatabase("dpdb");
			try {
				db.createCollection(TABLE_BLOG_ENTRIES_V2_BACKUP);
			} catch (Exception e1) {
			}
			MongoCollection<Document> collection = db.getCollection(TABLE_BLOG_ENTRIES_V2_BACKUP);
			try {
				collection.insertOne(doc);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static synchronized List<BlogEntry> listBlogEntries(BlogEntryType entryType, String startId, int count) {
		if (count < 1) {
			count = 1;
		}
		if (count > 50) {
			count = 50;
		}
		return _listBlogEntries(entryType, startId, count);
	}

	public static synchronized List<BlogEntry> listBlogEntriesFor(String username, String startId, int count) {
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
					BlogEntry entry = deserializeAndSanitizeContent(item);
					list.add(entry);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			find.close();
		}
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
		BlogEntry entry = getEntry(username, permlink);
		if (entry == null || !username.equals(entry.getUsername())) {
			return;
		}
		if (insertBackupEntry(entry)) {
			try (MongoClient client = MongoClients.create()) {
				MongoDatabase db = client.getDatabase("dpdb");
				MongoCollection<Document> blogEntries = db.getCollection(TABLE_BLOG_ENTRIES_V2);
				Bson eqUsername = Filters.eq("username", username);
				Bson eqPermlink = Filters.eq("permlink", permlink);
				Bson filter = Filters.and(eqUsername, eqPermlink);
				blogEntries.deleteOne(filter);
			}
		}
	}
}
