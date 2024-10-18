package ai.getuseful.duitbetter.controller;

import ai.getuseful.duitbetter.service.QuestionsVectorStoreService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@Controller
public class ChatbotController {

    @GetMapping
    public String home(Model model){
        model.addAttribute("answer", "");
        return "chatbot";
    }

}
