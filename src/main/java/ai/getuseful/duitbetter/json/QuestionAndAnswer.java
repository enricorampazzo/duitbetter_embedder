package ai.getuseful.duitbetter.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class QuestionAndAnswer {
    @JsonProperty
    private String question;
    @JsonProperty
    private String answer;
}
