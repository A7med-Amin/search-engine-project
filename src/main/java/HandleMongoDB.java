import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

public class HandleMongoDB {
    private com.mongodb.MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> toBeCrawled;
    private MongoCollection<Document> wordsIndices;
    private MongoCollection<Document> searchQuery;

    public HandleMongoDB() {
        this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
        this.db = mongoClient.getDatabase("SampleDB");
        this.toBeCrawled = db.getCollection("toBeCrawled");
        this.wordsIndices = db.getCollection("wordsIndices");
        this.searchQuery = db.getCollection("searchQuery");
    }

    // Get Crawled documents from DB
    public void getCrawledDocs(List<Document> documents)
    {
        toBeCrawled.find().map(doc -> new Document("_id", doc.get("_id")).append("url", doc.get("url"))).into(documents);
    }

    // Add documents of words into DB
    public void insertWordsIntoDb(Document doc)
    {

        wordsIndices.insertOne(doc);
    }
}
