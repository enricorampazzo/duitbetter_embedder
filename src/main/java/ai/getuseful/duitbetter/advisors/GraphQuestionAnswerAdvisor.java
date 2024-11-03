package ai.getuseful.duitbetter.advisors;

import ai.getuseful.duitbetter.entities.QuestionNode;
import ai.getuseful.duitbetter.repository.QuestionNodeRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.client.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Content;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class GraphQuestionAnswerAdvisor extends QuestionAnswerAdvisor {

    private Neo4jVectorStore vectorStore;
    private SearchRequest searchRequest;
    private String userTextAdvise;
    private QuestionNodeRepository questionNodeRepository;
    private List<Document> documents;

    private static final String DEFAULT_USER_TEXT_ADVISE = """
			Context information is below.
			---------------------
			{question_answer_context}
			---------------------
			Given the context and provided history information and not prior knowledge,
			reply to the user comment. If the answer is not in the context, inform
			the user that you can't answer the question.
			""";

    public GraphQuestionAnswerAdvisor(Neo4jVectorStore vectorStore, SearchRequest searchRequest,
                                      QuestionNodeRepository repository) {
        super(vectorStore, searchRequest);
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.questionNodeRepository = repository;
        this.userTextAdvise = DEFAULT_USER_TEXT_ADVISE;

    }

    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String advisedUserText = request.userText() + System.lineSeparator() + this.userTextAdvise;
        List<Document> questionDocuments = vectorStore.similaritySearch(searchRequest);
        System.out.println("Most similar questions:");
        questionDocuments.forEach(d -> System.out.format("%s\n", d.getContent()));
        List<QuestionNode> questions = questionNodeRepository.findDistinctByIdIn(questionDocuments.stream()
                .map(qd -> UUID.fromString(qd.getId())).toList());
        var documents = questions.stream().map(qn -> Document.builder().withContent(qn.getWebPage().getCleanedText()).
                withMetadata(Map.of("url", qn.getWebPage().getUrl(), "title", qn.getWebPage().getTitle()))
                .build()).toList();
        new GraphQuestionAnswerAdvisor(vectorStore, searchRequest, questionNodeRepository);
        context.put(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS, documents);
        String documentContext = documents.stream()
                .map(Content::getContent)
                .collect(Collectors.joining(System.lineSeparator()));

        // 4. Advise the user parameters.
        Map<String, Object> advisedUserParams = new HashMap<>(request.userParams());
        System.out.format("document context: \n%s\n", documentContext);
        advisedUserParams.put("question_answer_context", documentContext);
        this.documents = documents;
        return AdvisedRequest.from(request)
                .withUserText(advisedUserText)
                .withUserParams(advisedUserParams)
                .build();
    }

}
