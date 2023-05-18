import com.indexer.Indexer;
import com.indexer.HandleMongoDB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.ranker.relevance.Ranker;
import com.webcrawler.Crawler;
import com.query_processor.QueryProcessor;

import java.io.IOException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws IOException {
    
        
          /*MongoClient mongoClient;
          MongoDatabase db;
          MongoCollection<Document> wordsIndices;
          MongoCollection<Document> searchQuery;
          MongoCollection<Document> stateOfCrawler;
          MongoCollection<Document> compactStrings;
          MongoCollection<Document> crawledAlreadyLinksDB;
          MongoCollection<Document> toBeCrawled;
         mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
         db = mongoClient.getDatabase("SampleDB");
         wordsIndices = db.getCollection("wordsIndices");
         toBeCrawled = db.getCollection("toBeCrawled");
         crawledAlreadyLinksDB = db.getCollection("crawledAlreadyLinks");
         stateOfCrawler = db.getCollection("stateOfCrawler");
         compactStrings = db.getCollection("compactStrings");*/
    
    
    
        Crawler crawler = new Crawler();
        Indexer indexer = new Indexer();
        QueryProcessor queryProcessor = new QueryProcessor();
        Ranker ranker = new Ranker();
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("-------------- Welcome to MilkyWay search engine CLI------------- " );
         System.out.println("(1) Configuration " );
         System.out.println("(2) Search " );
        String input1 = scanner.next();
         if (input1.equals("1")){
             System.out.println("--------------------------------------------------" );
             System.out.println("(1) Start crawling " );
             System.out.println("(2) Start Indexing " );
             String input2 = scanner.next();
             if (input2.equals("1"))
                ranker.populairty();
            else if (input2.equals("2"))
                indexer.main();
         }
         else if (input1.equals("2")){
             String cont = "continue$%";
             while (!cont.equals("!exit")){
                 System.out.println("--------------------------------------------------" );
                 System.out.println("Enter your query: " );
                 String input3 = scanner.next();
                 if (input3.charAt(0) == '"' && input3.charAt(input3.length()-1) == '"'){
                     //This is a phrase query
                     ranker.intersectionDetector(input3);
                 }
                 else {
                     //This is a normal query
                     ranker.relevanceDetector(input3);
                 }
    
                 System.out.println("To exit write !exit, to continue press any key" );
                 cont = scanner.next();
    
             }
         }
    

    }
}