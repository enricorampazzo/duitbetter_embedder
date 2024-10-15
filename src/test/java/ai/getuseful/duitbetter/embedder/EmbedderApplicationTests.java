package ai.getuseful.duitbetter.embedder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.regex.Pattern;


class EmbedderApplicationTests {

	@Test
	void regexTest() {
		var text = """
				Here is the extracted data in JSON format:
				   
				[
				  {
				    "question": "How can I track my eShop order through du website?",
				    "answer": "To track your eShop order, please visit https://shop.du.ae/en/order-tracking"
				  }
				]
				""";
		var questionPattern = Pattern.compile("\"(question)\"");

		var matcher =  questionPattern.matcher(text);
		matcher.find();
		System.out.println("hello");
	}

}
