package isetjb.soa.tpmicro.orderservice;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRestService {

    private final OrderService orderService;

    public OrderRestService(OrderService orderService) {
        this.orderService = orderService;
    }

    // 1) Lister les commandes
    @GetMapping("/commandes")
    public List<Order> getOrders() {
        return orderService.findAll();
    }

    // 2) Consulter une commande
    @GetMapping("/commandes/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return order == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(order);
    }

    // 3) Commandes d'un client
    @GetMapping("/commandes/client/{customerId}")
    public List<Order> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderService.findByCustomer(customerId);
    }

    // 3 bis) Exigence TP4 : commandes non livrées (repository : findByDeliveredFalse)
    @GetMapping("/commandes/non-livrees")
    public List<Order> getNonDeliveredOrders() {
        return orderService.findNonDelivered();
    }

    // 3 ter) Commandes livrées
    @GetMapping("/commandes/livrees")
    public List<Order> getDeliveredOrders() {
        return orderService.findDelivered();
    }

    // 4) Créer une commande (validation + prix via appel batch du service Produits)
    @PostMapping("/commandes")
    @ResponseStatus(HttpStatus.CREATED)
    public Order addOrder(@RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    // 5) Mettre à jour le statut
    @PutMapping("/commandes/{id}/statut")
    public Order updateStatus(@PathVariable Long id, @RequestParam String statut) {
        return orderService.updateStatus(id, statut);
    }

    // 5 bis) Marquer une commande comme livrée / non livrée
    @PutMapping("/commandes/{id}/livraison")
    public Order updateDelivery(@PathVariable Long id, @RequestParam boolean livre) {
        return orderService.updateDelivery(id, livre);
    }

    // 6) Supprimer
    @DeleteMapping("/commandes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
    }
}
