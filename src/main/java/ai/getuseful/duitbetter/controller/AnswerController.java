package ai.getuseful.duitbetter.controller;

import ai.getuseful.duitbetter.dto.GeneratedAnswer;
import ai.getuseful.duitbetter.service.QuestionsVectorStoreService;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AnswerController {
    @Autowired
    private QuestionsVectorStoreService service;
    @GetMapping(value = "/answer", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public GeneratedAnswer answer(@RequestParam String userInput){
        return service.answer(SearchRequest.defaults().withQuery(userInput));


    }
}
