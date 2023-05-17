package com.database.objects;
import java.util.Vector;

import java.util.Arrays;

public class OutputArray {
    
    public Vector<ChoosenDocument> choosenDocuments;
    public Vector<ChoosenDocument> intersectedDocuments;
    
    int addedToArray = 0;
    
    public OutputArray() {
        this.choosenDocuments = new Vector<>();
        this.intersectedDocuments = new Vector<>();
    }
    public void AddDoucment(ChoosenDocument Doc) {
        int addedToArray = 0;
        for (int i=0; i< choosenDocuments.size(); i++) {
            //This condition is to check if the document is already in the array
            String exsistedID = choosenDocuments.get(i).id;
            String newID = Doc.id;
            if (exsistedID.equals(newID)) {
                Doc.setTF_IDF(choosenDocuments.get(i).TF_IDF + Doc.TF_IDF);
                choosenDocuments.set(i, Doc);
                addedToArray=1;
                break;
            }
        }
        //This condition is to check if the document is not in the array
        if (addedToArray==0) {
            choosenDocuments.add(Doc);
            intersectedDocuments.add(Doc);
        }
    }
    
    public void getIntersectedDocuments(ChoosenDocument Doc) {
        int addedToArray = 0;
        if (choosenDocuments.size()==0) {
            choosenDocuments.add(Doc);
            return;
        }
        for (int i=0; i< choosenDocuments.size(); i++) {
            //This condition is to check if the document is already in the array
            String exsistedID = choosenDocuments.get(i).id;
            String newID = Doc.id;
            if (exsistedID.equals(newID)) {
                choosenDocuments.get(i).availableWords.addAll(Doc.availableWords);
                intersectedDocuments.add(choosenDocuments.get(i));
                addedToArray=1;
                break;
            }
     
        }
        //This condition is to check if the document is not in the array
        if (addedToArray==0) {
        }
    }
    
    
    @Override
    public String toString() {
        return "OutputArray{" + "choosenDocument=" + choosenDocuments + '}';
    }
}