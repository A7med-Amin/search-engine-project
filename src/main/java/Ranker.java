import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import com.database.objects.IndexerItem;
import com.database.objects.ChoosenDocument;
import com.database.objects.OutputArray;
import com.database.objects.DocumentSource;
import com.database.objects.CrawledItem;


import javax.print.Doc;
import java.util.*;
import java.util.Map;
import java.util.Arrays;
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
    
    
    public void phraseRelvanceDetector(String query) {
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
              //I have an indexer item, Item . I am looping on the doucment within it to create a different data structure
                // Array of intersected documents
                DocumentChoosen.setDoc_id(Item.documents[j].doc_id);
                DocumentChoosen.setIDF(IDF);
                
                DocumentChoosen.availableWords.add(Pair.of(words[i], Item.documents[j].positions));
    
                if (i == 0) {
                    output.AddDoucment(DocumentChoosen);
                } else {
                    output.getIntersectedDocuments(DocumentChoosen);
                }
    
    
    
            }
            output.choosenDocuments.clear();
            output.choosenDocuments.addAll(output.intersectedDocuments);
            output.intersectedDocuments.clear();
            
        }
    
        phraseRelevanceConfim(output, words);
        output.toString();
    }
    
    public void phraseRelevanceConfim( OutputArray output, String[] query){
        
        int numOfPhrases = 0;
        //loop over number of documents to know which one is gotta be deprived
        for (int i=0; i<output.choosenDocuments.size(); i++){
            
            //loop over number of occ. of the first word in the query
            for (int j=0; j<output.choosenDocuments.get(i).availableWords.get(0).getRight().length; j++){
                
                int firstOccurance = output.choosenDocuments.get(i).availableWords.get(0).getRight()[j];
                
                int isPhraseFound = 0;
                for (int k=1; k< query.length; k++){
                    int [] positions = output.choosenDocuments.get(i).availableWords.get(k).getRight();
                    
                    if (Arrays.binarySearch(positions,(firstOccurance+k)) >=0)
                        isPhraseFound++;
                    else
                        break;
                }
                
                if (isPhraseFound == query.length-1)
                    numOfPhrases++;
            }
            output.choosenDocuments.get(i).setTF(numOfPhrases);
        }
        
        
    }
    
    
    public void populairty() {
        //Getting the number of documents in the database
        long dataBaseSize = mockDB.countDocuments();
        // Initiating the treemap that will hold the database items
        TreeMap<String, DocumentSource> treeMap_i  = new TreeMap<>();
        TreeMap<String, DocumentSource> treeMap_ii = new TreeMap<>();
    
        Document update = new Document("$set", new Document("pop", (float) 1/6001));
        mockDB.updateMany(new Document(), update);
        //looping on the number of the documents in the database and store them in the treemap "i"
        
        for (Document document : mockDB.find().limit(6001)) {
            Gson gson = new Gson();
            CrawledItem Crawled = gson.fromJson(document.toJson(), CrawledItem.class);
            DocumentSource DocumentSource = new DocumentSource(Crawled.parentPages, Crawled.includedLinks, Crawled.pop);
            //get the treemap full of entries
            treeMap_i.put(Crawled.url, DocumentSource);
        }
        
        //number of iterations is missing
        for (String key : treeMap_i.keySet()) {
           
            System.out.println(key);
            //Do calculations for each document
            String parentPages[] = treeMap_i.get(key).getCameFrom();
            double newPopularity = 0;
            //This means that the document has no parent pages, and its popularity will still as it is
            if (parentPages[0] ==null)
                treeMap_ii.put(key, treeMap_i.get(key));
            else{
                for (int i=0; i< parentPages.length; i++){
                    double parentPop = treeMap_i.get(parentPages[i]).getPop();
                    int parentOutgoingLinks = treeMap_i.get(parentPages[i]).getHyperLinkCount();
                    newPopularity = newPopularity + (parentPop/parentOutgoingLinks);
                    treeMap_ii.put(key, new DocumentSource(treeMap_i.get(key).getCameFrom(), treeMap_i.get(key).getHyperLinkCount(), newPopularity));
                    mockDB.updateOne(new Document("url", key), Updates.set("pop", newPopularity));
    
                }
                
            }
            
        }
        
    }
    
    
    //create main function to test
    public static void main(String[] args) {
        Ranker ranker = new Ranker();

        ranker.phraseRelvanceDetector("food animal fruit");
    }
    
}