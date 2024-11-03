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
    @ToString.Exclude
    @Property(readOnly = true)
    private Object embedding;
    @Property(readOnly = true)
    @ToString.Exclude
    private Object textEmbedding3Small;
    @Relationship(type = "HAS_QUESTION", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private WebPageNode webPage;
    @Relationship(type = "HAS_ANSWER", direction = Relationship.Direction.OUTGOING)
    private AnswerNode answer;

    public QuestionNode(String text, AnswerNode answer){
        this.text = text;
        this.answer = answer;
    }
}
