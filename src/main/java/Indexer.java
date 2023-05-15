//package main.java;

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
import org.bson.conversions.Bson;

import org.tartarus.snowball.ext.porterStemmer;

//import com.mongodb.*;
//import com.mongodb.client.model.UpdateOptions;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.model.Filters;
//import com.mongodb.client.model.Updates;
//import com.mongodb.client.model.UpdateOptions;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Indexer {
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                               Variables                                                     ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Array that has all doc links read from crawler DB
    public static List<Document> docs = new ArrayList<>();
    // List of stop words to be excluded from indexer DB
    public static List<String> stopWords;
    // HashMap that contains entry to DB
    public static HashMap<String, Word> dbEntry = new HashMap();
    // List of actual Elements that has no child in tags
    public static List<Element> actualElements;
    // List of all words exists in 1 document
    public static List<String> words;
    // Links & Ids of docs that is crawled already and saved in DB
    static List<Document> toBeCrawledDocs = new ArrayList<>();

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                            Helper functions                                                 ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> splitToWords(String elemntString) {
        List<String> Words = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(elemntString);
        while (match.find()) {
            String word = match.group();
            if (!word.matches("[0-9]+") && word.length() <= 20) {
                Words.add(word);
            }
        }
        return Words;
    }

    public static void readStopWordsFromFile() throws IOException {
        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader("stopWords.txt"));
        stopWords = new ArrayList<String>();
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
    public static void getElementsWithoutChildren(org.jsoup.nodes.Document document) {
        actualElements = new ArrayList<Element>();
        // Get all elements <tags> in doc
        Elements elements = document.select("*");
        // Remove elements that contain children
        for (Element element : elements) {
            if (element.children().isEmpty()) {
                // add only tags that has text in it and ignore empty tags
                if (!element.text().isEmpty()) {
                    actualElements.add(element);
                }
            }
        }
    }

    // Function that get all words in a doc
    public static void getDocWords() {
        words = new ArrayList<String>();
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
                //convert it to lower case
                words.add(str);
            }
        }
    }

    // Function to Read Crawled links from mongoDB toBeCrawled collection and save it in List<Document>
    public static void readCrawledLinks() {
        HandleMongoDB mongoDBHandler = new HandleMongoDB();
        mongoDBHandler.getCrawledDocs(toBeCrawledDocs);
        String id = toBeCrawledDocs.get(0).getObjectId("_id").toString();
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                                 Main Function                                               ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws IOException {

        // Read stop words from file containing all stop words and save it in an array
        readStopWordsFromFile();

        // Get all crawled docs data {id & url} form mongoDB and save it in list of docs
        readCrawledLinks();

        // Loop on all docs in list of docs that is already crawled and index words in each doc
        for (int i = 0; i < 1; i++) {
            // Get the document html elements
            org.jsoup.nodes.Document document = Jsoup.connect(toBeCrawledDocs.get(i).getString("url").toString()).get();
            // Get actual elements that have no children
            getElementsWithoutChildren(document);
            // Get doc words after remove stop words / stemming / convert to lower-case
            getDocWords();

            //Get number of words in a document
            //Get words in doc
//            String htmlText = document.body().text();
//            String[] htmlWords = htmlText.split("\\s+");
//            System.out.println(htmlWords.length);
//            System.out.println(toBeCrawledDocs.get(i).getString("url").toString());
            ///////////////////////////////////////////////////////////////////////////

        }
    }

}
