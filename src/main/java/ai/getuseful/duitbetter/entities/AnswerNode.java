package ai.getuseful.duitbetter.entities;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.UUID;

@Node("Answer")
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AnswerNode {
    @Id
    @GeneratedValue
    private UUID id;
    @Property
    private String text;
    @Relationship(type = "HAS_ANSWER", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private QuestionNode question;

    public AnswerNode(String text){
        this.text = text;
    }
}
