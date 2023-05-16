import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.database.objects.IndexerItem;
import com.database.objects.ChoosenDocument;
import com.database.objects.OutputArray;


public class Ranker {
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> mockDB;

    public Ranker() {
        this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
        this.db = mongoClient.getDatabase("MarwanMockup");
        this.mockDB = db.getCollection("MockupDB");
    }
    
    public void relvanceDetector(String query) {
        query = query.toLowerCase();
        String[] words = query.split("\\W+");
        OutputArray output = new OutputArray();
        
        //looping on the number of the words in the query
        for (int i = 0; i < words.length; i++) {
            Document doc = mockDB.find(new Document("id", words[i])).first();
            Gson gson = new Gson();
            IndexerItem Item = gson.fromJson(doc.toJson(), IndexerItem.class);
            double IDF = Math.log10(7/Item.DF);

            for (int j = 0; j < Item.documents.length; j++) {
                ChoosenDocument DocumentChoosen = new ChoosenDocument();
                double N_TF = (Item.documents[j].TF) / Double.parseDouble(Item.documents[j].doc_len);
                double TF_IDF = N_TF * IDF;
                DocumentChoosen.setDoc_id(Item.documents[j].doc_id);
                DocumentChoosen.setTF(N_TF);
                DocumentChoosen.setIDF(IDF);
                DocumentChoosen.setTF_IDF(TF_IDF);
                output.AddDoucment(DocumentChoosen);
            }
            
        }
    
        output.toString();
    }
    
    //create main function to test
    public static void main(String[] args) {
        Ranker ranker = new Ranker();
        ranker.relvanceDetector("phone food");
    
    }
    
}