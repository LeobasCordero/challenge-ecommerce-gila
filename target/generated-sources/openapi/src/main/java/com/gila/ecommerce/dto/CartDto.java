package com.gila.ecommerce.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.gila.ecommerce.dto.CartItemDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CartDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-10T22:30:28.573925500-06:00[GMT-06:00]", comments = "Generator version: 7.4.0")
public class CartDto {

  @Valid
  private List<@Valid CartItemDto> items = new ArrayList<>();

  private Double totalPrice;

  public CartDto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CartDto(List<@Valid CartItemDto> items, Double totalPrice) {
    this.items = items;
    this.totalPrice = totalPrice;
  }

  public CartDto items(List<@Valid CartItemDto> items) {
    this.items = items;
    return this;
  }

  public CartDto addItemsItem(CartItemDto itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
  */
  @NotNull @Valid 
  @Schema(name = "items", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("items")
  public List<@Valid CartItemDto> getItems() {
    return items;
  }

  public void setItems(List<@Valid CartItemDto> items) {
    this.items = items;
  }

  public CartDto totalPrice(Double totalPrice) {
    this.totalPrice = totalPrice;
    return this;
  }

  /**
   * Get totalPrice
   * @return totalPrice
  */
  @NotNull 
  @Schema(name = "totalPrice", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalPrice")
  public Double getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(Double totalPrice) {
    this.totalPrice = totalPrice;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CartDto cartDto = (CartDto) o;
    return Objects.equals(this.items, cartDto.items) &&
        Objects.equals(this.totalPrice, cartDto.totalPrice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, totalPrice);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CartDto {\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    totalPrice: ").append(toIndentedString(totalPrice)).append("\n");
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

