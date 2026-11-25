package isetjb.soa.tpmicro.orderfrontend;

/**
 * Formulaire simple : un client et jusqu'à trois lignes de commande.
 * Les lignes laissées vides sont ignorées.
 */
public class OrderForm {

    private Long customerId;

    private Integer productId1;
    private Integer quantity1 = 1;

    private Integer productId2;
    private Integer quantity2 = 1;

    private Integer productId3;
    private Integer quantity3 = 1;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Integer getProductId1() { return productId1; }
    public void setProductId1(Integer productId1) { this.productId1 = productId1; }

    public Integer getQuantity1() { return quantity1; }
    public void setQuantity1(Integer quantity1) { this.quantity1 = quantity1; }

    public Integer getProductId2() { return productId2; }
    public void setProductId2(Integer productId2) { this.productId2 = productId2; }

    public Integer getQuantity2() { return quantity2; }
    public void setQuantity2(Integer quantity2) { this.quantity2 = quantity2; }

    public Integer getProductId3() { return productId3; }
    public void setProductId3(Integer productId3) { this.productId3 = productId3; }

    public Integer getQuantity3() { return quantity3; }
    public void setQuantity3(Integer quantity3) { this.quantity3 = quantity3; }
}
