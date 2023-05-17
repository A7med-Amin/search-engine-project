import org.tartarus.snowball.ext.porterStemmer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryProcessor {

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////                               Variables                                                     ////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // List of stop words to be excluded from indexer DB
    public static List<String> stopWords;

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

    // Function that process query words to match that saved by indexer
    public static List<String> getQueryWords(String query) {
        List<String> words = new ArrayList<>();
        // Read Stop words
        try {
            readStopWordsFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Get all words in the query (including stop words)
        // Convert words into lowercase to match stop words
        List<String> queryWords = splitToWords(query.toLowerCase());
        // Remove stop words
        excludeStopWords(queryWords);
        // Stem remaining words
        stemWords(queryWords);

        for (String word : queryWords) {
            // Add these fine words now to list of words
            words.add(word);
        }
        return words;
    }

//    public static void main(String[] args) throws IOException {
//        String query = "\"This tennis player is good enough to play for our team.\"";
//        List<String> output = getQueryWords(query);
//        for (String str : output) {
//            System.out.println(str);
//        }
//    }

//    // Convert the List<String> into Document
//    public static void convertMapToDoc(List<String> words) {
//        Document entryDoc = new Document();
//        entryDoc.put("queryWords", new ArrayList<>());
//        for (String word : words) {
//            Document wordDoc = new Document();
//            wordDoc.put("word", word);
//            entryDoc.getList("queryWords", Document.class).add(wordDoc);
//            dbWords.add(entryDoc);
//        }
//    }
}
