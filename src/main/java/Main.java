package main.java;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Main {
    public static void main(String[] args)
    {
        MongoClient client = MongoClients.create("mongodb+srv://Ahmed:n5XPwrcZ3ELSDoJ3@cluster0.oykbykb.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase db = client.getDatabase("SampleDB");
        MongoCollection col = db.getCollection("SampleCollection");
        Document sampleDoc2 = new Document("_id" , "7").append("name" , "Ahmed Ameen");
        col.insertOne(sampleDoc2);
}
}