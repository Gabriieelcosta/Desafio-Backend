package com.solicitations.dto.solicitation;

import com.solicitations.domain.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Step1Request {

    @NotNull
    private ServiceType serviceType;

    @NotBlank
    @Size(min = 3, max = 80)
    private String title;

    @NotBlank
    @Size(min = 20, max = 1000)
    private String description;
}
