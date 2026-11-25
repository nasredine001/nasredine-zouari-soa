package isetjb.soa.tpmicro.bffservice;

import java.util.ArrayList;
import java.util.List;

public class OrderInput {

    private Long customerId;
    private List<OrderItemInput> items = new ArrayList<>();

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<OrderItemInput> getItems() { return items; }
    public void setItems(List<OrderItemInput> items) { this.items = items; }
}
