
package ai.getuseful.duitbetter.vectorstore;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Values;
import org.springframework.ai.autoconfigure.vectorstore.neo4j.Neo4jVectorStoreProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.Neo4jVectorStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomEmbeddingPropertyNeo4jVectorStore extends Neo4jVectorStore {

    private final EmbeddingModel embeddingModel;
    private final Driver driver;
    private final BatchingStrategy batchingStrategy;
    private final Neo4jVectorStoreProperties vsProperties;


    public CustomEmbeddingPropertyNeo4jVectorStore(Driver driver, EmbeddingModel embeddingModel,
                                                   Neo4jVectorStoreProperties vsProperties) {
        super(driver, embeddingModel, Neo4jVectorStoreConfig.builder()
                .withEmbeddingProperty(vsProperties.getEmbeddingProperty())
                .withIndexName(vsProperties.getIndexName()).withIdProperty(vsProperties.getIdProperty())
                .withConstraintName(vsProperties.getConstraintName())
                .withDistanceType(vsProperties.getDistanceType())
                .withEmbeddingDimension(vsProperties.getEmbeddingDimension())
                .withLabel(vsProperties.getLabel())
                .withDatabaseName(vsProperties.getDatabaseName()).build(), vsProperties.isInitializeSchema());
        this.embeddingModel = embeddingModel;
        this.driver = driver;
        this.batchingStrategy = new TokenCountBatchingStrategy();
        this.vsProperties = vsProperties;
    }



    @Override
    public void doAdd(List<Document> documents) {
        embeddingModel.embed(documents, EmbeddingOptionsBuilder.builder().build(), this.batchingStrategy);

        var rows = documents.stream().map(this::documentToRecord).toList();

        try (var session = this.driver.session()) {
            var statement = """
                    	UNWIND $rows AS row
                    	MERGE (u:%s {%2$s: row.id})
                    	ON CREATE
                    		SET u += row.properties
                    	ON MATCH
                    		SET u = {}
                    		SET u.%2$s = row.id,
                    			u += row.properties
                    	WITH row, u
                    	CALL db.create.setNodeVectorProperty(u, $embeddingProperty, row[$embeddingProperty])
                    """.formatted(vsProperties.getLabel(), Optional.ofNullable(vsProperties.getIdProperty()).orElse("id"));
            session.run(statement, Map.of("rows", rows, "embeddingProperty", vsProperties.getEmbeddingProperty())).consume();
        }
    }

    private Map<String, Object> documentToRecord(Document document) {
        document.setEmbedding(document.getEmbedding());

        var row = new HashMap<String, Object>();

        row.put("id", document.getId());

        var properties = new HashMap<String, Object>();
        properties.put("text", document.getContent());

        document.getMetadata().forEach((k, v) -> properties.put("metadata." + k, Values.value(v)));
        row.put("properties", properties);

        row.put(this.vsProperties.getEmbeddingProperty(), Values.value(document.getEmbedding()));
        return row;
    }

}
