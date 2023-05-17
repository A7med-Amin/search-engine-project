package com.database.objects;

import org.bson.types.ObjectId;

public class CrawledItem {
    
    public ObjectId _id;
    public String url;
    public String name;
    public int includedLinks;
    public String id;
    public String[] parentPages;
    public double pop;
    
    public CrawledItem(ObjectId _id, String url, String name, int includedLinks, String id, String[] parentPages, double pop) {
        this._id = _id;
        this.url = url;
        this.name = name;
        this.includedLinks = includedLinks;
        this.id = id;
        this.parentPages = parentPages;
        this.pop = pop;
    }
    
    public void set_id(ObjectId _id) {
        this._id = _id;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setIncludedLinks(int includedLinks) {
        this.includedLinks = includedLinks;
    }
    
    public void setParentPages(String[] parentPages) {
        this.parentPages = parentPages;
    }
    
    public void setPop(int pop) {
        this.pop = pop;
    }
    
    public ObjectId get_id() {
        return _id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getName() {
        return name;
    }
    
    public int getIncludedLinks() {
        return includedLinks;
    }
    
    public String[] getParentPages() {
        return parentPages;
    }
    
    public double getPop() {
        return pop;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
}