package com.gila.ecommerce.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ProductImportStatusDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-11T13:14:35.770236700-06:00[GMT-06:00]", comments = "Generator version: 7.4.0")
public class ProductImportStatusDto {

  private UUID taskId;

  private String status;

  private Integer totalRows;

  private Integer processedRows;

  private Integer errorCount;

  @Valid
  private List<String> warnings = new ArrayList<>();

  public ProductImportStatusDto() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ProductImportStatusDto(UUID taskId, String status, Integer totalRows, Integer processedRows, Integer errorCount, List<String> warnings) {
    this.taskId = taskId;
    this.status = status;
    this.totalRows = totalRows;
    this.processedRows = processedRows;
    this.errorCount = errorCount;
    this.warnings = warnings;
  }

  public ProductImportStatusDto taskId(UUID taskId) {
    this.taskId = taskId;
    return this;
  }

  /**
   * Get taskId
   * @return taskId
  */
  @NotNull @Valid 
  @Schema(name = "taskId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("taskId")
  public UUID getTaskId() {
    return taskId;
  }

  public void setTaskId(UUID taskId) {
    this.taskId = taskId;
  }

  public ProductImportStatusDto status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @NotNull 
  @Schema(name = "status", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public ProductImportStatusDto totalRows(Integer totalRows) {
    this.totalRows = totalRows;
    return this;
  }

  /**
   * Get totalRows
   * @return totalRows
  */
  @NotNull 
  @Schema(name = "totalRows", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalRows")
  public Integer getTotalRows() {
    return totalRows;
  }

  public void setTotalRows(Integer totalRows) {
    this.totalRows = totalRows;
  }

  public ProductImportStatusDto processedRows(Integer processedRows) {
    this.processedRows = processedRows;
    return this;
  }

  /**
   * Get processedRows
   * @return processedRows
  */
  @NotNull 
  @Schema(name = "processedRows", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("processedRows")
  public Integer getProcessedRows() {
    return processedRows;
  }

  public void setProcessedRows(Integer processedRows) {
    this.processedRows = processedRows;
  }

  public ProductImportStatusDto errorCount(Integer errorCount) {
    this.errorCount = errorCount;
    return this;
  }

  /**
   * Get errorCount
   * @return errorCount
  */
  @NotNull 
  @Schema(name = "errorCount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("errorCount")
  public Integer getErrorCount() {
    return errorCount;
  }

  public void setErrorCount(Integer errorCount) {
    this.errorCount = errorCount;
  }

  public ProductImportStatusDto warnings(List<String> warnings) {
    this.warnings = warnings;
    return this;
  }

  public ProductImportStatusDto addWarningsItem(String warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }
    this.warnings.add(warningsItem);
    return this;
  }

  /**
   * Get warnings
   * @return warnings
  */
  @NotNull 
  @Schema(name = "warnings", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("warnings")
  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProductImportStatusDto productImportStatusDto = (ProductImportStatusDto) o;
    return Objects.equals(this.taskId, productImportStatusDto.taskId) &&
        Objects.equals(this.status, productImportStatusDto.status) &&
        Objects.equals(this.totalRows, productImportStatusDto.totalRows) &&
        Objects.equals(this.processedRows, productImportStatusDto.processedRows) &&
        Objects.equals(this.errorCount, productImportStatusDto.errorCount) &&
        Objects.equals(this.warnings, productImportStatusDto.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskId, status, totalRows, processedRows, errorCount, warnings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProductImportStatusDto {\n");
    sb.append("    taskId: ").append(toIndentedString(taskId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    totalRows: ").append(toIndentedString(totalRows)).append("\n");
    sb.append("    processedRows: ").append(toIndentedString(processedRows)).append("\n");
    sb.append("    errorCount: ").append(toIndentedString(errorCount)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
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

