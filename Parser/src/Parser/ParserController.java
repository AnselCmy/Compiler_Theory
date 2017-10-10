package Parser;

import java.util.ArrayList;
import java.util.HashMap;

public class ParserController {
    ParserModel model;

    public ArrayList<ArrayList<String>> getTraceStack(String inputString) {
        model = new ParserModel();
        model.grammar.readGrammar();
        model.grammar.mapGrammar();
        model.grammar.genFIRST();
        model.grammar.genFOLLOW();
        model.grammar.genTable();
        return model.predictor.run(inputString);
    }

    public HashMap<String, ArrayList<String>> getFIRST(String grammarStr) {
        model = new ParserModel();
        model.grammar.readGrammar(grammarStr);
        model.grammar.mapGrammar();
        model.grammar.genFIRST();
        return model.grammar.FIRST;
    }

    public HashMap<String, ArrayList<String>> getFOLLOW(String grammarStr) {
        model = new ParserModel();
        model.grammar.readGrammar(grammarStr);
        model.grammar.mapGrammar();
        model.grammar.genFIRST();
        model.grammar.genFOLLOW();
        return model.grammar.FOLLOW;
    }

    public HashMap<String, HashMap<String, ArrayList<String>>> getAnalysisTable(String grammarStr) {
        model = new ParserModel();
        model.grammar.readGrammar(grammarStr);
        model.grammar.mapGrammar();
        model.grammar.genFIRST();
        model.grammar.genFOLLOW();
        model.grammar.genTable();
        return model.grammar.analysisTable;
    }
}
