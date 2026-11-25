package isetjb.soa.tpmicro.orderfrontend;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client GraphQL du front-end Commandes.
 *
 * Le WebClient est "load-balanced" : l'URL utilise le nom logique
 * "bff-service" enregistré dans Eureka, pas un port codé en dur.
 */
@Configuration
public class GraphQlClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public HttpGraphQlClient graphQlClient(@LoadBalanced WebClient.Builder builder) {
        return HttpGraphQlClient.builder(builder.build())
                .url("http://bff-service/graphql")
                .build();
    }
}
