package com.solicitations.service;

import com.solicitations.domain.entity.Solicitation;
import com.solicitations.domain.entity.User;
import com.solicitations.domain.enums.SolicitationStatus;
import com.solicitations.dto.analyst.Decision;
import com.solicitations.dto.analyst.DecisionRequest;
import com.solicitations.dto.solicitation.SolicitationResponse;
import com.solicitations.exception.BusinessException;
import com.solicitations.exception.NotFoundException;
import com.solicitations.repository.AnalystCoverageRepository;
import com.solicitations.repository.SolicitationRepository;
import com.solicitations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalystService {

    private final SolicitationRepository solicitationRepository;
    private final AnalystCoverageRepository analystCoverageRepository;
    private final UserRepository userRepository;

    public SolicitationResponse getById(Long id, Long analystId) {
        Solicitation solicitation = findWithinCoverage(id, analystId);
        return SolicitationResponse.from(solicitation);
    }

    public List<SolicitationResponse> list(Long analystId) {
        List<String> states = getAnalystStates(analystId);
        return solicitationRepository.findByStateIn(states)
                .stream()
                .filter(s -> s.getStatus() != SolicitationStatus.DRAFT)
                .map(SolicitationResponse::from)
                .toList();
    }

    public SolicitationResponse startReview(Long id, Long analystId) {
        Solicitation solicitation = findWithinCoverage(id, analystId);

        if (solicitation.getStatus() != SolicitationStatus.SUBMITTED) {
            throw new BusinessException("Somente solicitações SUBMITTED podem ser iniciadas para revisão.");
        }

        solicitation.setStatus(SolicitationStatus.IN_REVIEW);
        return SolicitationResponse.from(solicitationRepository.save(solicitation));
    }

    public SolicitationResponse decide(Long id, Long analystId, DecisionRequest request) {
        Solicitation solicitation = findWithinCoverage(id, analystId);

        if (solicitation.getStatus() != SolicitationStatus.SUBMITTED
                && solicitation.getStatus() != SolicitationStatus.IN_REVIEW) {
            throw new BusinessException("Somente solicitações SUBMITTED ou IN_REVIEW podem ser decididas.");
        }

        User analyst = userRepository.findById(analystId)
                .orElseThrow(() -> new NotFoundException("Analista não encontrado."));

        SolicitationStatus newStatus = request.getDecision() == Decision.APPROVE
                ? SolicitationStatus.APPROVED
                : SolicitationStatus.REJECTED;

        solicitation.setStatus(newStatus);
        solicitation.setAnalysisComment(request.getComment());
        solicitation.setAnalyzedBy(analyst);
        solicitation.setAnalyzedAt(LocalDateTime.now());

        return SolicitationResponse.from(solicitationRepository.save(solicitation));
    }

    private Solicitation findWithinCoverage(Long id, Long analystId) {
        List<String> states = getAnalystStates(analystId);
        Solicitation solicitation = solicitationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Solicitação não encontrada."));

        if (solicitation.getState() == null || !states.contains(solicitation.getState())) {
            throw new BusinessException("Você não tem acesso a solicitações deste estado.");
        }

        return solicitation;
    }

    private List<String> getAnalystStates(Long analystId) {
        List<String> states = analystCoverageRepository.findStatesByAnalystId(analystId);
        if (states.isEmpty()) {
            throw new BusinessException("Analista não possui estados de cobertura configurados.");
        }
        return states;
    }
}
