package co.dporn.gmd.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import co.dporn.gmd.shared.Post;

public class MongoDpornoCo {
	
	private static final int PAGE_SIZE = 25;

	static {
		Logger mongoLogger = Logger.getLogger("org.mongodb");
		mongoLogger.setLevel(Level.WARNING);
	}

	public static synchronized List<Post> listPosts(int page) {
		List<Post> list = new ArrayList<>();
		MongoClient client = MongoClients.create();
		try {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection("videos");
			MongoCursor<Document> find = collection.find()//
					.sort(Sorts.descending("posteddate"))//
					.skip(PAGE_SIZE * page)//
					.limit(50).iterator();
			int id=50*page;
			while (find.hasNext()) {
				Document item = find.next();
				Post post = new Post();
				post.setAuthor(item.getString("username"));
//				post.setCoverImage("https://cloudflare-ipfs.com/ipfs/"+item.getString("posterHash"));
				post.setCoverImageIpfs(item.getString("posterHash"));
				post.setCreated(item.getDate("posteddate"));
				post.setId(id++);
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
