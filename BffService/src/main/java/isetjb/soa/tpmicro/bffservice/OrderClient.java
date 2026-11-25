package isetjb.soa.tpmicro.bffservice;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/commandes")
    List<OrderDto> getAll();

    @GetMapping("/commandes/{id}")
    OrderDto getById(@PathVariable("id") Long id);

    @GetMapping("/commandes/client/{customerId}")
    List<OrderDto> getByCustomer(@PathVariable("customerId") Long customerId);

    @GetMapping("/commandes/non-livrees")
    List<OrderDto> getNonDelivered();

    @GetMapping("/commandes/livrees")
    List<OrderDto> getDelivered();

    @PostMapping("/commandes")
    OrderDto create(@RequestBody OrderInput input);

    @PutMapping("/commandes/{id}/statut")
    OrderDto updateStatus(@PathVariable("id") Long id, @RequestParam("statut") String status);

    @PutMapping("/commandes/{id}/livraison")
    OrderDto updateDelivery(@PathVariable("id") Long id, @RequestParam("livre") boolean delivered);
}
