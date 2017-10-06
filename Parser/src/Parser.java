import javax.sql.rowset.FilteredRowSet;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Parser {
    public static void main(String[] args) {
        Table table = new Table();
        table.readGrammar();
        table.mapGrammar();
        table.genFIRST();
        table.genFOLLOW();
        System.out.println(table.tableMap);
        System.out.println(table.FIRST);
        System.out.println(table.FOLLOW);
    }
}

class Table {
    HashMap<String, ArrayList<ArrayList<String>>> tableMap = new HashMap<>();
    HashMap<String, ArrayList<String>> FIRST = new HashMap<>();
    HashMap<String, ArrayList<String>> FOLLOW = new HashMap<>();
    String startCh = "";

    public ArrayList<String> readGrammar() {
        String line = "";
        ArrayList<String> grammar = new ArrayList<>();
        try {
            String fileName = "/Users/chen/Documents/curriculum/Compiler/Parser/src/Grammar";
            File file = new File(fileName);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader buffer = new BufferedReader(reader);
            // read one line from the file buffer
            line = buffer.readLine();
            while (line != null) {
                // add all line into the grammar list
                grammar.add(line);
                line = buffer.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grammar;
    }

    public void mapGrammar() {
        ArrayList<String> grammer = readGrammar();
        String[] rightLeft;
        String[] candidates;
        String[] charArray;
        ArrayList<ArrayList<String>> rightList;
        for(int i=0; i<grammer.size(); i++) {
            // split right and left by "->"
            rightLeft = grammer.get(i).split("->");
            // trim the space
            String left = rightLeft[0].trim();
            String right = rightLeft[1];
            // split different candidate
            candidates = right.split("\\|");
            rightList = new ArrayList<>();
            // split every candidate into char and add into map
            for (String candidate : candidates) {
                charArray = candidate.trim().split(" ");
                rightList.add(new ArrayList<>(Arrays.asList(charArray)));
            }
            tableMap.put(left, rightList);
            // get the start character
            if (i==0) {
                startCh = left;
            }
        }
    }

    public void genFIRST() {
        // get the key set and the as well as the nonterminal
        Set<String> keySet = tableMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            genFIRSTof(curr);
        }
    }

    public ArrayList<String> genFIRSTof(String curr) {
        if (FIRST.containsKey(curr)) {
            return FIRST.get(curr);
        }
        else {
            // allocate a new ArrayList to this nonterminal
            FIRST.put(curr, new ArrayList<>());
            // dealing with each candidate of this nonterminal by for loop
            for (ArrayList<String> candidateFormula : tableMap.get(curr)) {
                for (int i=0; i<candidateFormula.size(); i++) {
                    String currCand = candidateFormula.get(i);
                    if (isNonTerminal(currCand)) {
                        ArrayList<String> currCandFIRST = genFIRSTof(currCand);
                        // has the epsilon, find for next character
                        if (currCandFIRST.contains("epsilon")) {
                            ArrayList<String> temp = new ArrayList<>(currCandFIRST);
                            temp.remove("epsilon");
                            FIRST.get(curr).addAll(temp);
                            // means that all the candidate has epsilon as FIRST
                            if (i == candidateFormula.size()-1) {
                                FIRST.get(curr).add("epsilon");
                            }
                        }
                        // if has no epsilon, break the for loop
                        else {
                            FIRST.get(curr).addAll(currCandFIRST);
                            break;
                        }
                    }
                    // if is nonterminal or epsilon, just put into FIRST and break for loop
                    else {
                        FIRST.get(curr).add(currCand);
                        break;
                    }
                }
            }
            return FIRST.get(curr);
        }
    }

    public boolean isNonTerminal(String ch) {
        return !ch.equals("epsilon") && Character.isUpperCase(ch.charAt(0));
    }

    public ArrayList<String> genTokenStrFIRST(ArrayList<String> tokenStr) {
        ArrayList<String> tokenStrFIRST = new ArrayList<>();
        for (int i=0; i<tokenStr.size(); i++) {
            String token = tokenStr.get(i);
            if (isNonTerminal(token)) {
                ArrayList<String> currCandFIRST = genFIRSTof(token);
                if (currCandFIRST.contains("epsilon")) {
                    ArrayList<String> temp = new ArrayList<>(currCandFIRST);
                    temp.remove("epsilon");
                    tokenStrFIRST.addAll(temp);
                    // means that all the candidate has epsilon as FIRST
                    if (i == tokenStr.size()-1) {
                        tokenStrFIRST.add("epsilon");
                    }
                }
                // if has no epsilon, break the for loop
                else {
                    tokenStrFIRST.addAll(currCandFIRST);
                    break;
                }
            }
            else {
                tokenStrFIRST.add(token);
                break;
            }
        }
        return tokenStrFIRST;
    }

    public void genFOLLOW() {
        // step 1
        for (Map.Entry entry : tableMap.entrySet()) {
            String key = (String)entry.getKey();
            FOLLOW.put(key, new ArrayList<>());
            if (key.equals(startCh)) {
                FOLLOW.get(startCh).add("#");
            }
        }
        // step 2
        Set<String> keySet = tableMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            for (ArrayList<String> candidateFormula : tableMap.get(curr)) {
                for (int t=0; t<candidateFormula.size(); t++) {
                    String token = candidateFormula.get(t);
                    if (isNonTerminal(token)) {
                        // get the FIRST of last token string FIRST
                        List<String> last = candidateFormula.subList(t+1, candidateFormula.size());
                        ArrayList<String>lastFIRST = genTokenStrFIRST(new ArrayList<>(last));
                        lastFIRST.remove("epsilon");
                        FOLLOW.get(token).addAll(lastFIRST);
                    }
                }
            }
        }
        // step 3
        while (true) {
            HashMap<String, ArrayList<String>> oldFOLLOW = cloneOldFOLLOW();
            iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String curr = iterator.next();
                for (ArrayList<String> candidateFormula : tableMap.get(curr)) {
                    for (int t=0; t<candidateFormula.size(); t++) {
                        String token = candidateFormula.get(t);
                        if (isNonTerminal(token)) {
                            // this nonterminal is the last in this candidate formula
                            if (t == candidateFormula.size()-1) {
                                FOLLOW.get(token).addAll(FOLLOW.get(curr));
                            }
                            // not the last but the last token string FIRST has epsilon
                            else {
                                List<String> last = candidateFormula.subList(t+1, candidateFormula.size());
                                ArrayList<String>lastFIRST = genTokenStrFIRST(new ArrayList<>(last));
                                if (lastFIRST.contains("epsilon")) {
                                    FOLLOW.get(token).addAll(FOLLOW.get(curr));
                                }
                            }
                        }
                    }
                }
            }
            cleanRepeatInFOLLOW();
            // do while the FOLLOW is stable
            if(stableFOLLOW(oldFOLLOW)) {
                break;
            }
        }
    }

    public void cleanRepeatInFOLLOW() {
        // delete the repeated
        Set<String> keySet = tableMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            ArrayList<String> currFOLLOW = FOLLOW.get(curr);
            HashSet<String> h = new HashSet<>(currFOLLOW);
            currFOLLOW.clear();
            currFOLLOW.addAll(h);
        }
    }

    public HashMap<String, ArrayList<String>> cloneOldFOLLOW() {
        HashMap<String, ArrayList<String>> oldFollow = new HashMap<>();
        Set<String> keySet = tableMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            oldFollow.put(curr, new ArrayList<>(FOLLOW.get(curr)));
        }
        return oldFollow;
    }

    public boolean stableFOLLOW(HashMap<String, ArrayList<String>> oldFOLLOW) {
        Set<String> keySet = tableMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            if (!oldFOLLOW.get(curr).equals(FOLLOW.get(curr))) {
                return false;
            }
        }
        return true;
    }
}
