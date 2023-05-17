package com.database.objects;
import org.bson.types.ObjectId;

public class IndexerItem {
    public ObjectId _id;
    public String word;
    public double df;
    public DocumentItem[] documents;
    
    public IndexerItem(ObjectId _id, String word, double df, DocumentItem[] documentItem) {
        this._id = _id;
        this.word = word;
        this.df = df;
        this.documents = documentItem;
    }
}