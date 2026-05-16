package com.solicitations.controller;

import com.solicitations.aop.Audit;
import com.solicitations.dto.analyst.DecisionRequest;
import com.solicitations.dto.search.PagedResponse;
import com.solicitations.dto.search.SolicitationSearchParams;
import com.solicitations.dto.solicitation.SolicitationResponse;
import com.solicitations.elasticsearch.document.SolicitationDocument;
import com.solicitations.repository.AnalystCoverageRepository;
import com.solicitations.service.AnalystService;
import com.solicitations.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Analista", description = "Revisão e decisão de solicitações dentro da cobertura do analista")
@RestController
@RequestMapping("/analyst/solicitations")
@RequiredArgsConstructor
public class AnalystController {

    private final AnalystService analystService;
    private final SearchService searchService;
    private final AnalystCoverageRepository analystCoverageRepository;

    @Operation(summary = "Listar solicitações da cobertura do analista")
    @GetMapping
    public ResponseEntity<List<SolicitationResponse>> list() {
        return ResponseEntity.ok(analystService.list(currentUserId()));
    }

    @Operation(summary = "Buscar solicitações com filtros via Elasticsearch")
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<SolicitationDocument>> search(SolicitationSearchParams params) {
        List<String> forcedStates = analystCoverageRepository.findStatesByAnalystId(currentUserId());
        return ResponseEntity.ok(searchService.search(params, forcedStates));
    }

    @Operation(summary = "Buscar solicitação por ID")
    @GetMapping("/{id}")
    public ResponseEntity<SolicitationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(analystService.getById(id, currentUserId()));
    }

    @Operation(summary = "Iniciar revisão de uma solicitação")
    @PostMapping("/{id}/start")
    public ResponseEntity<SolicitationResponse> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(analystService.startReview(id, currentUserId()));
    }

    @Operation(summary = "Aprovar ou rejeitar uma solicitação")
    @Audit(action = "DECIDE_SOLICITATION")
    @PostMapping("/{id}/decide")
    public ResponseEntity<SolicitationResponse> decide(@PathVariable Long id,
                                                       @Valid @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(analystService.decide(id, currentUserId(), request));
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
