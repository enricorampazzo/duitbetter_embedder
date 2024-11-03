package ai.getuseful.duitbetter;

import ai.getuseful.duitbetter.advisors.GraphQuestionAnswerAdvisor;
import ai.getuseful.duitbetter.entities.AnswerNode;
import ai.getuseful.duitbetter.entities.QuestionNode;
import ai.getuseful.duitbetter.entities.WebPageNode;
import ai.getuseful.duitbetter.json.QuestionAndAnswer;
import ai.getuseful.duitbetter.json.QuestionAndAnswerConverter;
import ai.getuseful.duitbetter.repository.QuestionNodeRepository;
import ai.getuseful.duitbetter.repository.WebPageNodeRepository;
import ai.getuseful.duitbetter.service.QuestionsVectorStoreService;
import ai.getuseful.duitbetter.vectorstore.CustomEmbeddingPropertyNeo4jVectorStore;
import org.neo4j.driver.Driver;
import org.springframework.ai.autoconfigure.vectorstore.neo4j.Neo4jVectorStoreProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
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
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class Main implements CommandLineRunner {
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private Neo4jVectorStore questionsVectorStore;
    @Autowired
    private Driver driver;
    @Autowired
    private WebPageNodeRepository repository;
    @Autowired
    private QuestionNodeRepository questionNodeRepository;
    @Autowired
    private ChatClient.Builder chatClientBuilder;
    @Autowired
    QuestionsVectorStoreService questionsVectorStoreService;
    @Autowired
    Neo4jVectorStoreProperties vsProperties;



//    public Neo4jVectorStore getQuestionsVectorStore() {
//        if(questionsVectorStore == null){
//            questionsVectorStore = new Neo4jVectorStore(driver, embeddingModel,
//                    Neo4jVectorStore.Neo4jVectorStoreConfig.builder().withIndexName("questions-index")
//                            .withLabel("Question").withEmbeddingProperty("embedding")
//                            .withEmbeddingDimension(3096).build(), true);
//        }
//        return questionsVectorStore;
//    }

    private Neo4jVectorStore getQuestionsVectorStore(){
        String activeProfile = Optional.ofNullable(System.getenv("SPRING_PROFILES_ACTIVE")).orElse("default");
        if(activeProfile.equals("openai") && !questionsVectorStore.getClass().equals(CustomEmbeddingPropertyNeo4jVectorStore.class)){
            questionsVectorStore =  new CustomEmbeddingPropertyNeo4jVectorStore(driver, embeddingModel,vsProperties);
        }
        return questionsVectorStore;

    }

    @Override
    public void run(String... args) {
        //answerQuestions();
        //extractQuestionAnswerPairs();
        embedQuestions();
        //System.exit(0);
    }

    private void performSimilaritySearch(){

        List<String> questions = List.of(
                "Why are the speedtests result from netchek different from those from ookla?",
                "Speedtest results from ookla and netcheck are different. Why is that?",
                "Connection speed reported by du is different from that reported by ookla. Why?",
                "download speed from du is different from that I get from speedtest, explain"
        );
        for(String question:questions){
            System.out.format("Question: %s\n", question);
            List<Document> results = questionsVectorStore.similaritySearch(SearchRequest.defaults()
                    .withSimilarityThreshold(0.7).withQuery(question));
            Document result = results.getFirst();
            assert result.getId().equals("f1b065ed-5c78-4634-87d4-cac70c9505da");
            var q = questionNodeRepository.findById(UUID.fromString((String) result.getMetadata().get("id")));
            System.out.format("Answer: %s\n",q.getAnswer().getText());
        }
    }


    private void autoLabelWebpages(){
        WebPageNode page = repository.findByUrl("https://www.du.ae/personal/support/articledetail?artid=PROD-59292&lang=en-US");
        var chatClient = chatClientBuilder.build();
        ChatResponse chatResponse = chatClient.prompt()
                .system("Your are an expert at labelling documents to make them easy to retrieve when asking questions")
                .user(String.format("""
                        Return all the applicable labels for this document, separated by a semicolon, \
                        without any preamble:\s

                         %s""", page.getCleanedText()))
                .call()
                .chatResponse();
        System.out.println(chatResponse.getResult().getOutput().toString());
    }

    private void extractQuestionAnswerPairs() {
        Pageable pageable = Pageable.ofSize(1);
        Page<WebPageNode> webPagesWithoutQuestions = repository.findWebPagesWithoutQuestionsAndAnswers(pageable);
        String systemPrompt = "You are a customer support specialist, good at detecting questions and answers in documents" ;
        var chatClient = chatClientBuilder.defaultSystem(systemPrompt).build();
        while(!webPagesWithoutQuestions.isEmpty()){
            for(WebPageNode webPageWithoutQuestions: webPagesWithoutQuestions){
                long started = System.currentTimeMillis();
                System.out.format("Extracting question and answers from \n%s\nText length: %d\n",
                        webPageWithoutQuestions.getCleanedText(), webPageWithoutQuestions.getCleanedText().length());
                var converter = new QuestionAndAnswerConverter();
                List<QuestionAndAnswer> response;

                Flux<String> modelResponseFlux = chatClient.prompt().user(String.format("""
                                      Extract each question and answer from the following text,
                                      keeping in mind that a question must contain a question mark (?) and that an
                                      answer can have multiple periods (.).
                                      An answer can also be structured into bullet points and continue after a
                                      semicolon (:) Make sure to concatenate each answer into a string:
                                %s
                               %s
                               """, webPageWithoutQuestions.getCleanedText(), converter.getFormat())).stream().content();
                var modelResponse = modelResponseFlux.map(r -> {
                    System.out.print(r);
                    return r;
                }).collectList().block().stream().collect(Collectors.joining());
                response = converter.convert(modelResponse);
                long finished = System.currentTimeMillis();
                System.out.format("Q&A extraction took %d seconds\n", (finished - started)/1000);
                System.out.format("Parsed JSON: %s\n", response.toString());
                List<QuestionNode> questions = new ArrayList<>();
                int lastQuestionNotEmpty = 0;
                int lastAnswerNotEmpty = 0;
                for(int i = 0; i < response.size(); i++){
                    String question = Optional.ofNullable(response.get(i).getQuestion()).orElse("");
                    String answer = Optional.ofNullable(response.get(i).getAnswer()).orElse("");
                    boolean questionIsEmpty = question.isEmpty();
                    boolean answerIsEmpty = answer.isEmpty();
                    lastQuestionNotEmpty = questionIsEmpty ? lastQuestionNotEmpty : i;
                    lastAnswerNotEmpty = answerIsEmpty ? lastAnswerNotEmpty: i;
                    if(!questionIsEmpty && question.contains("?") && !answerIsEmpty){
                        questions.add(new QuestionNode(question, new AnswerNode(answer)));
                        continue;
                    }
                    if(questionIsEmpty || !question.contains("?")){
                        if(questions.isEmpty()) {
                            String existingAnswer = (String) Optional.ofNullable(response.get(lastQuestionNotEmpty).getAnswer()).orElse("");
                            questions.add(new QuestionNode((String) response.get(lastQuestionNotEmpty).getAnswer(), new AnswerNode(existingAnswer + answer)));
                        }
                        else {
                            QuestionNode lastQuestion = questions.getLast();
                            lastQuestion.getAnswer().setText(lastQuestion.getAnswer().getText() + question);
                        }
                    }
                }
                if(!questions.isEmpty()) {
                    System.out.format("Processed questions:\n %s\n", questions);
                    webPageWithoutQuestions.setQuestions(questions);
                    repository.save(webPageWithoutQuestions);
                }
            }
            pageable = pageable.next();
            webPagesWithoutQuestions = repository.findWebPagesWithoutQuestionsAndAnswers(pageable);
        }
    }

    private void embedQuestions(){
        Pageable pageable = Pageable.ofSize(100);
        Page<QuestionNode> questionsWithoutEmbedding = questionNodeRepository.findByTextEmbedding3SmallIsNull(pageable);
        while(!questionsWithoutEmbedding.isEmpty()) {
            getQuestionsVectorStore().add(questionsWithoutEmbedding.get().map(qn -> {
                System.out.format("now embedding %s\n", qn);
                return Document.builder().withContent(qn.getText()).withId(qn.getId().toString()).build();
            }).toList());
            pageable = pageable.next();
            questionsWithoutEmbedding = questionNodeRepository.findByTextEmbedding3SmallIsNull(pageable);
        }
    }

    private void answerQuestions(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("what would you like to ask?");
        for(String question = scanner.nextLine(); question!=null && !question.equals("exit"); System.out.println("what would you like to ask?"), question=scanner.nextLine()) {
            System.out.println(questionsVectorStoreService.answer(SearchRequest.defaults().withSimilarityThreshold(0.7).withTopK(4).withQuery(question)));
        }

    }
}
