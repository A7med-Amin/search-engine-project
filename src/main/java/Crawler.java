import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import java.io.*;
import java.util.*;


public class Crawler implements Runnable {
    // define a hash map for already crawled must not visit the same PAGE more than once "see compact string thing"
    // define a seed set fill with awel 10 zewws
    // crawl the 10 zewws, extracted hyperlinks added to the db
    // define array for links to be crawler bl tarteeb
    // handle the robo thing for security issues
    // The crawler can only crawl documents of specific types (HTML is sufficient for the project).??
    // The crawler must maintain its state so that it can, if interrupted, be started again to crawl the documents on the list without revisiting documents that have been previously downloaded.??
    // 6000 max crawled links

    static HashMap<String, Integer> crawledAlready = new HashMap<String, Integer>(); // hashmap to store crawled link to avoid redoing them when coming from db
    static Integer NoOfCrawledPagesMax = 6000; // max no of to be crawled pages
    static Integer NoOfCrawledPagesAlready = 0; // max no of to be crawled pages
    static Queue<String> toBeCrawledLinks = new LinkedList<String>(); // array of to be crawled links FIFO

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
                    if (urlData.contains("http") && !toBeCrawledLinks.contains(urlData)// for some reason links still get added more than once

                    ) {
                        addToToBeCrawledLinks(urlData);
                        toBeCrawled.insertOne(new Document("url", normalizer.filter(urlData)));
                    }
                }

                myScannerReader.close();
            } catch (FileNotFoundException ex) {
                System.out.println("An Error has Occurred During Reading URL Seed List...");
                ex.printStackTrace();
            }
        }

        public boolean hashed(String url) {
            return true;
        }

        public void addToToBeCrawledLinks(String url) {
            BasicURLNormalizer normalizer = new BasicURLNormalizer();
            String normalizedUrl = normalizer.filter(url);
            if (url.contains("http") && !toBeCrawledLinks.contains(normalizedUrl)) {
                toBeCrawledLinks.add(normalizedUrl);
                toBeCrawled.insertOne(new Document("url", normalizedUrl));
            }

        }

        public String getLinkForCrawling() {
            String nextLink = toBeCrawledLinks.poll();
            if (nextLink != null) {
                toBeCrawledLinks.remove(nextLink);
                toBeCrawled.deleteOne(new Document("url", nextLink));
                crawledAlreadyLinksDB.insertOne(new Document("url", nextLink));
                return nextLink;
            } else {
                // the queue is empty
                return null;
            }
        }

    }

    public void crawl() {

        // check fi we reached the max for crawling
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        while (NoOfCrawledPagesAlready < NoOfCrawledPagesMax && !toBeCrawledLinks.isEmpty()) {
            String URL = mongoDBHandler.getLinkForCrawling();
            System.out.println("the link we curretnly crawling" + URL);
            if (URL != null) {
                try {
                    org.jsoup.nodes.Document doc = Jsoup.connect(URL).get();
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        String href = link.attr("href");
                        if (!toBeCrawledLinks.contains(href)) {
                            mongoDBHandler.addToToBeCrawledLinks(href);
                        }
                    }
                    NoOfCrawledPagesAlready++;
                    System.out.println("NO OF CRALWED"+NoOfCrawledPagesAlready);
                } catch (IOException e) {
                    // handle the exception
                }
            }
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        // maintain threading 3lshan n5ls crawling bsor3a
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        mongoDBHandler.fillSeedSet();

        int numThreads = 4; // default number of threads
        if (args.length > 0) {
            numThreads = Integer.parseInt(args[0]);
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.execute(new Crawler());
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }

    @Override
    public void run() {
        crawl();
    }
}