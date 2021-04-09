package com.altruist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"uuid"})
public class Trade implements Serializable {

    @JsonIgnore
    private UUID uuid;
    @JsonIgnore
    private UUID accountUuid;
    @NotBlank
    private String symbol;
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotNull
    private TradeSide side;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
    private TradeStatus status;
    private BigDecimal totalAmount;
}
