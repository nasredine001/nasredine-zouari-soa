package isetjb.soa.tpmicro.orderservice;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> findNonDelivered() {
        return orderRepository.findByDeliveredFalse();
    }

    @Transactional(readOnly = true)
    public List<Order> findDelivered() {
        return orderRepository.findByDeliveredTrue();
    }

    @Transactional
    public Order create(OrderRequest request) {
        if (request == null || request.getCustomerId() == null) {
            throw new IllegalArgumentException("Le client (customerId) est obligatoire.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins un article.");
        }

        // 1 seul appel batch vers le service Produits pour toute la commande
        List<Integer> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .distinct()
                .toList();

        List<ProductDto> products = productClient.getProductsBatch(productIds);
        Map<Integer, ProductDto> productsById = products.stream()
                .collect(Collectors.toMap(ProductDto::getId, Function.identity()));

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setOrderDate(LocalDate.now());
        order.setStatus("CREATED");

        double total = 0.0;
        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = productsById.get(itemRequest.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("Produit introuvable : " + itemRequest.getProductId());
            }
            if (itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Stock insuffisant pour le produit " + product.getName()
                        + " (disponible : " + product.getStock() + ")");
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getPrice());
            order.addItem(item);

            total += product.getPrice() * itemRequest.getQuantity();
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable : " + id));
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        order.setStatus(status.toUpperCase());
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateDelivery(Long id, boolean delivered) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable : " + id));
        order.setDelivered(delivered);
        return orderRepository.save(order);
    }

    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Commande introuvable : " + id);
        }
        orderRepository.deleteById(id);
    }
}
