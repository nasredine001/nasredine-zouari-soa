package isetjb.soa.tpmicro.bffservice;

public class OrderItemDto {

    private Long id;
    private Integer productId;
    private int quantity;
    private double unitPrice;

    // Champ enrichi, rempli par le DataLoader GraphQL (pas par le service Commandes)
    private ProductDto product;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public ProductDto getProduct() { return product; }
    public void setProduct(ProductDto product) { this.product = product; }
}
