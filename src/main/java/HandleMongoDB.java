import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class HandleMongoDB {
    private com.mongodb.MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> toBeCrawled;

    public HandleMongoDB() {
        this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
        this.db = mongoClient.getDatabase("SampleDB");
        this.toBeCrawled = db.getCollection("toBeCrawled");
    }

    public void getCrawledDocs(List<Document> documents)
    {
        toBeCrawled.find().map(doc -> new Document("_id", doc.get("_id")).append("url", doc.get("url"))).into(documents);
    }
}
