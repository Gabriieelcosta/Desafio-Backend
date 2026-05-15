package com.solicitations.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solicitations.dto.solicitation.SolicitationResponse;
import com.solicitations.dto.solicitation.Step1Request;
import com.solicitations.domain.enums.ServiceType;
import com.solicitations.domain.enums.SolicitationStatus;
import com.solicitations.exception.GlobalExceptionHandler;
import com.solicitations.exception.NotFoundException;
import com.solicitations.service.SolicitationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = SolicitationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class SolicitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SolicitationService solicitationService;

    private static final Long CLIENT_ID = 1L;

    @BeforeEach
    void setUpSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken(CLIENT_ID, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_returns201WithSolicitationResponse() throws Exception {
        SolicitationResponse response = SolicitationResponse.builder()
                .id(1L)
                .status(SolicitationStatus.DRAFT)
                .currentStep(0)
                .build();

        when(solicitationService.create(CLIENT_ID)).thenReturn(response);

        mockMvc.perform(post("/solicitations"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(solicitationService.getById(99L, CLIENT_ID))
                .thenThrow(new NotFoundException("Solicitação não encontrada."));

        mockMvc.perform(get("/solicitations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Solicitação não encontrada."));
    }

    @Test
    void saveStep1_returns400WhenTitleIsTooShort() throws Exception {
        Step1Request request = new Step1Request();
        request.setServiceType(ServiceType.INSTALLATION);
        request.setTitle("ab");
        request.setDescription("Descrição válida com mais de vinte caracteres aqui.");

        mockMvc.perform(put("/solicitations/1/step/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }
}
