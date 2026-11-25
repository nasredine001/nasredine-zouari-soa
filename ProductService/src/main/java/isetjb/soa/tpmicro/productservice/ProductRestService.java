package isetjb.soa.tpmicro.productservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductRestService {

    @Autowired
    private ProductRepository productRepository;

    // Port de l'instance courante
    @Value("${server.port:8080}")
    private String port;

    // Propriétés fournies par le Config Service (valeur par défaut si il est arrêté)
    @Value("${vglobale:(config-service indisponible)}")
    private String globale;

    @Value("${sujet:(config-service indisponible)}")
    private String sujet;

    @GetMapping("/messages")
    public String tellMe() {
        return "c'est l'instance du " + port + " qui a répondu ! : sujet = " + sujet + " : globale = " + globale;
    }

    // 1) Lister
    @GetMapping("/produits")
    public Iterable<Product> getProducts() {
        return productRepository.findAll();
    }

    // 2) Consulter un produit
    @GetMapping("/produits/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable int id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // TP4 : endpoint batch (un seul appel pour plusieurs IDs, évite le N+1)
    // POST /produits/batch  avec [1, 2, 3] dans le corps
    @PostMapping({"/produits/batch", "/api/products/batch"})
    public List<Product> getProductsBatch(@RequestBody List<Integer> ids) {
        System.out.println("Appel batch produits : " + ids);
        return productRepository.findAllById(ids);
    }

    // 3) Ajouter
    @PostMapping("/produits")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(@RequestBody Product p) {
        p.setId(0); // force la génération d'un nouvel identifiant
        return productRepository.save(p);
    }

    // 4) Modifier
    @PutMapping("/produits/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product p) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(p.getName());
            return ResponseEntity.ok(productRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5) Supprimer
    @DeleteMapping("/produits/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
