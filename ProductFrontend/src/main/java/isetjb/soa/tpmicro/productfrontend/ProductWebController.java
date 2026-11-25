package isetjb.soa.tpmicro.productfrontend;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductWebController {

    private static final String PRODUCTS_URL = "http://product-service/produits";

    private final RestTemplate restTemplate;

    public ProductWebController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/produits";
    }

    // Affichage de la liste des produits via l'API REST Produits
    @GetMapping("/produits")
    public String listProducts(Model model) {
        try {
            ProductDto[] products = restTemplate.getForObject(PRODUCTS_URL, ProductDto[].class);
            model.addAttribute("products", products == null ? List.of() : Arrays.asList(products));
        } catch (RestClientException e) {
            model.addAttribute("products", List.of());
            model.addAttribute("error", "Le service Produits est indisponible.");
        }
        return "products";
    }

    @GetMapping("/produits/nouveau")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductDto());
        return "product-form";
    }

    // Création d'un produit via l'API REST Produits
    @PostMapping("/produits")
    public String createProduct(@ModelAttribute ProductDto product, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.postForObject(PRODUCTS_URL, product, ProductDto.class);
            redirectAttributes.addFlashAttribute("message", "Produit ajouté avec succès.");
        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute("error", "Impossible d'ajouter le produit.");
        }
        return "redirect:/produits";
    }

    // Suppression d'un produit
    @PostMapping("/produits/{id}/supprimer")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(PRODUCTS_URL + "/" + id);
            redirectAttributes.addFlashAttribute("message", "Produit supprimé.");
        } catch (RestClientException e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer le produit.");
        }
        return "redirect:/produits";
    }
}
