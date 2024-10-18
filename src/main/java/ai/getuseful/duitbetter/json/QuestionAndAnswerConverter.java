package ai.getuseful.duitbetter.json;

import org.springframework.ai.converter.StructuredOutputConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QuestionAndAnswerConverter implements StructuredOutputConverter<List<QuestionAndAnswer>> {
    private final Pattern questionPattern = Pattern.compile("\"question\"[^:]*:\\s*\"([^\"]*)\"");
    private final Pattern answerPattern = Pattern.compile("\"answer\"\\s*:\\s*\"([^\"]*)\"");
    @Override
    public String getFormat() {
        return """
                Convert each question and answer to JSON list using the following schema:
                [
                    {
                        "question": <the question, as a JSON string>"
                        "answer": <the answer, as a JSON string"
                    }
                ]
                Just return the JSON data, without any preamble
                """;
    }

    @Override
    public List<QuestionAndAnswer> convert(String source) {
        source = source.substring(source.indexOf("["), source.lastIndexOf("]"));
        String[] qaPairs = source.split("}\\s*,");
        List<QuestionAndAnswer> questionAndAnswers = new ArrayList<>();
        for(String qa : qaPairs) {
            var questionMatcher = questionPattern.matcher(qa);
            var answerMatcher = answerPattern.matcher(qa);
            if (!questionMatcher.find()) throw new AssertionError();
            if (!answerMatcher.find()) throw new AssertionError();
            String question = questionMatcher.group(1);
            String answer = answerMatcher.group(1);
            QuestionAndAnswer parsed = new QuestionAndAnswer();
            parsed.setQuestion(question);
            parsed.setAnswer(answer);
            questionAndAnswers.add(parsed);

        }
        return questionAndAnswers;
    }
}
