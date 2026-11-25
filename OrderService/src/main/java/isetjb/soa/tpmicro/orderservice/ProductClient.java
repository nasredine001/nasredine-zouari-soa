package isetjb.soa.tpmicro.orderservice;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client Feign vers le micro-service Produits.
 * L'appel batch permet de récupérer tous les produits d'une commande
 * en une seule requête HTTP au lieu de N appels unitaires.
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    @PostMapping("/produits/batch")
    List<ProductDto> getProductsBatch(@RequestBody List<Integer> productIds);
}
