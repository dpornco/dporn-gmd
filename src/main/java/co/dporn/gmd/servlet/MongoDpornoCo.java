package co.dporn.gmd.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
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
			return post;
		} finally {
			client.close();
		}
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
				// post.setCoverImage("https://cloudflare-ipfs.com/ipfs/"+item.getString("posterHash"));
				post.setCoverImageIpfs(item.getString("posterHash"));
				post.setCreated(item.getDate("posteddate"));
				post.setId(item.getObjectId("_id").toHexString());
				post.setPermlink(item.getString("permlink"));
				post.setScore(-1);
				post.setTitle(item.getString("title"));
				post.setVideoIpfs(item.getString("originalHash"));
				list.add(post);
			}
			find.close();
		} finally {
			client.close();
		}
		return list;
	}
}
