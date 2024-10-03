package ai.getuseful.duitbetter;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class EmbedderApplication {

	public static void main(String[] args) {
//		SpringApplication.run(EmbedderApplication.class, args);
		new SpringApplicationBuilder(EmbedderApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

}
