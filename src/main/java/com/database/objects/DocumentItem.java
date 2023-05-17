package com.database.objects;

public class DocumentItem {
    public String doc_id;
    public String doc_link;
    public String doc_length;
    public double tf;
    public String first_occurrence;
    public int[] positions;
    
    public DocumentItem(String doc_id, String doc_link, String doc_length, double tf,  String first_occurrence, String string) {
        this.doc_id = doc_id;
        this.doc_link = doc_link;
        this.doc_length = doc_length;
        this.tf = tf;
        this.first_occurrence = first_occurrence;
    }
    
}