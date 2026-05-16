package com.solicitations.controller;

import com.solicitations.aop.Audit;
import com.solicitations.dto.solicitation.SolicitationResponse;
import com.solicitations.dto.solicitation.Step1Request;
import com.solicitations.dto.solicitation.Step2Request;
import com.solicitations.dto.solicitation.Step3Request;
import com.solicitations.service.SolicitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Solicitações", description = "Abertura e acompanhamento de solicitações pelo cliente")
@RestController
@RequestMapping("/solicitations")
@RequiredArgsConstructor
public class SolicitationController {

    private final SolicitationService solicitationService;

    @Operation(summary = "Criar rascunho de solicitação")
    @PostMapping
    public ResponseEntity<SolicitationResponse> create() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitationService.create(currentUserId()));
    }

    @Operation(summary = "Preencher step 1 — tipo, título e descrição")
    @PutMapping("/{id}/step/1")
    public ResponseEntity<SolicitationResponse> saveStep1(@PathVariable Long id,
                                                          @Valid @RequestBody Step1Request request) {
        return ResponseEntity.ok(solicitationService.saveStep1(id, currentUserId(), request));
    }

    @Operation(summary = "Preencher step 2 — endereço (CEP consultado via ViaCEP)")
    @PutMapping("/{id}/step/2")
    public ResponseEntity<SolicitationResponse> saveStep2(@PathVariable Long id,
                                                          @Valid @RequestBody Step2Request request) {
        return ResponseEntity.ok(solicitationService.saveStep2(id, currentUserId(), request));
    }

    @Operation(summary = "Preencher step 3 — prioridade, data e valor estimado")
    @PutMapping("/{id}/step/3")
    public ResponseEntity<SolicitationResponse> saveStep3(@PathVariable Long id,
                                                          @Valid @RequestBody Step3Request request) {
        return ResponseEntity.ok(solicitationService.saveStep3(id, currentUserId(), request));
    }

    @Operation(summary = "Submeter solicitação para análise")
    @Audit(action = "SUBMIT_SOLICITATION")
    @PostMapping("/{id}/submit")
    public ResponseEntity<SolicitationResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(solicitationService.submit(id, currentUserId()));
    }

    @Operation(summary = "Buscar solicitação por ID")
    @GetMapping("/{id}")
    public ResponseEntity<SolicitationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(solicitationService.getById(id, currentUserId()));
    }

    @Operation(summary = "Listar minhas solicitações")
    @GetMapping
    public ResponseEntity<List<SolicitationResponse>> list() {
        return ResponseEntity.ok(solicitationService.listByClient(currentUserId()));
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
