package ai.getuseful.duitbetter.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("WebPage")
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class WebPageNode {

    @Id
    @Property
    private String url;
    @Property
    private String cleanedText;
    @Property
    private String text;
    @Property
    private String title;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Relationship(type = "HAS_QUESTION", direction = Relationship.Direction.OUTGOING)
    private List<QuestionNode> questions;

}












