package com.solicitations.service;

import com.solicitations.domain.entity.Solicitation;
import com.solicitations.domain.entity.User;
import com.solicitations.domain.enums.Priority;
import com.solicitations.domain.enums.SolicitationStatus;
import com.solicitations.dto.solicitation.SolicitationResponse;
import com.solicitations.dto.solicitation.Step1Request;
import com.solicitations.dto.solicitation.Step2Request;
import com.solicitations.dto.solicitation.Step3Request;
import com.solicitations.exception.BusinessException;
import com.solicitations.exception.NotFoundException;
import com.solicitations.repository.SolicitationRepository;
import com.solicitations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitationService {

    private final SolicitationRepository solicitationRepository;
    private final UserRepository userRepository;

    public SolicitationResponse create(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        Solicitation solicitation = new Solicitation();
        solicitation.setClient(client);

        return SolicitationResponse.from(solicitationRepository.save(solicitation));
    }

    public SolicitationResponse saveStep1(Long id, Long clientId, Step1Request request) {
        Solicitation solicitation = findOwnedDraft(id, clientId);

        solicitation.setServiceType(request.getServiceType());
        solicitation.setTitle(request.getTitle().trim());
        solicitation.setDescription(request.getDescription().trim());

        if (solicitation.getCurrentStep() < 1) {
            solicitation.setCurrentStep(1);
        }

        return SolicitationResponse.from(solicitationRepository.save(solicitation));
    }

    public SolicitationResponse saveStep2(Long id, Long clientId, Step2Request request) {
        Solicitation solicitation = findOwnedDraft(id, clientId);

        if (solicitation.getCurrentStep() < 1) {
            throw new BusinessException("Complete o Step 1 antes de prosseguir.");
        }

        String normalizedCep = request.getCep().replaceAll("[^0-9]", "");
        solicitation.setCep(normalizedCep);
        solicitation.setNumber(request.getNumber());
        solicitation.setComplement(request.getComplement());

        return SolicitationResponse.from(solicitationRepository.save(solicitation));
    }

    public SolicitationResponse saveStep3(Long id, Long clientId, Step3Request request) {
        Solicitation solicitation = findOwnedDraft(id, clientId);

        if (solicitation.getCurrentStep() < 2) {
            throw new BusinessException("Complete o Step 2 antes de prosseguir.");
        }

        if (!Boolean.TRUE.equals(request.getTermsAccepted())) {
            throw new BusinessException("Os termos devem ser aceitos para concluir o Step 3.");
        }

        if (request.getPreferredDate().isBefore(LocalDate.now())) {
            throw new BusinessException("preferredDate não pode ser no passado.");
        }

        if (request.getPriority() == Priority.HIGH
                && request.getEstimatedValue().compareTo(new BigDecimal("100")) < 0) {
            throw new BusinessException("Para prioridade HIGH, estimatedValue deve ser >= 100.");
        }

        solicitation.setPriority(request.getPriority());
        solicitation.setPreferredDate(request.getPreferredDate());
        solicitation.setEstimatedValue(request.getEstimatedValue());
        solicitation.setTermsAccepted(request.getTermsAccepted());
        solicitation.setCurrentStep(3);

        return SolicitationResponse.from(solicitationRepository.save(solicitation));
    }

    public SolicitationResponse submit(Long id, Long clientId) {
        Solicitation solicitation = findOwnedDraft(id, clientId);

        validateForSubmit(solicitation);

        solicitation.setStatus(SolicitationStatus.SUBMITTED);
        solicitation.setSubmittedAt(LocalDateTime.now());

        return SolicitationResponse.from(solicitationRepository.save(solicitation));
    }

    public SolicitationResponse getById(Long id, Long clientId) {
        return SolicitationResponse.from(
                solicitationRepository.findByIdAndClientId(id, clientId)
                        .orElseThrow(() -> new NotFoundException("Solicitação não encontrada."))
        );
    }

    public List<SolicitationResponse> listByClient(Long clientId) {
        return solicitationRepository.findByClientId(clientId)
                .stream()
                .map(SolicitationResponse::from)
                .toList();
    }

    private Solicitation findOwnedDraft(Long id, Long clientId) {
        Solicitation solicitation = solicitationRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new NotFoundException("Solicitação não encontrada."));

        if (solicitation.getStatus() != SolicitationStatus.DRAFT) {
            throw new BusinessException("Somente solicitações em DRAFT podem ser editadas.");
        }

        return solicitation;
    }

    private void validateForSubmit(Solicitation s) {
        List<String> errors = new ArrayList<>();

        if (s.getServiceType() == null) errors.add("serviceType é obrigatório");
        if (s.getTitle() == null || s.getTitle().trim().length() < 3) errors.add("title inválido");
        if (s.getDescription() == null || s.getDescription().trim().length() < 20) errors.add("description inválida");

        if (s.getCep() == null) errors.add("cep é obrigatório");
        if (s.getNumber() == null) errors.add("number é obrigatório");
        if (s.getStreet() == null) errors.add("street é obrigatório");
        if (s.getNeighborhood() == null) errors.add("neighborhood é obrigatório");
        if (s.getCity() == null) errors.add("city é obrigatório");
        if (s.getState() == null) errors.add("state é obrigatório");

        if (s.getPriority() == null) errors.add("priority é obrigatório");
        if (s.getPreferredDate() == null) errors.add("preferredDate é obrigatório");
        else if (s.getPreferredDate().isBefore(LocalDate.now())) errors.add("preferredDate não pode ser no passado");
        if (s.getEstimatedValue() == null || s.getEstimatedValue().compareTo(BigDecimal.ZERO) < 0)
            errors.add("estimatedValue deve ser >= 0");
        if (!Boolean.TRUE.equals(s.getTermsAccepted())) errors.add("termsAccepted deve ser true");

        if (s.getPriority() == Priority.HIGH && s.getEstimatedValue() != null
                && s.getEstimatedValue().compareTo(new BigDecimal("100")) < 0) {
            errors.add("prioridade HIGH requer estimatedValue >= 100");
        }

        if (!errors.isEmpty()) {
            throw new BusinessException("Solicitação incompleta: " + String.join(", ", errors));
        }
    }
}
