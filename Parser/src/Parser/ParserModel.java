package Parser;

import java.io.*;
import java.util.*;

public class ParserModel {
    public Grammar grammar;
    public Predictor predictor;

    public ParserModel() {
        grammar = new Grammar();
//        grammar.readGrammar();
//        grammar.mapGrammar();
//        grammar.genFIRST();
//        grammar.genFOLLOW();
//        grammar.genTable();

        predictor = new Predictor(grammar);
    }

    public static void main(String[] args) {
        ParserModel model = new ParserModel();
        model.grammar.readGrammar("E->TH\n" +
                "H->+TH|e\n" +
                "T->FI\n" +
                "I->*FI|e\n" +
                "F->(E)|i");
        model.grammar.mapGrammar();
        model.grammar.genFIRST();
        model.grammar.genFOLLOW();
        model.grammar.genTable();
        model.predictor.run("i*i+i");

    }
}


class Predictor {
    Grammar grammar;
    Stack<String> tokenStack = new Stack<>();
    ArrayList<String> inputTokenString = new ArrayList<>();
    String epsilon = "$";

    public Predictor(Grammar grammar) {
        this.grammar = grammar;
    }

    public void readTokenString(String inputString) {
        inputTokenString = new ArrayList<>();
        for(int i=0; i<inputString.length(); i++) {
            inputTokenString.add(String.valueOf(inputString.charAt(i)));
        }
        inputTokenString.add("#");
    }

    public ArrayList<ArrayList<String>> run(String inputString) {
        // read the string into ArrayList
        readTokenString(inputString);
        // initial the stack
        ArrayList<ArrayList<String>> rst = new ArrayList<>();
        tokenStack.push("#");
        tokenStack.push(grammar.startCh);
        // initial the rst
        ArrayList<String> rstEntry = new ArrayList<>();
        rstEntry.add(utils.listToString(tokenStack));
        rstEntry.add(utils.listToString(inputTokenString));
        rstEntry.add(" ");
        rst.add(rstEntry);
        int tokenPoint = 0;
        String X, a;
        // loop for analysis
        while (true) {
            // allocate the entry of one result
            rstEntry = new ArrayList<>();
            // the candidate if using candidate
            ArrayList<String> candidate = new ArrayList<>();
            X = tokenStack.pop();
            a = inputTokenString.get(tokenPoint);
            if (grammar.isTerminal(X)) {
                if (X.equals(a)) {
                    tokenPoint++;
                }
                else {
                    error();
                    break;
                }
            }
            else if (X.equals("#")) {
                if (X.equals(a)) {
                    break;
                }
                else {
                    error();
                }
            }
            else if (grammar.analysisTable.get(X).containsKey(a)) {
                candidate = grammar.analysisTable.get(X).get(a);
                if (!candidate.contains(epsilon)) {
                    for (int i = candidate.size() - 1; i >= 0; i--) {
                        tokenStack.push(candidate.get(i));
                    }
                }
            }
            else {
                error();
            }
            rstEntry.add(utils.listToString(tokenStack));
            rstEntry.add(utils.listToString(inputTokenString.subList(tokenPoint, inputTokenString.size())));
            if (candidate.size()==0)
                rstEntry.add("");
            else
                rstEntry.add(X+"->"+utils.listToString(candidate));
            rst.add(rstEntry);
        }
//        System.out.println("result here:");
//        System.out.println(inputString);
//        System.out.println(rst);
//        System.out.println("result end");
        return rst;
    }

    public void error() {
        System.out.println("ERROR");
    }
}


class Grammar {
    ArrayList<String> rawGrammar;
    HashMap<String, ArrayList<ArrayList<String>>> tableMap = new HashMap<>();
    HashMap<String, ArrayList<String>> FIRST = new HashMap<>();
    HashMap<String, ArrayList<String>> FOLLOW = new HashMap<>();
    HashMap<String, HashMap<String, ArrayList<String>>> analysisTable = new HashMap<>();
    String startCh = "";
    public String epsilon = "$";

    public void readGrammar() {
        String line = "";
        rawGrammar = new ArrayList<>();
        try {
            String fileName = "src/Parser/Grammar";
            File file = new File(fileName);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader buffer = new BufferedReader(reader);
            // read one line from the file buffer
            line = buffer.readLine();
            while (line != null) {
                // add all line into the grammar list
                rawGrammar.add(line);
                line = buffer.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readGrammar(String s) {
        rawGrammar = new ArrayList<>(Arrays.asList(s.split("\n")));
    }

    public void mapGrammar() {
        String[] rightLeft;
        String[] candidates;
        String[] charArray;
        ArrayList<ArrayList<String>> rightList;
        for(int i=0; i<rawGrammar.size(); i++) {
            // split right and left by "->"
            rightLeft = rawGrammar.get(i).split("->");
            // trim the space
            String left = rightLeft[0].trim();
            String right = rightLeft[1];
            // split different candidate
            candidates = right.split("\\|");
            rightList = new ArrayList<>();
            // split every candidate into char and add into map
//            for (String candidate : candidates) {
//                charArray = candidate.trim().split(" ");
//                rightList.add(new ArrayList<>(Arrays.asList(charArray)));
//            }
            for (String candidate : candidates) {
                ArrayList<String> temp = new ArrayList<>();
                for (int j=0; j<candidate.length(); j++) {
                    temp.add(String.valueOf(candidate.charAt(j)));
                }
                rightList.add(temp);
            }
            tableMap.put(left, rightList);
            // get the start character
            if (i==0) {
                startCh = left;
            }
        }
//        System.out.println(tableMap);
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
                        if (currCandFIRST.contains(epsilon)) {
                            ArrayList<String> temp = new ArrayList<>(currCandFIRST);
                            temp.remove(epsilon);
                            FIRST.get(curr).addAll(temp);
                            // means that all the candidate has epsilon as FIRST
                            if (i == candidateFormula.size()-1) {
                                FIRST.get(curr).add(epsilon);
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
            utils.cleanRepeat(FIRST.get(curr));
            return FIRST.get(curr);
        }
    }

    public boolean isNonTerminal(String ch) {
        return !ch.equals(epsilon) && Character.isUpperCase(ch.charAt(0));
    }

    public boolean isTerminal(String ch) {
        return !ch.equals(epsilon) && !ch.equals("#") && !isNonTerminal(ch);
    }

    public ArrayList<String> genTokenStrFIRST(ArrayList<String> tokenStr) {
        ArrayList<String> tokenStrFIRST = new ArrayList<>();
        for (int i=0; i<tokenStr.size(); i++) {
            String token = tokenStr.get(i);
            if (isNonTerminal(token)) {
                ArrayList<String> currCandFIRST = genFIRSTof(token);
                if (currCandFIRST.contains(epsilon)) {
                    ArrayList<String> temp = new ArrayList<>(currCandFIRST);
                    temp.remove(epsilon);
                    tokenStrFIRST.addAll(temp);
                    // means that all the candidate has epsilon as FIRST
                    if (i == tokenStr.size()-1) {
                        tokenStrFIRST.add(epsilon);
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
                        lastFIRST.remove(epsilon);
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
                                if (lastFIRST.contains(epsilon)) {
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

    public void genTable() {
        Set<String> keySet = tableMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            // allocate the mem for each nonterminal
            analysisTable.put(curr, new HashMap<>());
            for (ArrayList<String> candidateFormula : tableMap.get(curr)) {
                ArrayList<String> candFIRST = genTokenStrFIRST(candidateFormula);
                // step 2
                for (String ch : candFIRST) {
                    if (isTerminal(ch)) {
                        analysisTable.get(curr).put(ch, candidateFormula);
                    }
                }
                // step 3
                if (candFIRST.contains(epsilon)) {
                    for (String ch : FOLLOW.get(curr)) {
                        analysisTable.get(curr).put(ch, candidateFormula);
                    }
                }
            }
        }
    }
}
