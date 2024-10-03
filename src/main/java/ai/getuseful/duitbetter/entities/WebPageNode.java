package ai.getuseful.duitbetter.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("WebPage")
@Getter
@Setter
public class WebPageNode {

    @Id
    @Property
    private String url;
    @Property(value = "cleaned_text")
    private String cleanedText;
    @Property
    private boolean addedToVectorStore;
    @Property
    private String text;
}

