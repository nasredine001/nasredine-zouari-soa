package isetjb.soa.tpmicro.orderfrontend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {

    // Champs GraphQL communs à toutes les requêtes de commandes
    private static final String ORDER_FIELDS = """
            id
            customerId
            orderDate
            status
            totalAmount
            delivered
            items {
              id
              productId
              quantity
              unitPrice
              product {
                id
                name
                price
              }
            }
            """;

    private final HttpGraphQlClient graphQlClient;

    public OrderWebController(HttpGraphQlClient graphQlClient) {
        this.graphQlClient = graphQlClient;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/commandes";
    }

    /**
     * Affichage avec filtres demandés :
     *   /commandes                  -> toutes
     *   /commandes?filtre=livrees   -> livrées
     *   /commandes?filtre=non-livrees -> non livrées
     *   /commandes?id=3             -> recherche par id
     * Une seule requête GraphQL : le BFF regroupe les appels produits
     * via son DataLoader (pas de N+1).
     */
    @GetMapping("/commandes")
    public String listOrders(@RequestParam(defaultValue = "toutes") String filtre,
                             @RequestParam(required = false) Long id,
                             Model model) {
        model.addAttribute("filtre", filtre);
        model.addAttribute("searchId", id);

        List<OrderDto> orders = new ArrayList<>();
        try {
            if (id != null) {
                String document = "query GetOrder($id: ID!) { orderById(id: $id) { " + ORDER_FIELDS + " } }";
                OrderDto order = graphQlClient.document(document)
                        .variable("id", id)
                        .retrieve("orderById")
                        .toEntity(OrderDto.class)
                        .block();
                if (order != null) {
                    orders.add(order);
                } else {
                    model.addAttribute("error", "Aucune commande avec l'id " + id + ".");
                }
            } else if ("livrees".equals(filtre)) {
                String document = "query { ordersByDelivery(delivered: true) { " + ORDER_FIELDS + " } }";
                orders = graphQlClient.document(document)
                        .retrieve("ordersByDelivery")
                        .toEntityList(OrderDto.class)
                        .block();
            } else if ("non-livrees".equals(filtre)) {
                String document = "query { ordersByDelivery(delivered: false) { " + ORDER_FIELDS + " } }";
                orders = graphQlClient.document(document)
                        .retrieve("ordersByDelivery")
                        .toEntityList(OrderDto.class)
                        .block();
            } else {
                String document = "query { orders { " + ORDER_FIELDS + " } }";
                orders = graphQlClient.document(document)
                        .retrieve("orders")
                        .toEntityList(OrderDto.class)
                        .block();
            }
        } catch (RuntimeException e) {
            model.addAttribute("error", "Le BFF GraphQL est indisponible : " + e.getMessage());
        }

        model.addAttribute("orders", orders == null ? List.of() : orders);
        return "orders";
    }

    /**
     * Formulaire de création : les produits sont chargés via GraphQL
     * (requête products du BFF), pas directement via l'API REST Produits.
     */
    @GetMapping("/commandes/nouvelle")
    public String showCreateForm(Model model) {
        String document = """
                query GetProducts {
                  products {
                    id
                    name
                    price
                    stock
                  }
                }
                """;
        try {
            List<ProductDto> products = graphQlClient.document(document)
                    .retrieve("products")
                    .toEntityList(ProductDto.class)
                    .block();
            model.addAttribute("products", products == null ? List.of() : products);
        } catch (RuntimeException e) {
            model.addAttribute("products", List.of());
            model.addAttribute("error", "Impossible de charger les produits : " + e.getMessage());
        }
        model.addAttribute("orderForm", new OrderForm());
        return "order-form";
    }

    /**
     * Envoie la mutation createOrder au BFF GraphQL.
     */
    @PostMapping("/commandes")
    public String createOrder(@ModelAttribute OrderForm form, RedirectAttributes redirectAttributes) {
        if (form.getCustomerId() == null) {
            redirectAttributes.addFlashAttribute("error", "L'identifiant du client est obligatoire.");
            return "redirect:/commandes/nouvelle";
        }

        List<Map<String, Object>> items = new ArrayList<>();
        addItem(items, form.getProductId1(), form.getQuantity1());
        addItem(items, form.getProductId2(), form.getQuantity2());
        addItem(items, form.getProductId3(), form.getQuantity3());

        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ajoutez au moins un produit.");
            return "redirect:/commandes/nouvelle";
        }

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("customerId", form.getCustomerId());
        input.put("items", items);

        String document = """
                mutation CreateOrder($input: OrderInput!) {
                  createOrder(input: $input) {
                    id
                    status
                    totalAmount
                    delivered
                  }
                }
                """;
        try {
            graphQlClient.document(document)
                    .variable("input", input)
                    .retrieve("createOrder")
                    .toEntity(OrderDto.class)
                    .block();
            redirectAttributes.addFlashAttribute("message", "Commande créée avec succès.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Création impossible : " + e.getMessage());
        }
        return "redirect:/commandes";
    }

    /**
     * Marque une commande comme livrée / non livrée (mutation GraphQL).
     */
    @PostMapping("/commandes/{id}/livraison")
    public String updateDelivery(@PathVariable Long id,
                                 @RequestParam boolean livre,
                                 @RequestParam(defaultValue = "toutes") String filtre,
                                 RedirectAttributes redirectAttributes) {
        String document = """
                mutation UpdateDelivery($id: ID!, $delivered: Boolean!) {
                  updateOrderDelivery(id: $id, delivered: $delivered) {
                    id
                    delivered
                  }
                }
                """;
        try {
            graphQlClient.document(document)
                    .variable("id", id)
                    .variable("delivered", livre)
                    .retrieve("updateOrderDelivery")
                    .toEntity(OrderDto.class)
                    .block();
            redirectAttributes.addFlashAttribute("message",
                    livre ? "Commande marquée comme livrée." : "Commande marquée comme non livrée.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Mise à jour impossible : " + e.getMessage());
        }
        return "redirect:/commandes?filtre=" + filtre;
    }

    private void addItem(List<Map<String, Object>> items, Integer productId, Integer quantity) {
        if (productId != null && quantity != null && quantity > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productId", productId);
            item.put("quantity", quantity);
            items.add(item);
        }
    }
}
