package com.altruist.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"uuid"})
public class Account {

  @JsonIgnore
  private UUID uuid;
  @JsonIgnore
  private UUID addressUuid;
  @NotBlank
  private String username;
  @NotBlank
  private String email;
  @Valid
  private Address address;
}
