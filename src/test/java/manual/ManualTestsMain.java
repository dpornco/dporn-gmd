package manual;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

public class ManualTestsMain {

	public static void main(String[] args) {
		Logger mongoLogger = Logger.getLogger("org.mongodb");
		mongoLogger.setLevel(Level.WARNING);
		MongoClient client = MongoClients.create();
		try {
			MongoDatabase db = client.getDatabase("dpdb");
			MongoCollection<Document> collection = db.getCollection("videos");
			System.out.println(" Size: " + collection.countDocuments());
			MongoCursor<Document> find = collection.find().sort(Sorts.descending("posteddate")).limit(50).iterator();
			while (find.hasNext()) {
				Document item = find.next();
				System.out.println(" -> https://dporn.co/watch/@" + item.getString("username") + "/"
						+ item.getString("permlink") + " [" + item.getString("originalHash") + "]");
				if (!item.getString("originalHash").startsWith("Q")) {
					System.out.println(" : "+item.toJson());
				}
			}
			find.close();
		} finally {
			client.close();
		}
	}

}
