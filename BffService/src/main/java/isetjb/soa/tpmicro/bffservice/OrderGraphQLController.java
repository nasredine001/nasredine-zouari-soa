package isetjb.soa.tpmicro.bffservice;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.dataloader.DataLoader;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import feign.FeignException;

@Controller
public class OrderGraphQLController {

    private final OrderClient orderClient;

    public OrderGraphQLController(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @QueryMapping
    public List<OrderDto> orders() {
        return orderClient.getAll();
    }

    @QueryMapping
    public OrderDto orderById(@Argument("id") Long id) {
        try {
            return orderClient.getById(id);
        } catch (FeignException.NotFound e) {
            return null;
        }
    }

    @QueryMapping
    public List<OrderDto> ordersByCustomer(@Argument("customerId") Long customerId) {
        return orderClient.getByCustomer(customerId);
    }

    // Filtre demandé : toutes / livrées / non livrées
    @QueryMapping
    public List<OrderDto> ordersByDelivery(@Argument("delivered") boolean delivered) {
        return delivered ? orderClient.getDelivered() : orderClient.getNonDelivered();
    }

    @QueryMapping
    public List<OrderDto> nonDeliveredOrders() {
        return orderClient.getNonDelivered();
    }

    @MutationMapping
    public OrderDto createOrder(@Argument("input") OrderInput input) {
        return orderClient.create(input);
    }

    @MutationMapping
    public OrderDto updateOrderStatus(@Argument("id") Long id, @Argument("status") String status) {
        return orderClient.updateStatus(id, status);
    }

    @MutationMapping
    public OrderDto updateOrderDelivery(@Argument("id") Long id, @Argument("delivered") boolean delivered) {
        return orderClient.updateDelivery(id, delivered);
    }

    /**
     * Résout le champ 'product' pour chaque 'OrderItem' sans faire de N+1 :
     * le DataLoader regroupe tous les productId de la requête en un seul
     * appel batch vers le service Produits.
     */
    @SchemaMapping(typeName = "OrderItem", field = "product")
    public CompletableFuture<ProductDto> getProduct(OrderItemDto orderItem,
                                                    DataLoader<Integer, ProductDto> loader) {
        return loader.load(orderItem.getProductId());
    }
}
