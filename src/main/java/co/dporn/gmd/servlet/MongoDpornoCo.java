package co.dporn.gmd.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import co.dporn.gmd.shared.Post;

public class MongoDpornoCo {

	static {
		Logger mongoLogger = Logger.getLogger("org.mongodb");
		mongoLogger.setLevel(Level.WARNING);
	}
	
	private static final int MAX_TAG_SUGGEST=10;
	private static final Set<String> CACHED_TAGS = new TreeSet<>();
	private static long cachedTagsExpire=0;
	public static synchronized List<String> getMatchingTags(String prefix) {

		if (CACHED_TAGS.isEmpty() || cachedTagsExpire<System.currentTimeMillis()) {
			initCachedTags();
		}
		
		prefix = prefix.trim().toLowerCase();
		if (prefix.isEmpty()) {
			return new ArrayList<>(CACHED_TAGS).subList(0, MAX_TAG_SUGGEST);
		}
		
		Set<String> tags = new TreeSet<>();
		for (String tag: CACHED_TAGS) {
			if (tag.startsWith(prefix)) {
				tags.add(tag);
				if (tags.size()>=MAX_TAG_SUGGEST) {
					break;
				}
			}
		}
		return new ArrayList<>(tags);		
	}

	private static void initCachedTags() {
		listPosts("", 500).forEach(p->{
			CACHED_TAGS.addAll(p.getTags());
		});
	}

	public static synchronized Post getPost(String authorname, String permlink) {
		MongoClient client = MongoClients.create();
		try {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection("videos");
			Document item = collection
					.find(Filters.and(Filters.eq("username", authorname), Filters.eq("permlink", permlink))).first();
			Post post = new Post();
			if (item==null || item.isEmpty()) {
				return post;
			}
			post.setAuthor(item.getString("username"));
			post.setCoverImageIpfs(item.getString("posterHash"));
			post.setCreated(item.getDate("posteddate"));
			post.setId(item.getObjectId("_id").toHexString());
			post.setPermlink(item.getString("permlink"));
			post.setScore(-1);
			post.setTitle(item.getString("title"));
			post.setVideoIpfs(item.getString("originalHash"));
			post.setTags(extractTags(item.getString("tags")));
			return post;
		} finally {
			client.close();
		}
	}

	@SuppressWarnings("serial")
	protected static class ListOfStrings extends ArrayList<String> {};
	private static List<String> extractTags(String string) {
		if (string == null ||string.trim().isEmpty()) {
			return new ArrayList<>();
		}
		string = string.trim();
		Set<String> tags = new TreeSet<String>();
		//tags are either a) a json array of strings, b) a comma list of words
		if (!string.startsWith("[")) {
			//munge comma array of words into a json array list;
			string = StringEscapeUtils.escapeJson(string);
			string = string.replace(",", "\",\"");
			string = "[\"" + string + "\"]";
		}
		List<String> tmptags;
		try {
			tmptags = Mapper.get().readValue(string, ListOfStrings.class);
		} catch (IOException e) {
			tmptags= new ArrayList<>();
		}
		for (String tmp: tmptags) {
			tmp = tmp.trim().toLowerCase().replace(" ", "-");
			if (tmp.isEmpty() || StringUtils.countMatches(tmp, "-")>1) {
				continue;
			}
			tags.add(tmp);
		}
		return new ArrayList<>(tags);
	}

	public static synchronized List<Post> listPosts(String startId, int count) {
		if (count<1) {
			count = 1;
		}
		if (count>50) {
			count = 50;
		}
		List<Post> list = new ArrayList<>();
		MongoClient client = MongoClients.create();
		try {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection("videos");
			MongoCursor<Document> find;
			if (startId!=null && !startId.trim().isEmpty()) {
				find = collection.find(Filters.lte("_id", new ObjectId(startId)) )//
						.sort(Sorts.descending("_id"))//
						.limit(count).iterator();
			} else {
				find = collection.find()//
						.sort(Sorts.descending("_id"))//
						.limit(count).iterator();
			}
			while (find.hasNext()) {
				Document item = find.next();
				Post post = new Post();
				post.setAuthor(item.getString("username"));
				post.setCoverImageIpfs(item.getString("posterHash"));
				post.setCreated(item.getDate("posteddate"));
				post.setId(item.getObjectId("_id").toHexString());
				post.setPermlink(item.getString("permlink"));
				post.setScore(-1);
				post.setTitle(item.getString("title"));
				post.setVideoIpfs(item.getString("originalHash"));
				post.setTags(extractTags(item.getString("tags")));
				list.add(post);
			}
			find.close();
		} finally {
			client.close();
		}
		return list;
	}
	
	public static synchronized List<Post> listPostsFor(String username, String startId, int count) {
		if (count<1) {
			count = 1;
		}
		if (count>50) {
			count = 50;
		}
		List<Post> list = new ArrayList<>();
		MongoClient client = MongoClients.create();
		try {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection("videos");
			MongoCursor<Document> find;
			Bson eqUsername = Filters.eq("username", username);
			if (startId!=null && !startId.trim().isEmpty()) {
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
				Post post = new Post();
				post.setAuthor(item.getString("username"));
				post.setCoverImageIpfs(item.getString("posterHash"));
				post.setCreated(item.getDate("posteddate"));
				post.setId(item.getObjectId("_id").toHexString());
				post.setPermlink(item.getString("permlink"));
				post.setScore(-1);
				post.setTitle(item.getString("title"));
				post.setVideoIpfs(item.getString("originalHash"));
				post.setTags(extractTags(item.getString("tags")));
				list.add(post);
			}
			find.close();
		} finally {
			client.close();
		}
		return list;
	}
}
