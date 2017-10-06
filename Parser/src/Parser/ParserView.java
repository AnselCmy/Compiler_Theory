package Parser;

import com.sun.xml.internal.messaging.saaj.soap.ver1_1.BodyElement1_1Impl;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class ParserView extends JFrame {
    ParserController controller;

    public ParserView(ParserController controller) {
        // controller to connect with model
        this.controller = controller;
        // declare base element of this frame
        GrammarPanel grammarPanel = new GrammarPanel();
        InputPanel inputPanel = new InputPanel();
        OutputPanel outputPanel = new OutputPanel();
        JButton confirmBtn = new JButton("Confirm");
        // add action listener
        confirmBtn.addActionListener(actionEvent -> {
            // clear the panel
            outputPanel.clear();
            // get the input and send to controller
            String inputString = inputPanel.textField.getText();
            ArrayList<ArrayList<String>> traceStack = controller.getTraceStack(inputString);
            outputPanel.displayRst(traceStack);
        });
        // declare bottoms
        JButton showFIRSTBtn = new JButton("FIRST");
        showFIRSTBtn.addActionListener(actionEvent -> {
            // clear the panel
            outputPanel.clear();
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarPanel.textArea.getText();
            HashMap<String, ArrayList<String>> FIRST = controller.getFIRST(grammarStr);
            // show FIRST
            outputPanel.showF(FIRST, "FIRST");
        });
        JButton showFOLLOWBtn = new JButton("FOLLOW");
        showFOLLOWBtn.addActionListener(actionEvent -> {
            // clear the panel
            outputPanel.clear();
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarPanel.textArea.getText();
            HashMap<String, ArrayList<String>> FOLLOW = controller.getFOLLOW(grammarStr);
            // show FIRST
            outputPanel.showF(FOLLOW, "FOLLOW");
        });
        JButton showAnalysisTableBtn = new JButton("Analysis Table");
        showAnalysisTableBtn.addActionListener(actionEvent -> {
            // clear the panel
            outputPanel.clear();
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarPanel.textArea.getText();
            HashMap<String, HashMap<String, ArrayList<String>>> analysisTabel = controller.getAnalysisTabel(grammarStr);
            // show FIRST
            outputPanel.showTable(analysisTabel);
        });
        // layout
        setLayout(new FlowLayout());
//        setLayout(new BorderLayout());
        setBounds(300, 300, 500, 500);
        // add element to frame
        getContentPane().add(grammarPanel);
        getContentPane().add(showFIRSTBtn);
        getContentPane().add(showFOLLOWBtn);
        getContentPane().add(showAnalysisTableBtn);
        getContentPane().add(inputPanel);
        getContentPane().add(confirmBtn);
        getContentPane().add(outputPanel);
        // basic setting
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

class GrammarPanel extends JPanel {
    JTextArea textArea;

    public GrammarPanel() {
        textArea = new JTextArea("E  -> T E'\n" +
                            "E' -> + T E' | epsilon\n" +
                            "T  -> F T'\n" +
                            "T' -> * F T' | epsilon\n" +
                            "F  -> ( E ) | i", 10, 40);
        add(textArea);
    }
}

class InputPanel extends JPanel {
    JTextField textField;

    public InputPanel() {
        textField = new JTextField(20);
        add(textField);
    }
}

class OutputPanel extends JPanel {
    JScrollPane scrollPane;
    JTable table;
    DefaultTableModel model;

    public OutputPanel() {
        model = new DefaultTableModel();
        table = new JTable(model);
        scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    public void displayRst(ArrayList<ArrayList<String>> rst) {
        for (String col : new String[]{"Step", "Analysis Stack", "Rest Input", "Production"}) {
            model.addColumn(col);
        }
        for(int i=0; i<rst.size(); i++) {
            ArrayList<String> temp = rst.get(i);
            temp.add(0, String.valueOf(i));
            model.addRow(new Object[]{temp.get(0), temp.get(1), temp.get(2), temp.get(3)});
        }
    }

    public void showF(HashMap<String, ArrayList<String>> F, String setType) {
        for (String col : new String[]{"Nonterminal", setType}) {
            model.addColumn(col);
        }
        Set<String> keySet = F.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            model.addRow(new Object[]{curr, utils.listToString(F.get(curr))});
        }
    }

    public void showTable(HashMap<String, HashMap<String, ArrayList<String>>> analysisTabel) {
        // find nonterminal and terminal as head of table
        ArrayList<String> terminal = new ArrayList<>();
        ArrayList<String> nonterminal = new ArrayList<>();
        Set<String> keySet = analysisTabel.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            terminal.addAll(analysisTabel.get(curr).keySet());
        }
        nonterminal.addAll(analysisTabel.keySet());
        // clean repeat
        utils.cleanRepeat(terminal);
        utils.cleanRepeat(nonterminal);
        // make table
        model.addColumn(" ");
        for (String col : terminal) {
            model.addColumn(col);
        }
        for (String rowHead : nonterminal) {
            HashMap<String, ArrayList<String>> rowTemp = analysisTabel.get(rowHead);
            ArrayList<String> rowContent = new ArrayList<>();
            rowContent.add(rowHead);
            for (String col : terminal) {
                if (rowTemp.containsKey(col)) {
                    rowContent.add(utils.listToString(rowTemp.get(col)));
                }
                else {
                    rowContent.add(" ");
                }
            }
            model.addRow(rowContent.toArray());
        }
    }

    public void clear() {
        model = new DefaultTableModel();
        table.setModel(model);
    }
}

