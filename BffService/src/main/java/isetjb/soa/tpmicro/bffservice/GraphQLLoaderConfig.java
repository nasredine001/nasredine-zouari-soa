package isetjb.soa.tpmicro.bffservice;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dataloader.BatchLoaderEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.DefaultBatchLoaderRegistry;

import reactor.core.publisher.Mono;

/**
 * Enregistre le DataLoader utilisé pour le champ OrderItem.product.
 * Le BatchLoaderRegistry collecte tous les IDs demandés pendant la requête
 * GraphQL puis effectue UN SEUL appel batch vers le service Produits.
 *
 * API Spring for GraphQL 1.3 : le loader renvoie un Mono (Reactor).
 */
@Configuration
public class GraphQLLoaderConfig {

    @Bean
    public BatchLoaderRegistry batchLoaderRegistry(ProductClient productClient) {
        BatchLoaderRegistry registry = new DefaultBatchLoaderRegistry();

        registry.forTypePair(Integer.class, ProductDto.class)
                .registerMappedBatchLoader((Set<Integer> productIds, BatchLoaderEnvironment environment) ->
                        Mono.fromCallable(() -> productClient.getProductsBatch(List.copyOf(productIds))
                                .stream()
                                .collect(Collectors.toMap(ProductDto::getId, Function.identity()))));

        return registry;
    }
}
