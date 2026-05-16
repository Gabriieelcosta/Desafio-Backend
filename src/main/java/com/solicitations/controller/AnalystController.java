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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analyst/solicitations")
@RequiredArgsConstructor
public class AnalystController {

    private final AnalystService analystService;
    private final SearchService searchService;
    private final AnalystCoverageRepository analystCoverageRepository;

    @GetMapping
    public ResponseEntity<List<SolicitationResponse>> list() {
        return ResponseEntity.ok(analystService.list(currentUserId()));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<SolicitationDocument>> search(SolicitationSearchParams params) {
        List<String> forcedStates = analystCoverageRepository.findStatesByAnalystId(currentUserId());
        return ResponseEntity.ok(searchService.search(params, forcedStates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(analystService.getById(id, currentUserId()));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<SolicitationResponse> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(analystService.startReview(id, currentUserId()));
    }

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
