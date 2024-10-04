package ai.getuseful.duitbetter.entities;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.UUID;

@Node("Question")
@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class QuestionNode {
    @Id
    @GeneratedValue
    UUID id;
    @Property
    private String text;
    @Relationship(type = "HAS_QUESTION", direction = Relationship.Direction.INCOMING)
    private WebPageNode webPage;
    @Relationship(type = "HAS_ANSWER", direction = Relationship.Direction.OUTGOING)
    private AnswerNode answer;

    public QuestionNode(String text, AnswerNode answer){
        this.text = text;
        this.answer = answer;
    }
}
