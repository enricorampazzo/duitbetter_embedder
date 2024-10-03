package ai.getuseful.duitbetter;

import ai.getuseful.duitbetter.entities.WebPageNode;
import ai.getuseful.duitbetter.repository.WebPageNodeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Main implements CommandLineRunner {
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private Neo4jVectorStore vectorStore;
    @Autowired
    private WebPageNodeRepository repository;
    @Autowired
    private ChatModel chatModel;
    @Autowired
    private ChatClient.Builder chatClientBuilder;
    @Override
    public void run(String... args) throws Exception {
//        performSimilaritySearch();
        autoLabelWebpages();
    }

    private void performSimilaritySearch(){
        List<Document> results = vectorStore.similaritySearch(SearchRequest.defaults().withSimilarityThreshold(0.5).withQuery("what is VoLTE?"));
        System.out.format("documents found: %d\n", results.size());
    }

    private void createEmbeddings(){
//        Pageable pageable = Pageable.ofSize(500);
//
//        Page<WebPageNode> webpagesWithoutEmbeddings = repository.findByCleanedTextIsNotNullAndEmbeddingIsNull(pageable);
//        while(!webpagesWithoutEmbeddings.isEmpty()){
//            for(WebPageNode webPageWithoutEmbedding: webpagesWithoutEmbeddings){
//                vectorStore.add(List.of(Document.builder().withContent(webPageWithoutEmbedding.getCleanedText()).withId(webPageWithoutEmbedding.getUrl()).build()));
//            }
//            pageable.next();
//            webpagesWithoutEmbeddings = repository.findByCleanedTextIsNotNullAndEmbeddingIsNull(pageable);
//        }
//        System.out.println("hello");
    }

    private void autoLabelWebpages(){
        WebPageNode page = repository.findByUrl("https://www.du.ae/personal/support/articledetail?artid=PROD-59292&lang=en-US");
        var chatClient = chatClientBuilder.build();
        ChatResponse chatResponse = chatClient.prompt()
                .system("Your are an expert at labelling documents to make them easy to retrieve when asking questions")
                .user(String.format("Return all the applicable labels for this document, separated by a semicolon, without any preamble: \n\n %s", page.getText()))
                .call()
                .chatResponse();
        System.out.println(chatResponse.getResult().getOutput().toString());
    }
}
