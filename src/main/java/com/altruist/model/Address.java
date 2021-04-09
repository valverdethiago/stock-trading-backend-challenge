package com.altruist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"uuid"})
public class Address {

  @JsonIgnore
  private UUID uuid;
  @NotBlank
  private String name;
  @NotBlank
  private String street;
  @NotBlank
  private String city;
  @NotNull
  private State state;
  @NotNull
  private Integer zipcode;

}
