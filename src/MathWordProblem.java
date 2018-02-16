import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.List;

public class MathWordProblem {

    private List<String> statements;
    private String question;
    private String solution;

    public MathWordProblem(List<String> statements, String question, String solution) {
        this.statements = statements;
        this.question = question;
        this.solution = solution;
    }

    public String problemText() {
        return String.join(" ", statements) + " " + question;
    }

    @Override
    public String toString() {
        return "Problem: " +
                String.join(" ", statements) +
                " " + question +
                " | Solution: " + solution;
    }
}
