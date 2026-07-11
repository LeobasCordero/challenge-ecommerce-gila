package com.gila.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity representing product records.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "initial_stock", nullable = false)
    private Integer initialStock;

    @Column(nullable = false, length = 100)
    private String category;

    /**
     * Retrieve product UUID.
     * @return unique product identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set product UUID.
     * @param id unique product identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retrieve product name.
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Set product name.
     * @param name product name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieve product description.
     * @return product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set product description.
     * @param description product description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieve product price.
     * @return product price value
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Set product price.
     * @param price product price value
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Retrieve current inventory stock level.
     * @return inventory stock count
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * Set inventory stock level.
     * @param stock inventory stock count
     */
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    /**
     * Retrieve initial inventory stock level.
     * @return initial inventory stock count
     */
    public Integer getInitialStock() {
        return initialStock;
    }

    /**
     * Set initial inventory stock level.
     * @param initialStock initial inventory stock count
     */
    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }

    /**
     * Retrieve category label.
     * @return category description
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set category label.
     * @param category category description
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
