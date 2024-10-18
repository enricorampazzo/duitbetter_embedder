package ai.getuseful.duitbetter.service;

import ai.getuseful.duitbetter.advisors.GraphQuestionAnswerAdvisor;
import ai.getuseful.duitbetter.dto.GeneratedAnswer;
import ai.getuseful.duitbetter.dto.Source;
import ai.getuseful.duitbetter.repository.QuestionNodeRepository;
import org.neo4j.driver.Driver;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


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

    public GeneratedAnswer answer(SearchRequest searchRequest){
        GraphQuestionAnswerAdvisor advisor = new GraphQuestionAnswerAdvisor(
                getQuestionsVectorStore(), searchRequest, questionNodeRepository);
        String answer = chatClientBuilder.build().prompt().advisors(advisor).user(searchRequest.getQuery()).stream()
                .content().collectList().block().stream().collect(Collectors.joining());
        return new GeneratedAnswer(answer, advisor.getDocuments().stream().map(d -> new Source(
                (String) d.getMetadata().get("url"),
                (String) d.getMetadata().get("title"))).toList());

    }

}
