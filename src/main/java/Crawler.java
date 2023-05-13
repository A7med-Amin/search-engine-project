import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.net.URL;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.bson.Document;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import crawlercommons.filters.basic.BasicURLNormalizer;
import java.io.*;
import java.util.*;

public class Crawler implements Runnable {

    static HashMap<String, Integer> crawledAlreadyHashMap = new HashMap<String, Integer>();
    static Integer NoOfCrawledPagesMax = 6000;
    static Integer NoOfAddedPagesAlready = 0;
    static Queue<String> toBeCrawledLinks = new LinkedList<String>();
    static Queue<String> crawledAlreadyLinks = new LinkedList<String>();
    static int numThreads = 8;
    static int numThreadsFinished = 0;
    static Object lock = new Object();

    static class HandleMongoDB {
        private MongoClient mongoClient;
        private MongoDatabase db;
        private MongoCollection<Document> toBeCrawled;
        private MongoCollection<Document> crawledAlreadyLinksDB;

        public HandleMongoDB() {
            this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
            this.db = mongoClient.getDatabase("SampleDB");
            this.toBeCrawled = db.getCollection("toBeCrawled");
            this.crawledAlreadyLinksDB = db.getCollection("crawledAlreadyLinks");
        }

        public void fillSeedSet() {
            try {
                File seedSetText = new File("seedSet.txt");
                Scanner myScannerReader = new Scanner(seedSetText);
                BasicURLNormalizer normalizer = new BasicURLNormalizer();

                while (myScannerReader.hasNextLine()) {
                    String urlData = myScannerReader.nextLine();

                    if (urlData.contains("http") && !toBeCrawledLinks.contains(normalizer.filter(urlData)) && urlData!=null) {
                        try {
                            org.jsoup.nodes.Document linkDoc = Jsoup.connect(urlData).get();
                            String websiteName = linkDoc.title().split(" - ")[0];

                            String ObjectID=getObjectIdForURL(urlData);
                            addToToBeCrawledLinks(urlData,websiteName,ObjectID);
                            toBeCrawled.insertOne(new Document("url", normalizer.filter(urlData)));
                        } catch (IOException e) {
                            // handle the exception
                        }
                    }
                }

                myScannerReader.close();
            } catch (FileNotFoundException ex) {
                System.out.println("An Error has Occurred During Reading URL Seed List...");
                ex.printStackTrace();
            }
        }

        public String getObjectIdForURL(String url) {
            MongoCollection<Document> collection = db.getCollection("crawledAlreadyLinks");
            BasicURLNormalizer normalizer = new BasicURLNormalizer();
            String normalizedUrl = normalizer.filter(url);
            Document query = new Document("url", normalizedUrl);
            Document result = collection.find(query).first();
            if (result != null) {
                String objectId = result.getObjectId("_id").toString();
                return objectId;
            } else {
                return null;
            }
        }
        public void addToToBeCrawledLinks(String url, String name,String pageObjectId) {
            if (url != null) {

                BasicURLNormalizer normalizer = new BasicURLNormalizer();
                String normalizedUrl = normalizer.filter(url);
                if (url.contains("http") && !toBeCrawledLinks.contains(normalizedUrl)) {
                    toBeCrawledLinks.add(normalizedUrl);
                    toBeCrawled.insertOne(new Document("url", normalizedUrl)
                            .append("name", name) .append("pageObjectId", pageObjectId));
                    System.out.println("WE JUST ADDED THIS TO THE TO BE CRAWLED LINKS"+url );
                }
            }
        }

        public String getLinkForCrawling() {
            String nextLink ;

            nextLink = toBeCrawledLinks.poll();
            if (nextLink != null) {
                toBeCrawledLinks.remove(nextLink);
                toBeCrawled.deleteOne(new Document("url", nextLink));
                crawledAlreadyLinksDB.insertOne(new Document("url", nextLink));
                crawledAlreadyLinks.add(nextLink);
                System.out.println("WE JUST CAPTURED THIS FOR CRAWLING"+nextLink );
            }

            return nextLink;
        }

    }

    public void crawl() {
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        BasicURLNormalizer normalizer = new BasicURLNormalizer();
        while (true) {
            String URL;
            synchronized (lock) {
                URL = mongoDBHandler.getLinkForCrawling();

                if (URL == null) {
                    // no more links to crawl
                    return;
                }
                crawledAlreadyHashMap.put(URL, 1);
            }

            try {
                String pageObjectId = mongoDBHandler.getObjectIdForURL(URL);
                org.jsoup.nodes.Document linkDoc = Jsoup.connect(URL).get();

                String websiteName = linkDoc.title().split(" - ")[0];

                Elements linksOnPage = linkDoc.select("a[href]");

                // process links on the page
                for (Element page : linksOnPage) {

                    String url = page.attr("abs:href");
                    String normalizedUrl = normalizer.filter(url);

                    if (url.contains("http") && !crawledAlreadyHashMap.containsKey(normalizedUrl)) {
                        if (NoOfAddedPagesAlready < NoOfCrawledPagesMax) {
                            mongoDBHandler.addToToBeCrawledLinks(url, websiteName,pageObjectId);
                            NoOfAddedPagesAlready++;
                            
                        }
                    }
                }

            } catch (IOException e) {
                // handle the exception
            }
        }
    }

    public void run() {
        crawl();
        synchronized (lock) {
            numThreadsFinished++;
        }
    }

    public static void main(String[] args) {
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        mongoDBHandler.fillSeedSet();

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new Crawler());
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Done crawling!");
    }
}