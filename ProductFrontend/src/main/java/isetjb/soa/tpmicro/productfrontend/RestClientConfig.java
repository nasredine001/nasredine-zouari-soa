package isetjb.soa.tpmicro.productfrontend;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    /**
     * RestTemplate "load-balancé" : l'URL http://product-service/...
     * est résolue via Eureka puis répartie entre les instances.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
