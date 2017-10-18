package Parser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class View {
    private JTextArea grammarText;
    private JButton FIRSTBtn;
    private JButton analysisTableBtn;
    private JButton confirmBtn;
    private JPanel parserPanel;
    private JTable FIRSTTable;
    private JTable FOLLOWTable;
    private JTable analysisTableTable;
    private JTable stackTable;
    private JTextField inputText;
    private JButton FOLLOWBtn;

    ParserController controller;

    public View(ParserController controller) {
        this.controller = controller;

        grammarText.setText("E->TH\n" +
                "H->+TH|$\n" +
                "T->FI\n" +
                "I->*FI|$\n" +
                "F->(E)|i");

        inputText.setText("i*i+i");

        FIRSTBtn.addActionListener(actionEvent -> {
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarText.getText();
            HashMap<String, ArrayList<String>> FIRST = controller.getFIRST(grammarStr);
            // show FIRST
            showFIRSTTable(FIRST);
        });

        FOLLOWBtn.addActionListener(actionEvent -> {
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarText.getText();
            HashMap<String, ArrayList<String>> FOLLOW = controller.getFOLLOW(grammarStr);
            // show FIRST
            showFOLLOWTable(FOLLOW);
        });

        analysisTableBtn.addActionListener(actionEvent -> {
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarText.getText();
            HashMap<String, HashMap<String, ArrayList<String>>> analysisTable
                    = controller.getAnalysisTable(grammarStr);
            // show FIRST
            showAnalysisTable(analysisTable);
        });

        confirmBtn.addActionListener(actionEvent -> {
            // get the input and send to controller
            String inputString = inputText.getText();
            String grammarStr = grammarText.getText();
            ArrayList<ArrayList<String>> traceStack = controller.getTraceStack(grammarStr, inputString);
            showStack(traceStack);
        });
        // set grid
        FIRSTTable.setGridColor(Color.black);
        FOLLOWTable.setGridColor(Color.black);
        analysisTableTable.setGridColor(Color.black);
        stackTable.setGridColor(Color.black);
        // set font
        Font font = FIRSTTable.getFont();
        Font newFont = new Font(font.getName(), font.getStyle(), 15);
        for(JTable table : new JTable[]{FIRSTTable, FOLLOWTable, analysisTableTable, stackTable}) {
            table.setRowHeight(25);
            table.getTableHeader().setFont(newFont);
            table.setFont(newFont);
        }
        grammarText.setFont(newFont);
        inputText.setFont(newFont);
    }

    void showStack(ArrayList<ArrayList<String>> traceStack) {
        DefaultTableModel model = new DefaultTableModel();

        for (String col : new String[]{"Step", "Analysis Stack", "Left Input", "Production"}) {
            model.addColumn(col);
        }
        for(int i=0; i<traceStack.size(); i++) {
            ArrayList<String> temp = traceStack.get(i);
            temp.add(0, String.valueOf(i));
            model.addRow(new Object[]{temp.get(0), temp.get(1), temp.get(2), temp.get(3)});
        }
        stackTable.setModel(model);
    }

    void showFIRSTTable(HashMap<String, ArrayList<String>> FIRST) {
        DefaultTableModel model = new DefaultTableModel();
        for (String col : new String[]{"Nonterminal", "FIRST"}) {
            model.addColumn(col);
        }
        Set<String> keySet = FIRST.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            model.addRow(new Object[]{curr, utils.listToString(FIRST.get(curr))});
        }
        FIRSTTable.setModel(model);
    }

    void showFOLLOWTable(HashMap<String, ArrayList<String>> FOLLOW) {
        DefaultTableModel model = new DefaultTableModel();
        for (String col : new String[]{"Nonterminal", "FOLLOW"}) {
            model.addColumn(col);
        }
        Set<String> keySet = FOLLOW.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            model.addRow(new Object[]{curr, utils.listToString(FOLLOW.get(curr))});
        }
        FOLLOWTable.setModel(model);
    }

    void showAnalysisTable(HashMap<String, HashMap<String, ArrayList<String>>> analysisTable) {
        DefaultTableModel model = new DefaultTableModel();

        ArrayList<String> terminal = new ArrayList<>();
        ArrayList<String> nonterminal = new ArrayList<>();
        Set<String> keySet = analysisTable.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            terminal.addAll(analysisTable.get(curr).keySet());
        }
        nonterminal.addAll(analysisTable.keySet());
        // clean repeat
        utils.cleanRepeat(terminal);
        utils.cleanRepeat(nonterminal);
        // make table
        model.addColumn(" ");
        for (String col : terminal) {
            model.addColumn(col);
        }
        for (String rowHead : nonterminal) {
            HashMap<String, ArrayList<String>> rowTemp = analysisTable.get(rowHead);
            ArrayList<String> rowContent = new ArrayList<>();
            rowContent.add(rowHead);
            for (String col : terminal) {
                if (rowTemp.containsKey(col)) {
                    rowContent.add(rowHead+"->"+utils.listToString(rowTemp.get(col)));
                }
                else {
                    rowContent.add(" ");
                }
            }
            model.addRow(rowContent.toArray());
        }
        analysisTableTable.setModel(model);
    }

    public static void main(String[] args) {
        ParserController controller = new ParserController();

        JFrame frame = new JFrame("View");
        frame.setContentPane(new View(controller).parserPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
