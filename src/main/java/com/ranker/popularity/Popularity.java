package com.ranker.popularity;

import com.database.objects.CrawledItem;
import com.database.objects.DocumentSource;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.TreeMap;

public class Popularity {
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> mockDB;
    
    public Popularity(MongoClient mongoClient, MongoDatabase db, MongoCollection<Document> mockDB) {
        this.mongoClient = mongoClient;
        this.db = db;
        this.mockDB = mockDB;
    }
    
    
    public void popularityRanking() {
        //Getting the number of documents in the database
        long dataBaseSize = mockDB.countDocuments();
        // Initiating the treemap that will hold the database items
        TreeMap<String, DocumentSource> treeMap_i = new TreeMap<>();
        TreeMap<String, DocumentSource> treeMap_ii = new TreeMap<>();
        
        //Document update = new Document("$set", new Document("pop", (float) 1 / 6001));
        //mockDB.updateMany(new Document(), update);
        
        //looping on the number of the documents in the database and store them in the treemap "i"
        for (Document document : mockDB.find().limit(6001)) {
            Gson gson = new Gson();
            CrawledItem Crawled = gson.fromJson(document.toJson(), CrawledItem.class);
            DocumentSource DocumentSource = new DocumentSource(Crawled.parentPages, Crawled.includedLinks, Crawled.pop);
            //get the treemap full of entries
            treeMap_i.put(Crawled.url, DocumentSource);
        }
        
        //number of iterations to be done
        for (int l = 0; l < 10; l++) {
            //looping on the number of the documents in the database and store them in the treemap "ii"
            for (String key : treeMap_i.keySet()) {
                //Do calculations for each document
                String parentPages[] = treeMap_i.get(key).getCameFrom();
                double newPopularity = 0;
                //This means that the document has no parent pages, and its popularity will still as it is
                if (parentPages[0] == null) treeMap_ii.put(key, treeMap_i.get(key));
                else {
                    for (int i = 0; i < parentPages.length; i++) {
                        double parentPop = treeMap_i.get(parentPages[i]).getPop();
                        int parentOutgoingLinks = treeMap_i.get(parentPages[i]).getHyperLinkCount();
                        newPopularity = newPopularity + (parentPop / parentOutgoingLinks);
                        treeMap_ii.put(key, new DocumentSource(treeMap_i.get(key).getCameFrom(), treeMap_i.get(key).getHyperLinkCount(), newPopularity));
                        mockDB.updateOne(new Document("url", key), Updates.set("pop", newPopularity));
                    }
                }
            }
        }
    }
    
    
    
}