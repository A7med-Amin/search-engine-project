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

import com.mongodb.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;

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


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                            Helper functions                                                 ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> splitToWords(String line)
    {
        List<String> Words = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(line);
        while (match.find())
        {
            String word = match.group();
            if(!word.matches("[0-9]+") && word.length() <= 20)
            {
                words.add(word);
            }
        }
        return words;
    }

    public static void readStopWordsFromFile() throws IOException
    {
        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader("stopWords.txt"));
        stopWords = new ArrayList<String>();
        while((line = reader.readLine()) != null)
        {
            stopWords.add(line);
        }
        reader.close();
    }

    public static void excludeStopWords ()
    {
        words.removeAll(stopWords);
    }

    // Function that loops on all words retrieved from the document and stem these words to its root
    public static void stemWords()
    {
        porterStemmer stemmer = new porterStemmer();
        for(int i = 0 ; i < words.size() ; i++)
        {
            stemmer.setCurrent(words.get(i));
            stemmer.stem();
            words.set(i,stemmer.getCurrent());
        }
    }


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                          Super loop of indexer                                              ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



}
