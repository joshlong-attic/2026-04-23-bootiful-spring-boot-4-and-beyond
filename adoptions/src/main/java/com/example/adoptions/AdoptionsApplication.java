package com.example.adoptions;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.registry.ImportHttpServices;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//@Import(MyBeanRegistrar.class)

//@EnableMultiFactorAuthentication(authorities = {
//        // FactorGrantedAuthority.PASSWORD_AUTHORITY,
//        // FactorGrantedAuthority.OTT_AUTHORITY
//})
@EnableResilientMethods
@ImportHttpServices(CatFactsClient.class)
@ImportHttpServices(MessageClient.class)
@SpringBootApplication
public class AdoptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptionsApplication.class, args);
    }

    @Bean
    OAuth2RestClientHttpServiceGroupConfigurer auth2RestClientHttpServiceGroupConfigurer(
            OAuth2AuthorizedClientManager auth2AuthorizedClientManager) {
        return OAuth2RestClientHttpServiceGroupConfigurer
                .from(auth2AuthorizedClientManager);
    }

}

// > BeanFactoryPostProcessors (don't need BeanDefinitions)
// > ApplicationContextInitializer (because it works in the component model)

// project valhalla

/// *value*/ record Point(float x, float y) {
// }

/*
 * void calculate() { int x, y; var p = new Point(1, 2); var list =new ArrayList<int!!>();
 *
 * // x .equals(y); var list = List.of(new Point(0, 0)); var arrayOfArrays = new
 * float[][]{{1, 2}, {3, 4}}; // cache line locality
 *
 * }
 *
 */
class MyBeanRegistrar implements BeanRegistrar {

    void notNull(String str) {
    }

    @Override
    public void register(BeanRegistry registry, Environment env) {

        // notNull(null);
        for (var i = 0; i < 5; i++) {
            var nom = "le monde #" + i;
            registry.registerBean("bean" + i, MyRunner.class,
                    spec -> spec.supplier(beans -> new MyRunner(beans.bean(DataSource.class), nom)));
        }
    }

}

// @Component
class Runners implements ApplicationRunner {

    private final Map<String, MyRunner> stringMyRunnerMap;

    Runners(Map<String, MyRunner> stringMyRunnerMap) {
        this.stringMyRunnerMap = stringMyRunnerMap;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (var myRunner : stringMyRunnerMap.entrySet()) {
            IO.println(myRunner.getKey() + ":" + myRunner.getValue());
        }
    }

}

// explicit
@Configuration
class MyConfiguration {

    // @Bean
    // MyRunner runner() {
    // return new MyRunner();
    // }

}

// @Component
class MyRunner implements ApplicationRunner {

    private final String nom;

    MyRunner(DataSource dataSource, String nom) {
        this.nom = nom;
        Assert.notNull(dataSource, "the db shoudl not ne null!");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IO.println("salut " + this.nom);
    }

}


// security + oauth + gateway
// modulith
// ai


@ClientRegistrationId("spring")
interface MessageClient {

    @GetExchange("http://localhost:8081/message")
    Message message();
}


@Controller
@ResponseBody
class MessageClientController {

    private final MessageClient mc;

    MessageClientController(MessageClient mc) {
        this.mc = mc;
    }

    @Nullable
    @GetMapping("/message")
    Message message(
//            @RegisteredOAuth2AuthorizedClient("spring") OAuth2AuthorizedClient client
    ) {
        return mc.message();
    }
}

/*
@Controller
@ResponseBody
class MessageClientController {

    private final RestClient http;

    MessageClientController(RestClient.Builder http, OAuth2AuthorizedClientManager am) {
        this.http = http
                .requestInterceptor(new OAuth2ClientHttpRequestInterceptor(am))
                .build();
    }

    @Nullable
    @GetMapping("/message")
    Message message(@RegisteredOAuth2AuthorizedClient("spring") OAuth2AuthorizedClient client) {
        return this.http
                .get()
                .uri("http://localhost:8081/message")
                .attributes(ClientAttributes.clientRegistrationId("spring"))
//                .headers(h -> h.setBearerAuth(client.getAccessToken().getTokenValue()))
                .retrieve()
                .body(Message.class);
    }
}*/

record Message(String message) {
}

@Controller
@ResponseBody
class MeController {

//    @GetMapping("/admin")
//    Map<String, String> admin(Principal principal) {
//        return Map.of("adminName", principal.getName());
//    }

    @GetMapping("/")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }

}

// implicit
// explicit

// stereotype annotations (from UML)
// component scanning
// and meta annotations

// REST by Dr. Roy Fielding in ~2001
// hypermedia
// Spring HATEOAS (hypermedia as the engine of application state)

@Controller
@ResponseBody
class CatFactsController {

    private final CatFactsClient facts;

    private final AtomicInteger counter = new AtomicInteger(0);

    CatFactsController(CatFactsClient facts) {
        this.facts = facts;
    }

    @ConcurrencyLimit(10)
    @Retryable(maxRetries = 5, includes = IllegalStateException.class)
    @GetMapping("/cats")
    CatFacts facts() {
        if (this.counter.getAndIncrement() < 5) {
            IO.println("oops!");
            throw new IllegalStateException("oops!");
        }
        IO.println("yay!");
        return this.facts.facts();
    }

}

interface CatFactsClient {

    @GetExchange("https://www.catfacts.net/api")
    CatFacts facts();

}

/*
 * @Component class CatFactsClient {
 *
 * private final RestClient http;
 *
 * CatFactsClient(RestClient.Builder http) { this.http = http.build(); }
 *
 * CatFacts facts() { return this.http .get() .uri("https://www.catfacts.net/api")
 * .retrieve() .body(CatFacts.class); }
 *
 * }
 */

record CatFact(String fact) {
}

record CatFacts(Collection<CatFact> facts) {
}

@Controller
@ResponseBody
class DogsController {

    private final DogRepository repository;

    DogsController(DogRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/dogs", version = "1.1")
    Collection<Map<String, Object>> dogsv11() {
        return this.repository.findAll()
                .stream()
                .map(dog -> Map.of("fullName", (Object) dog.name(), "id", dog.id()))
                .toList();
    }

    @GetMapping(value = "/dogs", version = "1.0")
    Collection<Dog> dogs() {
        return this.repository.findAll();
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {

}

record Dog(@Id int id, String name, String description, String owner) {
}