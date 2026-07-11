package com.gila.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity representing a line item in a billing order.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_purchase", nullable = false, precision = 12, scale = 4)
    private BigDecimal priceAtPurchase;

    /**
     * Retrieve order item UUID.
     * @return unique item identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set order item UUID.
     * @param id unique item identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retrieve parent order container.
     * @return parent order entity
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Set parent order container.
     * @param order parent order entity
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    /**
     * Retrieve targeted product record.
     * @return product entity
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Set targeted product record.
     * @param product product entity
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Retrieve purchased quantity.
     * @return quantity purchased
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Set purchased quantity.
     * @param quantity quantity purchased
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Retrieve recorded price at purchase time.
     * @return purchase price value
     */
    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    /**
     * Set recorded price at purchase time.
     * @param priceAtPurchase purchase price value
     */
    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
}
