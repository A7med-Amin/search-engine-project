package com.database.objects;
import java.util.Vector;

import java.util.Arrays;

public class OutputArray {
    
    public Vector<ChoosenDocument> choosenDocuments;
    int addedToArray = 0;
    
    public OutputArray() {
        this.choosenDocuments = new Vector<>();
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
        }
    }
    @Override
    public String toString() {
        return "OutputArray{" + "choosenDocument=" + choosenDocuments + '}';
    }
}