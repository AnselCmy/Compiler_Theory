package Parser;

public class ParserMain {
    public static void main(String[] args) {
        ParserModel model = new ParserModel();
        ParserController controller = new ParserController();
        ParserView view = new ParserView(controller);
    }
}
