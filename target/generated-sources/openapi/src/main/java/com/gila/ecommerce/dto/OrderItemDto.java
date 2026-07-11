package com.gila.ecommerce.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.gila.ecommerce.dto.ProductDto;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * OrderItemDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-10T23:03:24.260209300-06:00[GMT-06:00]", comments = "Generator version: 7.4.0")
public class OrderItemDto {

  private ProductDto product;

  private Integer quantity;

  private Double priceAtPurchase;

  public OrderItemDto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public OrderItemDto(ProductDto product, Integer quantity, Double priceAtPurchase) {
    this.product = product;
    this.quantity = quantity;
    this.priceAtPurchase = priceAtPurchase;
  }

  public OrderItemDto product(ProductDto product) {
    this.product = product;
    return this;
  }

  /**
   * Get product
   * @return product
  */
  @NotNull @Valid 
  @Schema(name = "product", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("product")
  public ProductDto getProduct() {
    return product;
  }

  public void setProduct(ProductDto product) {
    this.product = product;
  }

  public OrderItemDto quantity(Integer quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * @return quantity
  */
  @NotNull 
  @Schema(name = "quantity", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("quantity")
  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public OrderItemDto priceAtPurchase(Double priceAtPurchase) {
    this.priceAtPurchase = priceAtPurchase;
    return this;
  }

  /**
   * Get priceAtPurchase
   * @return priceAtPurchase
  */
  @NotNull 
  @Schema(name = "priceAtPurchase", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("priceAtPurchase")
  public Double getPriceAtPurchase() {
    return priceAtPurchase;
  }

  public void setPriceAtPurchase(Double priceAtPurchase) {
    this.priceAtPurchase = priceAtPurchase;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrderItemDto orderItemDto = (OrderItemDto) o;
    return Objects.equals(this.product, orderItemDto.product) &&
        Objects.equals(this.quantity, orderItemDto.quantity) &&
        Objects.equals(this.priceAtPurchase, orderItemDto.priceAtPurchase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(product, quantity, priceAtPurchase);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrderItemDto {\n");
    sb.append("    product: ").append(toIndentedString(product)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    priceAtPurchase: ").append(toIndentedString(priceAtPurchase)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

