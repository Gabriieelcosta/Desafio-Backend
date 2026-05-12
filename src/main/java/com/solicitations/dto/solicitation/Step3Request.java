package com.solicitations.dto.solicitation;

import com.solicitations.domain.enums.Priority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class Step3Request {

    @NotNull
    private Priority priority;

    @NotNull
    private LocalDate preferredDate;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal estimatedValue;

    @NotNull
    private Boolean termsAccepted;
}
