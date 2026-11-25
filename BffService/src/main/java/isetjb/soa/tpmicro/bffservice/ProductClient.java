package isetjb.soa.tpmicro.bffservice;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/produits")
    List<ProductDto> getAll();

    @GetMapping("/produits/{id}")
    ProductDto getById(@PathVariable("id") Integer id);

    // Appel batch : plusieurs produits en une seule requête
    @PostMapping("/produits/batch")
    List<ProductDto> getProductsBatch(@RequestBody List<Integer> productIds);

    @PostMapping("/produits")
    ProductDto create(@RequestBody ProductInput input);
}
