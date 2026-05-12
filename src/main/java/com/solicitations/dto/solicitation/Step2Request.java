package com.solicitations.dto.solicitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Step2Request {

    @NotBlank
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido. Use o formato 00000-000 ou 00000000.")
    private String cep;

    @NotBlank
    @Size(max = 20)
    private String number;

    @Size(max = 100)
    private String complement;
}
