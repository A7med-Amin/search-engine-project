package com.database.objects;

import java.util.List;
import java.util.ArrayList;
public class Word {
    public String word;
    public int df;
    public List<Doc> wordDocs = new ArrayList<>();
}