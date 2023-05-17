package com.database.objects;

import java.util.List;
import java.util.ArrayList;
public class Doc {
    public String docId;
    public String docLink;
    public int docLength;
    public int tf;
    public List<Integer> positions = new ArrayList<>();
    public String firstOccurrence;
}