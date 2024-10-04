package ai.getuseful.duitbetter;

import ai.getuseful.duitbetter.entities.AnswerNode;
import ai.getuseful.duitbetter.entities.QuestionNode;
import ai.getuseful.duitbetter.entities.WebPageNode;
import ai.getuseful.duitbetter.json.QuestionAndAnswer;
import ai.getuseful.duitbetter.repository.WebPageNodeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

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
        extractQuestionAnswerPairs();
    }

    private void performSimilaritySearch(){
        List<Document> results = vectorStore.similaritySearch(SearchRequest.defaults().withSimilarityThreshold(0.5).withQuery("what is VoLTE?"));
        System.out.format("documents found: %d\n", results.size());
    }


    private void autoLabelWebpages(){
        WebPageNode page = repository.findByUrl("https://www.du.ae/personal/support/articledetail?artid=PROD-59292&lang=en-US");
        var chatClient = chatClientBuilder.build();
        ChatResponse chatResponse = chatClient.prompt()
                .system("Your are an expert at labelling documents to make them easy to retrieve when asking questions")
                .user(String.format("Return all the applicable labels for this document, separated by a semicolon, " +
                        "without any preamble: \n\n %s", page.getText()))
                .call()
                .chatResponse();
        System.out.println(chatResponse.getResult().getOutput().toString());
    }

    private void extractQuestionAnswerPairs() throws JsonProcessingException {
        Pageable pageable = Pageable.ofSize(500);
        Page<WebPageNode> webPagesWithoutQuestions = repository.findWebPagesWithoutQuestionsAndAnswers(pageable);
        String systemPrompt = "You are a customer support specialist, good at detecting questions and answers in documents" ;
        var chatClient = chatClientBuilder.defaultSystem(systemPrompt).build();

        while(!webPagesWithoutQuestions.isEmpty()){
            for(WebPageNode webPageWithoutQuestions: webPagesWithoutQuestions){
                long started = System.currentTimeMillis();
                System.out.format("Extracting question and answers from \n%s\nText length: %d",
                        webPageWithoutQuestions.getCleanedText(), webPageWithoutQuestions.getCleanedText().length());
                List<QuestionAndAnswer> response = chatClient.prompt().user(String.format("""
                                        Extract each question and answer from the following text:
                                  %s
                                  """, webPageWithoutQuestions.getCleanedText())).call()
                        .entity(new ParameterizedTypeReference<>() {
                });
                long finished = System.currentTimeMillis();
                System.out.format("Q&A extraction took %d", finished - started);
                List<QuestionNode> questions = new ArrayList<>();
                for(QuestionAndAnswer questionAndAnswer: response){
                    questions.add(new QuestionNode(questionAndAnswer.getQuestion(),
                            new AnswerNode(questionAndAnswer.getAnswer())));
                }
                webPageWithoutQuestions.setQuestions(questions);
                repository.save(webPageWithoutQuestions);
            }
            pageable = pageable.next();
            webPagesWithoutQuestions = repository.findWebPagesWithoutQuestionsAndAnswers(pageable);
        }
    }
}
