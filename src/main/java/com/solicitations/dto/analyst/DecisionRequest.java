package com.solicitations.dto.analyst;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DecisionRequest {

    @NotNull
    private Decision decision;

    @NotBlank
    @Size(min = 10, max = 1000)
    private String comment;
}
