package com.solicitations.controller;

import com.solicitations.dto.solicitation.SolicitationResponse;
import com.solicitations.dto.solicitation.Step1Request;
import com.solicitations.dto.solicitation.Step2Request;
import com.solicitations.dto.solicitation.Step3Request;
import com.solicitations.service.SolicitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitations")
@RequiredArgsConstructor
public class SolicitationController {

    private final SolicitationService solicitationService;

    @PostMapping
    public ResponseEntity<SolicitationResponse> create() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitationService.create(currentUserId()));
    }

    @PutMapping("/{id}/step/1")
    public ResponseEntity<SolicitationResponse> saveStep1(@PathVariable Long id,
                                                          @Valid @RequestBody Step1Request request) {
        return ResponseEntity.ok(solicitationService.saveStep1(id, currentUserId(), request));
    }

    @PutMapping("/{id}/step/2")
    public ResponseEntity<SolicitationResponse> saveStep2(@PathVariable Long id,
                                                          @Valid @RequestBody Step2Request request) {
        return ResponseEntity.ok(solicitationService.saveStep2(id, currentUserId(), request));
    }

    @PutMapping("/{id}/step/3")
    public ResponseEntity<SolicitationResponse> saveStep3(@PathVariable Long id,
                                                          @Valid @RequestBody Step3Request request) {
        return ResponseEntity.ok(solicitationService.saveStep3(id, currentUserId(), request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SolicitationResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(solicitationService.submit(id, currentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(solicitationService.getById(id, currentUserId()));
    }

    @GetMapping
    public ResponseEntity<List<SolicitationResponse>> list() {
        return ResponseEntity.ok(solicitationService.listByClient(currentUserId()));
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
