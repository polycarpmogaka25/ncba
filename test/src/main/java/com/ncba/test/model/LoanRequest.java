package com.ncba.test.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanRequest {
    @NotNull
    private Long accountId;
    @Min(1)
    private Double amount;
    @Min(1)
    private Integer tenure;
}
