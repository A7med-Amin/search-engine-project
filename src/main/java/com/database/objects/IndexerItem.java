package com.database.objects;
import org.bson.types.ObjectId;

public class IndexerItem {
    public ObjectId _id;
    public String id;
    public double DF;
    public DocumentItem[] documents;
    
    public IndexerItem(ObjectId _id, String id, double DF, DocumentItem[] documentItem) {
        this._id = _id;
        this.id = id;
        this.DF = DF;
        this.documents = documentItem;
    }
}