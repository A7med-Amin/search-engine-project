import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import java.io.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.bson.Document;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;


import crawlercommons.filters.basic.BasicURLNormalizer;
public class Crawler implements Runnable {
    //define a hash map for already crawled must not visit the same PAGE more than once "see compact string thing"
    //define a seed set fill with awel 10 zewws
    //crawl the 10 zewws, extracted hyperlinks added to the db
    //define array for links to be crawler bl tarteeb
    //handle the robo thing for security issues
    //The crawler can only crawl documents of specific types (HTML is sufficient for the project).??
    //The crawler must maintain its state so that it can, if interrupted, be started again to crawl the documents on the list without revisiting documents that have been previously downloaded.??
    //6000 max crawled links
    static HashMap<String, Integer> crawledAlready = new HashMap<String, Integer>(); //hashmap to store crawled link to avoid redoing them when coming from db
    static Integer NoOfCrawledPagesMax=6000; //max no of to be crawled pages
    static Integer NoOfCrawledPagesAlready=0; //max no of to be crawled pages
    static Queue<String> toBeCrawledLinks = new LinkedList<String>(); //array of to be crawled links FIFO

    static class HandleMongoDB {
        private MongoClient mongoClient;
        private MongoDatabase db;
        private MongoCollection<Document> toBeCrawled;

        public HandleMongoDB() {
            this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
            this.db = mongoClient.getDatabase("SampleDB");
            this.toBeCrawled = db.getCollection("toBeCrawled");
        }

        public void fillSeedSet() {
            try {
                File seedSetText = new File("seedSet.txt");
                Scanner myScannerReader = new Scanner(seedSetText);
                BasicURLNormalizer normalizer = new BasicURLNormalizer();

                while (myScannerReader.hasNextLine()) {
                    String urlData = myScannerReader.nextLine();
                    toBeCrawled.insertOne(new Document("url", normalizer.filter(urlData)));
                }

                myScannerReader.close();
            } catch (FileNotFoundException ex) {
                System.out.println("An Error has Occurred During Reading URL Seed List...");
                ex.printStackTrace();
            }
        }
        public boolean hashed(String url)
        {
           return true;
        }
        public void addToToBeCrawledLinks(String url){
            BasicURLNormalizer normalizer = new BasicURLNormalizer();
            String normalizedUrl = normalizer.filter(url);
            if (!url.contains("void")  && url.contains("http")&&!toBeCrawledLinks.contains(normalizedUrl)
                  ) {
                toBeCrawledLinks.add(normalizedUrl);
                toBeCrawled.insertOne(new Document("url", normalizedUrl));
            }

        }

    }




    public void crawl(){
        //check fi we reached the max for crawling
        if(NoOfCrawledPagesAlready<NoOfCrawledPagesMax && !toBeCrawledLinks.isEmpty() ){
            String URL;//see how to fill it lsa




            NoOfCrawledPagesAlready++; //increment
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException {

      //maintain threading 3lshan n5ls crawling bsor3a
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        mongoDBHandler.fillSeedSet();
    }

    @Override
    public void run() {

    }
}
