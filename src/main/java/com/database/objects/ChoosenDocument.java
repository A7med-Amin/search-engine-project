package com.database.objects;

public class ChoosenDocument {
    public String id;
    public double TF;
    public double IDF;
    public double TF_IDF;
    
    public ChoosenDocument() {
        this.id = "";
        this.TF = 0;
        this.IDF = 0;
        this.TF_IDF = 0;
    }
    
    public void setTF(double TF) {
        this.TF = TF;
    }
    public void setIDF(double IDF) {
        this.IDF =  IDF;
    }
    public void setTF_IDF(double TF_IDF) {
        this.TF_IDF = TF_IDF;
    }
    public void setDoc_id(String id) {
        this.id = id;
    }
    
    public String getDoc_id() {
        return id;
    }
    public double getTF() {
        return TF;
    }
    public double getIDF() {
        return IDF;
    }
    public double getTF_IDF() {
        return TF_IDF;
    }
    @Override
    public String toString() {
        System.out.println("ChoosenDocument{" + "id='" + id + '\'' + ", TF=" + TF + ", IDF=" + IDF + ", TF_IDF=" + TF_IDF + '}');
        return "ChoosenDocument{" + "id='" + id + '\'' + ", TF=" + TF + ", IDF=" + IDF + ", TF_IDF=" + TF_IDF + '}';
    }
}