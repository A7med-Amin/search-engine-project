package com.database.objects;

public class DocumentItem {
    public String doc_id;
    public String doc_len;
    public String string;
    public double TF;
    public int[] positions;
    
    public DocumentItem(String doc_id, String doc_len, String string, double TF) {
        this.doc_id = doc_id;
        this.doc_len = doc_len;
        this.string = string;
        this.TF = TF;
    }
    
}