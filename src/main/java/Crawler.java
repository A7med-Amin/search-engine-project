import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;


import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler implements Runnable {
    //define a hash map for already crawled must not visit the same PAGE more than once "see compact string thing"
    //define a seed set fill with awel 10 zewws
    //crawl the 10 zewws, extracted hyperlinks added to the db
    //define array for links to be crawler bl tarteeb
    //handle the robo thing for security issues
    //The crawler can only crawl documents of specific types (HTML is sufficient for the project).??
    //The crawler must maintain its state so that it can, if interrupted, be started again to crawl the documents on the list without revisiting documents that have been previously downloaded.??
    //6000 max crawled links

    


    public static void main(String[] args) throws URISyntaxException, IOException {
      //maintain threading 3lshan n5ls crawling bsor3a
    }

    @Override
    public void run() {

    }
}
