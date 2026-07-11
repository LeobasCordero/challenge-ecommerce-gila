package com.gila.ecommerce.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CartItemRequestDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-11T15:30:27.950957500-06:00[GMT-06:00]", comments = "Generator version: 7.4.0")
public class CartItemRequestDto {

  private UUID productId;

  private Integer quantity;

  public CartItemRequestDto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CartItemRequestDto(UUID productId, Integer quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }

  public CartItemRequestDto productId(UUID productId) {
    this.productId = productId;
    return this;
  }

  /**
   * Get productId
   * @return productId
  */
  @NotNull @Valid 
  @Schema(name = "productId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("productId")
  public UUID getProductId() {
    return productId;
  }

  public void setProductId(UUID productId) {
    this.productId = productId;
  }

  public CartItemRequestDto quantity(Integer quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * minimum: 1
   * @return quantity
  */
  @NotNull @Min(1) 
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
    CartItemRequestDto cartItemRequestDto = (CartItemRequestDto) o;
    return Objects.equals(this.productId, cartItemRequestDto.productId) &&
        Objects.equals(this.quantity, cartItemRequestDto.quantity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, quantity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CartItemRequestDto {\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
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

