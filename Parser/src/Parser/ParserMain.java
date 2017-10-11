package Parser;

import javax.swing.*;

public class ParserMain {
    public static void main(String[] args) {
        ParserController controller = new ParserController();
        ParserView view = new ParserView(controller);
//        JFrame frame = new JFrame("View");
//        View view = new View(controller);
    }
}
