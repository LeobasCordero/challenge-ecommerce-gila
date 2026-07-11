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
 * CartItemDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-11T19:47:39.632494286Z[Etc/UTC]", comments = "Generator version: 7.4.0")
public class CartItemDto {

  private ProductDto product;

  private Integer quantity;

  public CartItemDto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CartItemDto(ProductDto product, Integer quantity) {
    this.product = product;
    this.quantity = quantity;
  }

  public CartItemDto product(ProductDto product) {
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

  public CartItemDto quantity(Integer quantity) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CartItemDto cartItemDto = (CartItemDto) o;
    return Objects.equals(this.product, cartItemDto.product) &&
        Objects.equals(this.quantity, cartItemDto.quantity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(product, quantity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CartItemDto {\n");
    sb.append("    product: ").append(toIndentedString(product)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
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

