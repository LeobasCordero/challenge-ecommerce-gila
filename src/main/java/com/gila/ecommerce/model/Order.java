package com.gila.ecommerce.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity representing checkout billing order records.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Retrieve order UUID.
     * @return unique order identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set order UUID.
     * @param id unique order identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retrieve associated customer user record.
     * @return customer user entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Set associated customer user record.
     * @param user customer user entity
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Retrieve order transaction status.
     * @return payment/transaction status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set order transaction status.
     * @param status payment/transaction status string
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retrieve calculated total price.
     * @return total price of order
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Set calculated total price.
     * @param totalPrice total price of order
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Retrieve creation timestamp.
     * @return creation timestamp offset dateTime
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set creation timestamp.
     * @param createdAt creation timestamp offset dateTime
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retrieve list of ordered items.
     * @return list of items associated with order
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Set list of ordered items.
     * @param items list of items associated with order
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
