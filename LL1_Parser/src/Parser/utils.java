package Parser;

import java.util.*;

public class utils {
    static public String listToString(List<String> arrayList) {
        StringBuilder rst = new StringBuilder();
        for(String s : arrayList) {
            rst.append(s);
            rst.append(" ");
        }
        return rst.toString();
    }

    static public void cleanRepeat(ArrayList<String> a) {
        HashSet<String> h = new HashSet<>(a);
        a.clear();
        a.addAll(h);
    }
}
