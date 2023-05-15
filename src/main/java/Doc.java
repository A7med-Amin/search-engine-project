import java.util.List;
import java.util.ArrayList;
public class Doc {
    String docId;
    int docLength;
    int tf;
    List<Integer> positions = new ArrayList<>();
    String firstOccurrence;
}
