package ai.getuseful.duitbetter.service;

import ai.getuseful.duitbetter.advisors.GraphQuestionAnswerAdvisor;
import ai.getuseful.duitbetter.repository.QuestionNodeRepository;
import org.neo4j.driver.Driver;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


@Service
public class QuestionsVectorStoreService {

    @Autowired
    private QuestionNodeRepository questionNodeRepository;
    @Autowired
    private Driver driver;
    @Autowired
    private EmbeddingModel embeddingModel;
    private Neo4jVectorStore questionsVectorStore;
    @Autowired
    private ChatClient.Builder chatClientBuilder;

    public Neo4jVectorStore getQuestionsVectorStore() {
        if(questionsVectorStore == null){
            questionsVectorStore = new Neo4jVectorStore(driver, embeddingModel,
                    Neo4jVectorStore.Neo4jVectorStoreConfig.builder().withIndexName("questions-index")
                            .withLabel("Question").withEmbeddingProperty("embedding")
                            .withEmbeddingDimension(3096).build(), true);
        }
        return questionsVectorStore;
    }

    public Flux<String> answer(SearchRequest searchRequest){
        GraphQuestionAnswerAdvisor advisor = new GraphQuestionAnswerAdvisor(
                getQuestionsVectorStore(), searchRequest, questionNodeRepository);
        return chatClientBuilder.build().prompt().advisors(advisor).user(searchRequest.getQuery()).stream().content();
    }
}
