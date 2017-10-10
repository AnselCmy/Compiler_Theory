package Lexer;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static void main(String[] args) {
        LexerScanner lexerScanner = new LexerScanner("if i=0 then n++;\n" +
                "a <= 3b %);");
        lexerScanner.scan();
//        System.out.println(lexerScanner.outputList);
    }
}

class LexerScanner {
    // the buffer of reading file
    BufferedReader buffer;
    // to indicate the next scan start
    char start;
    // table
    ArrayList<String> delimiter = new ArrayList<>(Arrays.asList(",", ";", "(", ")", "[", "]"));
    ArrayList<String> arithmetic = new ArrayList<>(Arrays.asList("+", "-", "*", "/"));
    ArrayList<String> relational = new ArrayList<>(Arrays.asList("<", "<=", "=", ">", ">=", "<>"));
    ArrayList<String> keyWord = new ArrayList<>(Arrays.asList("do", "end", "for", "if", "printf", "scanf", "then", "while"));
    ArrayList<String> identifier = new ArrayList<>();
    ArrayList<String> constant = new ArrayList<>();
    // string point
    int sp = 0;
    // program
    String program = "";
    // record the out put table
    ArrayList<ArrayList<String>> outputList = new ArrayList<>();

    LexerScanner() {
        initBuffer();
        start = getChar();
    }

    LexerScanner(String s) {
        program = s;
        start = getChar();
    }

    void initBuffer() {
        try {
            String fileName =  System.getProperty("user.dir") + "/src/Lexer/test.c";
            File file = new File(fileName);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            buffer = new BufferedReader(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    char getChar() {
//        int tempChar = -1;
//        try {
//            tempChar = buffer.read();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return (char)tempChar;
        char c;
        try {
            c = program.charAt(sp);
            sp++;
        } catch (Exception e) {
            c = (char)-1;
        }
        return c;
    }

    void scan() {
        char c = start;
        int[] pos = {1, 0};
        String string;
        while (c != (char)-1) {
            // digit
            if (Character.isDigit(c)) {
                // add the position
                pos[1]++;
                // get the digit string and shift c to start;
                string = scanType(c, "Digit");
                c = start;
                // check if has letter in digit
                if (hasLetter(string)) {
                    throwError(string, pos);
                    continue;
                }
                // check constant table and print
                if (!constant.contains(string)) {
                    constant.add(string);
                }
                formatOut(string, 5, pos);
            }
            // letter
            else if (Character.isLetter(c)) {
                pos[1]++;
                string = scanType(c, "Letter");
                c = start;
                // check if is key word
                if (isKeyWord(string)) {
                    formatOut(string, 1, pos);
                } else {
                    // check identifier table and print
                    if (!identifier.contains(string)) {
                        identifier.add(string);
                    }
                    formatOut(string, 6, pos);
                }
            }
            // symbol
            else if (isDelimiter(String.valueOf(c))) {
                pos[1]++;
                string = scanType(c, "Delimiter");
                c = start;
                if (isDelimiter(string)) {
                    formatOut(string, 2, pos);
                }
                else {
                   throwError(string, pos);
                }
            }
            else if (isArithmetic(String.valueOf(c))) {
                pos[1]++;
                string = scanType(c, "Arithmetic");
                c = start;
                if (isArithmetic(string)) {
                    formatOut(string, 3, pos);
                }
                else {
                    throwError(string, pos);
                }
            }
            else if (isRelational(String.valueOf(c))) {
                pos[1]++;
                string = scanType(c, "Relational");
                c = start;
                if (isRelational(string)) {
                    formatOut(string, 4, pos);
                }
                else {
                    throwError(string, pos);
                }
            }
            // new line
            else if (c == '\n') {
                pos[0]++;
                pos[1] = 0;
                c = getChar();
            }
            // space
            else if (c == ' ') {
                c = getChar();
            }
            else {
                pos[1]++;
                throwError(String.valueOf(c), pos);
                c = getChar();
            }
        }
    }

    String scanType(char from, String type) {
        StringBuilder string = new StringBuilder();
        string.append(from);
        char c = getChar();
        // get char till not this type
        while (c != (char)-1) {
            if (type.equals("Digit") && (!Character.isDigit(c) && !Character.isLetter(c))) {
                break;
            }
            else if (type.equals("Letter") && !Character.isLetter(c)) {
                break;
            }
            else if (type.equals("Delimiter")) {
                break;
            }
            else if (type.equals("Arithmetic") && !isArithmetic(String.valueOf(c))) {
                break;
            }
            else if (type.equals("Relational") && !isRelational(String.valueOf(c))) {
                break;
            }
            string.append(c);
            c = getChar();
        }
        // shift the start point
        start = c;
        // return the digit scanned
        return string.toString();
    }

    boolean isSymbol(char ch) {
        String s = String.valueOf(ch);
        return arithmetic.contains(s) || relational.contains(s) || delimiter.contains(s);
    }

    boolean isKeyWord(String string) {
        return keyWord.contains(string);
    }

    boolean isArithmetic(String string) {
        return arithmetic.contains(string);
    }

    boolean isRelational(String string) {
        return relational.contains(string);
    }

    boolean isDelimiter(String string) {
        return delimiter.contains(string);
    }

    void throwError(String word, int[] pos){
        System.out.printf("%s\t%s\t%s\t(%d, %d)\n", word, "ERROR", "ERROR", pos[0], pos[1]);
        outputList.add(new ArrayList<>(Arrays.asList(word, "ERROR", "ERROR", "("+String.valueOf(pos[0])+", "+String.valueOf(pos[1])+")")));
    }

    void formatOut(String word, int type, int[] pos) {
        String typeName = getTypeName(type);
        System.out.printf("%s\t(%d, %s)\t%s\t(%d, %d)\n", word, type, word, typeName, pos[0], pos[1]);
        outputList.add(new ArrayList<>(Arrays.asList(word, "("+type+", "+word+")", typeName, "("+String.valueOf(pos[0])+", "+String.valueOf(pos[1])+")")));
    }

    String getTypeName(int type) {
        String typeName = "ERROR";
        switch (type) {
            case 1:
                typeName = "Key_Word";
                break;
            case 2:
                typeName = "Delimiter";
                break;
            case 3:
                typeName = "Arithmetic_Operator";
                break;
            case 4:
                typeName = "Relational_Operator";
                break;
            case 5:
                typeName = "Constant";
                break;
            case 6:
                typeName = "Identifier";
                break;
        }
        return typeName;
    }

    boolean hasLetter(String s) {
        String regex = ".*[a-zA-Z]+.*";
        Matcher m = Pattern.compile(regex).matcher(s);
        return m.matches();
    }
}
