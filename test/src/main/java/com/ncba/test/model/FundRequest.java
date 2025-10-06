package com.ncba.test.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FundRequest {
    @NotNull
    private Long accountId;
    @Min(0)
    private Double amount;
}
