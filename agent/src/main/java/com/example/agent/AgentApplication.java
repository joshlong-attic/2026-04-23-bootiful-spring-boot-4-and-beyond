package com.example.agent;

import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static org.springaicommunity.mcp.security.client.sync.config.McpClientOAuth2Configurer.mcpClientOAuth2;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }
}

record Dog(@Id int id, String name, String description) {
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

@Configuration
class SecurityConfiguration {

    @Bean
    Customizer<HttpSecurity> customCorsConfigurationSource() {
        return http -> http.with(mcpClientOAuth2());
    }
}


@Controller
@ResponseBody
class AgentController {

    private final ChatClient ai;

    AgentController(
            DogRepository dogRepository,
            VectorStore v,
            ToolCallbackProvider toolCallbackProvider,
            QuestionAnswerAdvisor qa,
            ChatClient.Builder ai) {

        if (false) {
            dogRepository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        dog.id(), dog.name(), dog.description()
                ));
                v.add(List.of(dogument));
            });
        }

        var skills = SkillsTool
                .builder()
                .addSkillsResource(new ClassPathResource("/META-INF/skills"))
                .build();

        this.ai = ai
                .defaultToolCallbacks(skills)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultSystem("""
                        You are an AI powered assistant to help people adopt a dog from the adoptions agency named Pooch Palace with locations in 
                        Seoul, Tokyo, Singapore, Paris, Mumbai, New Delhi, Barcelona, San Francisco, and London. 
                        Information about the dogs availables will be presented below. If there is no information, then return a polite 
                        response suggesting we don't have any dogs available.
                        
                        If somebody asks you about animals, and there's no information in the context, then feel free to source the answer from other places.
                        
                        If somebody asks for a time to pick up the dog, don't ask other questions: simply provide a time by consulting the tools you have available.
                        
                        """)
                .defaultAdvisors(qa)
                .build();
    }

    @GetMapping("/ask")
    String ask(
//            @RequestParam String question
    ) {
        return this.ai
                .prompt()
                .user("""
                    fantastic. when can i pick up Prancer from the Paris Pooch Palce location?    
                    """)
                .call()
                .content();
//                .entity(DogAdoptionSuggestion.class);
    }
}


record DogAdoptionSuggestion(int id, String name) {
}