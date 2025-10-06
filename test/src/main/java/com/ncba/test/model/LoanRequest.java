package com.ncba.test.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {
    @NotNull
    private Long accountId;
    @Min(1)
    private Double amount;
    @Min(1)
    private Integer tenure;
}
