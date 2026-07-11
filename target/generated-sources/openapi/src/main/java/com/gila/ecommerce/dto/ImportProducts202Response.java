package com.gila.ecommerce.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ImportProducts202Response
 */

@JsonTypeName("importProducts_202_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-10T21:59:44.763631900-06:00[GMT-06:00]", comments = "Generator version: 7.4.0")
public class ImportProducts202Response {

  private UUID taskId;

  private String status;

  public ImportProducts202Response taskId(UUID taskId) {
    this.taskId = taskId;
    return this;
  }

  /**
   * Unique identifier of the queued import task
   * @return taskId
  */
  @Valid 
  @Schema(name = "taskId", description = "Unique identifier of the queued import task", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("taskId")
  public UUID getTaskId() {
    return taskId;
  }

  public void setTaskId(UUID taskId) {
    this.taskId = taskId;
  }

  public ImportProducts202Response status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Initial status of the task
   * @return status
  */
  
  @Schema(name = "status", example = "PENDING", description = "Initial status of the task", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ImportProducts202Response importProducts202Response = (ImportProducts202Response) o;
    return Objects.equals(this.taskId, importProducts202Response.taskId) &&
        Objects.equals(this.status, importProducts202Response.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskId, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ImportProducts202Response {\n");
    sb.append("    taskId: ").append(toIndentedString(taskId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

