package isetjb.soa.tpmicro.orderservice;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Crée deux commandes de démonstration au démarrage.
 * L'appel batch vers le service Produits est retenté le temps que
 * l'enregistrement Eureka soit propagé.
 */
@Component
class DummyDataCLR implements CommandLineRunner {

    private final OrderService orderService;

    DummyDataCLR(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (orderService.findAll().size() > 0) {
            return;
        }

        for (int attempt = 1; attempt <= 10; attempt++) {
            try {
                OrderRequest first = new OrderRequest();
                first.setCustomerId(1L);
                first.setItems(List.of(
                        new OrderItemRequest(1, 2),
                        new OrderItemRequest(2, 1)));

                OrderRequest second = new OrderRequest();
                second.setCustomerId(2L);
                second.setItems(List.of(
                        new OrderItemRequest(3, 5)));

                orderService.create(first);
                Order secondOrder = orderService.create(second);
                // Une commande livrée pour illustrer le filtre du front-end
                orderService.updateDelivery(secondOrder.getId(), true);
                System.out.println("Commandes de démonstration créées.");
                return;
            } catch (Exception e) {
                System.out.println("Service Produits non prêt (tentative " + attempt + "/10) : " + e.getMessage());
                Thread.sleep(2000);
            }
        }
        System.out.println("Impossible de créer les commandes de démonstration : service Produits indisponible.");
    }
}
