package com.solicitations.integration.viacep;

import com.solicitations.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ViaCepClient {

    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestTemplate restTemplate;

    public ViaCepResponse fetch(String cep) {
        try {
            ViaCepResponse response = restTemplate.getForObject(VIACEP_URL, ViaCepResponse.class, cep);

            if (response == null || response.isInvalid()) {
                throw new BusinessException("CEP não encontrado: " + cep);
            }

            return response;
        } catch (RestClientException e) {
            throw new BusinessException("Serviço de CEP indisponível. Tente novamente mais tarde.");
        }
    }
}
