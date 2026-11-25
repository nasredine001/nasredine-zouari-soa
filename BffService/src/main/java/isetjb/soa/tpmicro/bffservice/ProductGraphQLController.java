package isetjb.soa.tpmicro.bffservice;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import feign.FeignException;

@Controller
public class ProductGraphQLController {

    private final ProductClient productClient;

    public ProductGraphQLController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @QueryMapping
    public List<ProductDto> products() {
        return productClient.getAll();
    }

    @QueryMapping
    public ProductDto productById(@Argument("id") Integer id) {
        try {
            return productClient.getById(id);
        } catch (FeignException.NotFound e) {
            return null;
        }
    }

    @MutationMapping
    public ProductDto createProduct(@Argument("input") ProductInput input) {
        return productClient.create(input);
    }
}
