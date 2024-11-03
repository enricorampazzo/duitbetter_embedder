package ai.getuseful.duitbetter.embedder;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;

public class Embedder {
    @Autowired
    private EmbeddingModel embeddingModel;

}
