package ai.getuseful.duitbetter.controller;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ChatbotController {
    @GetMapping
    public String home(Model model){
        model.addAttribute("answer", "");
        return "chatbot";
    }

    @PostMapping("/answer")
    @HxRequest
    public String answer(@RequestBody String userInput){
        return "chatbot";
    }
}
