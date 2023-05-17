package com.database.objects;
import java.util.Vector;

public class DocumentSource {
    public  String[] cameFrom;
    public  int hyperLinkCount;
    public  double pop;
    
    public DocumentSource() {
        this.cameFrom = null;
        this.hyperLinkCount = 0;
    }
    
    public DocumentSource(String[] cameFrom, int hyperLinkCount, double pop) {
        this.cameFrom = cameFrom;
        this.hyperLinkCount = hyperLinkCount;
        this.pop = pop;
    }
    
    public void setCameFrom(String[] cameFrom) {
        this.cameFrom = cameFrom;
    }
    public void setHyperLinkCount(int hyperLinkCount) {
        this.hyperLinkCount = hyperLinkCount;
    }
    public String[] getCameFrom() {
        return cameFrom;
    }
    public int getHyperLinkCount() {
        return hyperLinkCount;
    }
    public void setPop(int pop) {
        this.pop = pop;
    }
    
    public double getPop() {
        return pop;
    }
}