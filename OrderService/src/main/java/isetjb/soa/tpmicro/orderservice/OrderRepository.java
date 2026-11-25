package isetjb.soa.tpmicro.orderservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    // Exigence TP4 : commandes non livrées / livrées
    List<Order> findByDeliveredFalse();

    List<Order> findByDeliveredTrue();
}
