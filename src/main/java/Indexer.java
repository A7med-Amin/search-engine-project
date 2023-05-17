//package main.java;

import java.io.IOException;
import java.util.HashMap;


import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.bson.Document;

import org.tartarus.snowball.ext.porterStemmer;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.javatuples.Triplet;

public class Indexer implements Runnable {
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                               Variables                                                     ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Number of threads
    static int numThreads = 10;
    // Finished Threads
    static int numThreadsFinished = 0;
    // Mutex to lock with it
    static Object lock = new Object();

    // Links & Ids of docs that is crawled already and saved in DB
    static List<Document> toBeCrawledDocs = new ArrayList<>();
    // List of stop words to be excluded from indexer DB
    public static List<String> stopWords;
    // HashMap that contains entry to DB
    public static HashMap<String, Word> dbEntries = new HashMap<>();
    // Documents to be added into DB
    public static List<Document> dbDocuments = new ArrayList<>();

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                            Helper functions                                                 ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> splitToWords(String elemntString) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(elemntString);
        while (match.find()) {
            String word = match.group();
            if (!word.matches("[0-9]+") && word.length() <= 20) {
                words.add(word);
            }
        }
        return words;
    }

    public static void readStopWordsFromFile() throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader("stopWords.txt"));
        stopWords = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            stopWords.add(line);
        }
        reader.close();
    }

    public static void excludeStopWords(List<String> listWithStopWords) {
        listWithStopWords.removeAll(stopWords);
    }

    // Function that loops on all words retrieved from the document and stem these words to its root
    public static void stemWords(List<String> listOfWordsToSteam) {
        porterStemmer stemmer = new porterStemmer();
        for (int i = 0; i < listOfWordsToSteam.size(); i++) {
            stemmer.setCurrent(listOfWordsToSteam.get(i));
            stemmer.stem();
            listOfWordsToSteam.set(i, stemmer.getCurrent());
        }
    }

    // Function get all elements in html document then exclude elements with children
    public static void getElementsWithoutChildren(org.jsoup.nodes.Document document, List<Element> actualElements) {
        if (document == null) {
            return;
        }
        Elements elements = document.getAllElements();

        // Remove elements that contain children
        for (Element element : elements) {
            String text = element.text();
            String tag = element.tagName();
            if ((element.children().isEmpty()) && (tag.equals("span") || tag.equals("title") || tag.equals("h1") || tag.equals("h2") || tag.equals("h3") || tag.equals("h4") || tag.equals("h5") || tag.equals("h6") || tag.equals("p") || tag.equals("td") || tag.equals("th") || tag.equals("li"))) {
                // add only tags that has text in it and ignore empty tags
                if (!text.isEmpty()) {
                    actualElements.add(element);
                }
            }
        }
    }

    // Function that get all words in a doc
    public static void getDocWords(List<Triplet<String, String, Integer>> words, List<Element> actualElements) {
        // Position of word in the doc
        Integer pos = 0;
        for (Element element : actualElements) {
            // Get element text as string
            String elementStr = element.text();
            // Get all words in the doc (including stop words)
            // Convert words into lowercase to match stop words
            List<String> allDocWords = splitToWords(elementStr.toLowerCase());
            // Remove stop words
            excludeStopWords(allDocWords);
            // Stem remaining words
            stemWords(allDocWords);

            // Add these fine words now to list of words
            for (String str : allDocWords) {
                words.add(Triplet.with(str, elementStr, pos));
                ++pos;
            }
        }
    }

    // Function to Read Crawled links from mongoDB toBeCrawled collection and save it in List<Document>
    public static void readCrawledLinks() {
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        mongoDBHandler.getCrawledDocs(toBeCrawledDocs);
    }

    // Insert doc words into hash map
    public static void insertWordsToHashMap(List<Triplet<String, String, Integer>> words, Document doc) {
        // Loop on words of the document and Put in hash-map
        for (Triplet x : words) {
            // New word not exist in DB before
            if (!dbEntries.containsKey(x.getValue0())) {
                Word wordInDoc = new Word();
                Doc wordDocs = new Doc();

                // insert in Word class
                wordInDoc.word = (x.getValue0().toString());
                ++wordInDoc.df;

                // insert to Doc class
                wordDocs.docId = doc.getObjectId("_id").toString();
                wordDocs.docLink = doc.getString("url");
                wordDocs.docLength = words.size();
                ++wordDocs.tf;
                wordDocs.positions.add((Integer) x.getValue2());
                wordDocs.firstOccurrence = x.getValue1().toString();
                wordInDoc.wordDocs.add(wordDocs);
                dbEntries.put(x.getValue0().toString(), wordInDoc);

            }
            // word exists in DB before
            else {
                boolean urlFound = false;
                Word wordInDoc = dbEntries.get(x.getValue0().toString());
                List<Doc> wordDocsList = wordInDoc.wordDocs;
                for (Doc document : wordDocsList) {
                    // word found in a doc exist in it before
                    if (document.docId.equals(doc.getObjectId("_id").toString())) {
                        urlFound = true;
                        ++document.tf;
                        document.positions.add((Integer) x.getValue2());
                        wordInDoc.wordDocs = wordDocsList;
                        dbEntries.put(x.getValue0().toString(), wordInDoc);
                        break;
                    }
                }
                // Word is in DB but in different docs
                if (!urlFound) {
                    Doc wordDocs = new Doc();
                    ++wordInDoc.df;
                    ++wordDocs.tf;
                    wordDocs.docId = doc.getObjectId("_id").toString();
                    wordDocs.docLink = doc.getString("url");
                    wordDocs.docLength = words.size();
                    wordDocs.positions.add((Integer) x.getValue2());
                    wordDocs.firstOccurrence = x.getValue1().toString();
                    wordInDoc.wordDocs.add(wordDocs);
                    dbEntries.put(x.getValue0().toString(), wordInDoc);
                }
            }
        }
    }

    // Convert the hash-map into Document
    public static void convertMapToDoc() {
        for (Map.Entry<String, Word> dbEntry : dbEntries.entrySet()) {
            Word word = dbEntry.getValue();
            Document entryDoc = new Document();
            entryDoc.put("word", word.word);
            entryDoc.put("df", word.df);
            entryDoc.put("documents", new ArrayList<>());

            for (Doc docs : word.wordDocs) {
                Document docsListData = new Document();
                docsListData.put("doc_id", docs.docId);
                docsListData.put("doc_link", docs.docLink);
                docsListData.put("doc_length", docs.docLength);
                docsListData.put("tf", docs.tf);
                docsListData.put("first_occurrence", docs.firstOccurrence);
                docsListData.put("positions", new ArrayList<>());

                for (Integer pos : docs.positions) {
                    Document positions = new Document();
                    positions.put("position ", pos);
                    docsListData.getList("positions", Document.class).add(positions);
                }
                entryDoc.getList("documents", Document.class).add(docsListData);
            }
            dbDocuments.add(entryDoc);
        }
    }

    // Function index which is executed by each thread
    public static void indexing() {
        // Loop on all docs in list of docs that is already crawled and index words in each doc
        int threadIndex = Integer.parseInt(Thread.currentThread().getName());
        int chunk = (toBeCrawledDocs.size() / numThreads);
        int remainder = (toBeCrawledDocs.size() % numThreads);
        int start = threadIndex * chunk;
        int end = start + chunk;
        if (threadIndex == numThreads - 1) {
            end += remainder;
        }

        for (int i = start; i < end; i++) {
            System.out.println(i);
            System.out.println(toBeCrawledDocs.get(i).getObjectId("_id").toString());
            System.out.println(toBeCrawledDocs.get(i).getString("url"));

            // List contains all words of a document in first place of hash-map
            // Each Entry in List:
            //              word
            //              first occurrence of each word
            //              position
            List<Triplet<String, String, Integer>> words = new ArrayList<>();

            // List of actual Elements that has no child in tags
            List<Element> actualElements = new ArrayList<>();

            // Get the document html elements
            org.jsoup.nodes.Document document;
            try {
                document = Jsoup.connect(toBeCrawledDocs.get(i).getString("url")).get();
                // Get actual elements that have no children
                getElementsWithoutChildren(document, actualElements);
                // Get doc words after remove stop words / stemming / convert to lower-case
                getDocWords(words, actualElements);
                // Get data to be added to hash map entry
                synchronized (lock) {
                    insertWordsToHashMap(words, toBeCrawledDocs.get(i));
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("This link Fails");
            }
        }
    }

    // Run function to be executed by each thread
    public void run() {
        indexing();
        synchronized (lock) {
            numThreadsFinished++;
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                                 Main Function                                               ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws IOException {

        // DB object to access the mongoDB from it
        HandleMongoDB mongoDBHandler = new HandleMongoDB();

        // Read stop words from file containing all stop words and save it in an array
        readStopWordsFromFile();

        // Get all crawled docs data {id & url} form mongoDB and save it in list of docs
        readCrawledLinks();

        // Start threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new Indexer());
            threads[i].setName(String.valueOf(i));
            threads[i].start();
        }

        // Wait for all threads to finish their work
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Convert the hashmap into Document
        convertMapToDoc();

        System.out.println(dbDocuments.size());
        System.out.println("Finished");

        // Insert to DB
        mongoDBHandler.insertWordsIntoDb(dbDocuments);
    }
}
