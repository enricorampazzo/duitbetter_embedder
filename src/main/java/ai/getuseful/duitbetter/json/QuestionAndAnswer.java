package ai.getuseful.duitbetter.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class QuestionAndAnswer {
    @JsonProperty
    private String question;
    @JsonProperty
    private String answer;
}
