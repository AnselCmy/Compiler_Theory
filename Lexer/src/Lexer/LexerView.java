package Lexer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class LexerView {
    private JPanel LexerPanel;
    private JTextArea inputText;
    private JButton lexerBtn;
    private JTable outputTable;
    private JButton identifierTableBtn;
    private JButton constantTableBtn;
    private JScrollPane scrollPane;
    private JLabel inputLabel;
    private JLabel outputLabel;
    LexerScanner lexerScanner;


    public LexerView() {
        inputText.setText("if i=0 then n++;\n" +
                        "a <= 3b %);");
        // set btn
        lexerBtn.addActionListener(e -> {
            String inputString = inputText.getText();
            lexerScanner = new LexerScanner(inputString);
            lexerScanner.scan();
            setOutputTable(lexerScanner.outputList);
        });
        identifierTableBtn.addActionListener(e -> {
            String inputString = inputText.getText();
            lexerScanner = new LexerScanner(inputString);
            lexerScanner.scan();
            setTable(lexerScanner.identifier);
        });
        constantTableBtn.addActionListener(e -> {
            String inputString = inputText.getText();
            lexerScanner = new LexerScanner(inputString);
            lexerScanner.scan();
            setTable(lexerScanner.constant);
        });
        // set table
        outputTable.setShowGrid(true);
        outputTable.setGridColor(Color.BLACK);
        outputTable.setRowHeight(30);
        outputTable.setVisible(true);
        // set font
        Font font = inputText.getFont();
        Font newFont = new Font(font.getName(), font.getStyle(), 20);
        inputText.setFont(newFont);
        outputTable.setFont(newFont);
        outputTable.getTableHeader().setFont(newFont);
        inputLabel.setFont(newFont);
        outputLabel.setFont(newFont);
    }

    public void setOutputTable(ArrayList<ArrayList<String>> outputList) {
        DefaultTableModel model = new DefaultTableModel();
        for (String col : new String[]{"Word", "Binary Seq", "Type", "Position"}) {
            model.addColumn(col);
        }
        for (int i=0; i<outputList.size(); i++) {
            ArrayList<String> temp = outputList.get(i);
            model.addRow(new Object[]{temp.get(0), temp.get(1), temp.get(2), temp.get(3)});
        }
        outputTable.setModel(model);
    }

    public void setTable(ArrayList<String> list) {
        DefaultTableModel model = new DefaultTableModel();
        for (String col : new String[]{"num", "identifier"}) {
            model.addColumn(col);
        }
        for (int i=0; i<list.size(); i++) {
            model.addRow(new Object[]{String.valueOf(i), list.get(i)});
        }
        outputTable.setModel(model);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("LexerView");
        frame.setContentPane(new LexerView().LexerPanel);
        frame.getContentPane().setPreferredSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
