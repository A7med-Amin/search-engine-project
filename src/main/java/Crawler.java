import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.net.URL;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import com.mongodb.client.model.UpdateOptions;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.bson.Document;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import crawlercommons.filters.basic.BasicURLNormalizer;
import java.io.*;
import java.util.*;

public class Crawler implements Runnable {

    static HashMap<String, Integer> crawledAlreadyHashMap = new HashMap<String, Integer>();
    static Set<String> visitedPages = new HashSet<>();
    static Integer NoOfCrawledPagesMax = 6000;
    static Integer NoOfAddedPagesAlready = 0;
    static Queue<String> toBeCrawledLinks = new LinkedList<String>();
    static Queue<String> crawledAlreadyLinks = new LinkedList<String>();
    static int numThreads = 8;
    static int state=0;
    static int numThreadsFinished = 0;
    static Object lock = new Object();

    static class HandleMongoDB {
        private MongoClient mongoClient;
        private MongoDatabase db;
        private MongoCollection<Document> toBeCrawled;
        private MongoCollection<Document> crawledAlreadyLinksDB;
        private MongoCollection<Document> stateOfCrawler;
        private MongoCollection<Document> compactStrings;

        public HandleMongoDB() {
            this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net"));
            this.db = mongoClient.getDatabase("SampleDB");
            this.toBeCrawled = db.getCollection("toBeCrawled");
            this.crawledAlreadyLinksDB = db.getCollection("crawledAlreadyLinks");
            this.stateOfCrawler = db.getCollection("stateOfCrawler");
            this.compactStrings = db.getCollection("compactStrings");
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
        private String getPageContent(String url) {
            try {
                Connection connection = Jsoup.connect(url);
                org.jsoup.nodes.Document document = connection.get();
                return document.toString();
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 403) {
                    System.out.println("Access to page content denied: " + url);
                } else if (e.getStatusCode() == 404) {
                    System.out.println("Page not found: " + url);
                } else {
                    System.out.println("HTTP error fetching URL. Status=" + e.getStatusCode() + ", URL=" + url);
                }
                return null;
            } catch (IOException e) {
                System.out.println("Failed to retrieve page content: " + url);
                return null;
            }
        }

        public String generateCompactString(String pageContent) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte[] hash = messageDigest.digest(pageContent.getBytes(StandardCharsets.UTF_8));

                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to generate compact string", e);
            }
        }
        public void addToToBeCrawledLinks(String url, String name, String pageObjectId) throws IOException {
            if (url != null) {
                BasicURLNormalizer normalizer = new BasicURLNormalizer();
                String normalizedUrl = normalizer.filter(url);

                // Remove "www." if it exists in the URL
                normalizedUrl = normalizedUrl.replaceFirst("www.", "");
                if (toBeCrawledLinks.contains(normalizedUrl)) {
                    // Update the existing document to add the pageObjectId to the parentPages array
                    Document toBeCrawledDoc = toBeCrawled.find(new Document("url", normalizedUrl)).first();
                    List<String> parentPages = toBeCrawledDoc.getList("parentPages", String.class);
                    if (!parentPages.contains(pageObjectId)) {
                        synchronized (lock) {
                            toBeCrawled.updateOne(new Document("url", normalizedUrl), new Document("$push", new Document("parentPages", pageObjectId)));
                        }
                    }
                    return;
                }
                if (url.contains("http") && !toBeCrawledLinks.contains(normalizedUrl)&& !crawledAlreadyHashMap.containsKey(normalizedUrl)) {
                    // Generate compact string from page content
                    String pageContent = null;
                    pageContent = getPageContent(url);
                    if(pageContent==null)
                    {
                        return;
                    }
                    String compactString = generateCompactString(pageContent);

                    // Check if compact string is already in hashset
                    if (visitedPages.contains(compactString)) {
                        System.out.println("Page already visited: " + normalizedUrl);
                        return;
                    }

                    // Add normalized URL and compact string to to-be-crawled and visited hashsets
                    NoOfAddedPagesAlready++;
                    toBeCrawledLinks.add(normalizedUrl);
                    toBeCrawled.insertOne(new Document("url", normalizedUrl)
                            .append("name", name).append("includedLinks", 0) .append("parentPages",  Arrays.asList(pageObjectId)));
                    visitedPages.add(compactString);
                    compactStrings.insertOne(new Document("url", normalizedUrl).append("compactString", compactString));
                    System.out.println("WE JUST ADDED THIS TO THE TO BE CRAWLED LINKS" + normalizedUrl);
                }
            }
        }


        public void fillLists() {
            //if we got interrupted  ,so we get what's in the db before we start
            BasicURLNormalizer normalizer = new BasicURLNormalizer();
            Iterator iterator = toBeCrawled.find().iterator();
            while (iterator.hasNext()) {
                Document document = (Document) iterator.next();
                if(document.getString("url")!=null)
                {
                    toBeCrawledLinks.add(normalizer.filter(document.getString("url")));}
            }

            iterator = crawledAlreadyLinksDB.find().iterator();
            while (iterator.hasNext()) {
                Document document = (Document) iterator.next();
                crawledAlreadyLinks.add(normalizer.filter(document.getString("url")));
                crawledAlreadyHashMap.put(normalizer.filter(document.getString("url")),1);
            }
            iterator = compactStrings.find().iterator();
            while (iterator.hasNext()) {
                Document document = (Document) iterator.next();
                String compactString = document.getString("compactString");
                visitedPages.add(compactString);
            }
        }

        public String getLinkForCrawling() {
            String nextLink ;

            nextLink = toBeCrawledLinks.poll();
            if (nextLink != null) {
                toBeCrawledLinks.remove(nextLink);

                crawledAlreadyLinksDB.insertOne(new Document("url", nextLink));
                crawledAlreadyLinks.add(nextLink);
                System.out.println("WE JUST CAPTURED THIS FOR CRAWLING"+nextLink );
            }

            return nextLink;
        }
        public int getPagesAdded() {
            Document query = new Document("_id", "crawler_state");
            Document result = stateOfCrawler.find(query).first();
            if (result != null) {
                return result.getInteger("addedPages");
            } else {
                return 0;
            }
        }
        public int getState() {
            Document query = new Document("_id", "crawler_state");
            Document result = stateOfCrawler.find(query).first();
            if (result != null) {
                return result.getInteger("state");
            } else {
                return 0;
            }
        }
        public void setState(int state,int pagesAdded) {
            System.out.println("NO OF ADDED "+pagesAdded);
            Document query = new Document("_id", "crawler_state");
            Document update = new Document("$set", new Document("state", state).append("addedPages", pagesAdded));
            UpdateOptions options = new UpdateOptions().upsert(true);
            stateOfCrawler.updateOne(query, update, options);
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
                // check if URL is allowed by robots.txt
                if (!isUrlAllowedByRobots(URL)) {
                    System.out.println("WE JUST FOUND A ROBOTTXT THAT FORBIDS");
                    continue;
                }
                System.out.println("no robottxt forbidding NOW WE CRAWLING"+URL);
                org.jsoup.nodes.Document linkDoc = Jsoup.connect(URL).get();

                String websiteName = linkDoc.title().split(" - ")[0];

                Elements linksOnPage = linkDoc.select("a[href]");

                // process links on the page
                Document toBeCrawledDoc = mongoDBHandler.toBeCrawled.find(new Document("url", URL)).first();

                int linkCount = toBeCrawledDoc.getInteger("includedLinks", 0);

                for (Element page : linksOnPage) {

                    synchronized (lock) {
                        linkCount++;
                        mongoDBHandler.toBeCrawled.updateOne(new Document("url", URL), new Document("$set", new Document("includedLinks", linkCount)));
                    }
                    String url = page.attr("abs:href");


                    if (url.contains("http") &&url!=null) {
                        if (NoOfAddedPagesAlready < NoOfCrawledPagesMax) {
                            mongoDBHandler.addToToBeCrawledLinks(url, websiteName,pageObjectId);

                            System.out.println(NoOfAddedPagesAlready);
                        }
                        else{
                            System.exit(1);
                        }

                    }
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isUrlAllowedByRobots(String url) throws IOException, URISyntaxException {
        // Extract the protocol, host, and port from the URL
        URL urlObj = new URL(url);
        String hostId = urlObj.getProtocol() + "://" + urlObj.getHost() + (urlObj.getPort() != -1 ? ":" + urlObj.getPort() : "");

        // Construct the URL for the robots.txt file for the website
        String robotsUrl = hostId + "/robots.txt";

        // Download the robots.txt file if it's not directly accessible through a URL
        String robotsTxtContent;
        String contentType = urlObj.openConnection().getHeaderField("Content-Type");
        if (contentType != null && contentType.equals("text/plain")) {
            robotsTxtContent = Jsoup.connect(robotsUrl).get().toString();
        } else {
            URL robotsTxtUrl = new URL(hostId + "/robots.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(robotsTxtUrl.openStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                content.append("\n");
            }
            in.close();
            robotsTxtContent = content.toString();
        }

        // Parse the contents of the robots.txt file and return the rules for the current user agent
        BaseRobotRules rules;
        SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
        rules = robotParser.parseContent(robotsUrl, robotsTxtContent.getBytes(), "text/plain", "mycrawler");

        // Check if the input URL is allowed to be crawled based on the rules specified in the robots.txt file
        return rules.isAllowed(url);
    }
    public void run() {

        crawl();

        synchronized (lock) {
            numThreadsFinished++;
        }
    }

    public static void main(String[] args) {
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        int state = mongoDBHandler.getState();
        NoOfAddedPagesAlready=mongoDBHandler.getPagesAdded();
        if (state==0) {
            System.out.println("WE ARE FILLING THE SEAD SET DE FIRST RUN ");
            mongoDBHandler.fillSeedSet();
        }
        else{
            System.out.println("WE ARE FILLING THE LISTS AFTER BEING INTERRUPTED");
            mongoDBHandler.fillLists();
        }

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new Crawler());
        }

        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }
        // Register a function to be called when the program is terminated
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Save state or perform cleanup tasks here
                mongoDBHandler.setState(1, NoOfAddedPagesAlready);
            }
        });
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //mongoDBHandler.setState(1);
        System.out.println("Done crawling!");
}



}
