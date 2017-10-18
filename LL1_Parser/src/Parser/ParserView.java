package Parser;

import javax.swing.*;
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
        FIRSTPanel firstPanel = new FIRSTPanel();
        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.setPreferredSize(new Dimension(150, 50));
        // add action listener
        confirmBtn.addActionListener(actionEvent -> {
            // clear the panel
            outputPanel.clear();
            // get the input and send to controller
            String inputString = inputPanel.textField.getText();
            String grammarStr = grammarPanel.textArea.getText();
            ArrayList<ArrayList<String>> traceStack = controller.getTraceStack(grammarStr, inputString);
            outputPanel.displayRst(traceStack);
        });
        // declare bottoms
        JButton showFIRSTBtn = new JButton("FIRST");
        showFIRSTBtn.setPreferredSize(new Dimension(150, 50));
        showFIRSTBtn.addActionListener(actionEvent -> {
//            // clear the panel
            outputPanel.clear();
//            firstPanel.clear();
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarPanel.textArea.getText();
            HashMap<String, ArrayList<String>> FIRST = controller.getFIRST(grammarStr);
            // show FIRST
            outputPanel.showF(FIRST, "FIRST");
//            firstPanel.showFIRST(FIRST);
        });
        JButton showFOLLOWBtn = new JButton("FOLLOW");
        showFOLLOWBtn.setPreferredSize(new Dimension(150, 50));
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
        showAnalysisTableBtn.setPreferredSize(new Dimension(150, 50));
        showAnalysisTableBtn.addActionListener(actionEvent -> {
            // clear the panel
            outputPanel.clear();
            // put the text into grammar parser and gen FIRST
            String grammarStr = grammarPanel.textArea.getText();
            HashMap<String, HashMap<String, ArrayList<String>>> analysisTabel = controller.getAnalysisTable(grammarStr);
            // show FIRST
            outputPanel.showTable(analysisTabel);
        });
        // panel all btn
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout());
        btnPanel.add(showFIRSTBtn);
        btnPanel.add(showFOLLOWBtn);
        btnPanel.add(showAnalysisTableBtn);
        btnPanel.add(inputPanel);
        btnPanel.add(confirmBtn);
        // layout
//        setLayout(new FlowLayout());
        setLayout(new BorderLayout());
        setSize(1000, 650);
//        setBounds(300, 300, 1000, 1000);
        // add element to frame
        getContentPane().add(grammarPanel, BorderLayout.NORTH);
//        getContentPane().add(showFIRSTBtn);
//        getContentPane().add(showFOLLOWBtn);
//        getContentPane().add(showAnalysisTableBtn);
//        getContentPane().add(inputPanel, BorderLayout.CENTER);
//        getContentPane().add(confirmBtn);
        getContentPane().add(btnPanel, BorderLayout.CENTER);
        getContentPane().add(outputPanel, BorderLayout.SOUTH);
//        getContentPane().add(firstPanel, BorderLayout.SOUTH);
        // basic setting
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

class GrammarPanel extends JPanel {
    JTextArea textArea;

    public GrammarPanel() {
        textArea = new JTextArea("E->TH\n" +
                            "H->+TH|$\n" +
                            "T->FI\n" +
                            "I->*FI|$\n" +
                            "F->(E)|i", 10, 70);
        Font font = textArea.getFont();
        textArea.setFont(new Font(font.getName(), font.getStyle(), 15));
        add(textArea);
    }
}

class InputPanel extends JPanel {
    JTextField textField;

    public InputPanel() {
        textField = new JTextField("i*i+i", 50);
        // set size
        textField.setPreferredSize(new Dimension(100, 50));
        Font font = textField.getFont();
        textField.setFont(new Font(font.getName(), font.getStyle(), 15));
        add(textField);
    }
}

class FIRSTPanel extends JPanel {
    JScrollPane scrollPane;
    JTable table;
    DefaultTableModel model;

    public FIRSTPanel() {
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setGridColor(Color.BLACK);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setPreferredScrollableViewportSize(new Dimension(960, 300));
        scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    public void showFIRST(HashMap<String, ArrayList<String>> F) {
        for (String col : new String[]{"Nonterminal", "FIRST"}) {
            model.addColumn(col);
        }
        Set<String> keySet = F.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String curr = iterator.next();
            model.addRow(new Object[]{curr, utils.listToString(F.get(curr))});
        }
    }

    public void clear() {
        model = new DefaultTableModel();
        table.setModel(model);
    }
}

class OutputPanel extends JPanel {
    JScrollPane scrollPane;
    JTable table;
    DefaultTableModel model;

    public OutputPanel() {
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setGridColor(Color.BLACK);
        table.setRowHeight(30);
        table.setShowGrid(true);
        // set font
        Font font = this.getFont();
        table.getTableHeader().setFont(new Font(font.getName(), font.getStyle(), 20));
        table.setFont(new Font(font.getName(), font.getStyle(), 20));
        // set size
        table.setPreferredScrollableViewportSize(new Dimension(960, 300));
        scrollPane = new JScrollPane(table);
//        scrollPane.setLayout(new FlowLayout());
//        scrollPane.setSize(800, 500);
        add(scrollPane);
    }

    public void displayRst(ArrayList<ArrayList<String>> rst) {
        for (String col : new String[]{"Step", "Analysis Stack", "Left Input", "Production"}) {
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
        // find nonterminal and terminal as h
        // \ead of table
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

