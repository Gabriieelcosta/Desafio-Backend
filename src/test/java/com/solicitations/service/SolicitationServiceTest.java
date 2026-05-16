package com.solicitations.service;

import com.solicitations.domain.entity.Solicitation;
import com.solicitations.domain.entity.User;
import com.solicitations.domain.enums.Priority;
import com.solicitations.domain.enums.SolicitationStatus;
import com.solicitations.dto.solicitation.Step2Request;
import com.solicitations.dto.solicitation.Step3Request;
import com.solicitations.exception.BusinessException;
import com.solicitations.exception.NotFoundException;
import com.solicitations.integration.viacep.ViaCepClient;
import com.solicitations.repository.SolicitationRepository;
import com.solicitations.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitationServiceTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViaCepClient viaCepClient;

    @Mock
    private ElasticsearchIndexService elasticsearchIndexService;

    @InjectMocks
    private SolicitationService solicitationService;

    private Solicitation draftSolicitation(int currentStep) {
        Solicitation s = new Solicitation();
        s.setClient(new User());
        s.setCurrentStep(currentStep);
        s.setStatus(SolicitationStatus.DRAFT);
        return s;
    }

    @Test
    void saveStep2_throwsWhenStep1NotComplete() {
        when(solicitationRepository.findByIdAndClientId(1L, 1L))
                .thenReturn(Optional.of(draftSolicitation(0)));

        Step2Request request = new Step2Request();
        request.setCep("01001000");
        request.setNumber("100");

        assertThatThrownBy(() -> solicitationService.saveStep2(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Complete o Step 1 antes de prosseguir.");
    }

    @Test
    void saveStep3_throwsWhenTermsNotAccepted() {
        when(solicitationRepository.findByIdAndClientId(1L, 1L))
                .thenReturn(Optional.of(draftSolicitation(2)));

        Step3Request request = new Step3Request();
        request.setPriority(Priority.LOW);
        request.setPreferredDate(LocalDate.now().plusDays(5));
        request.setEstimatedValue(new BigDecimal("50"));
        request.setTermsAccepted(false);

        assertThatThrownBy(() -> solicitationService.saveStep3(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Os termos devem ser aceitos para concluir o Step 3.");
    }

    @Test
    void saveStep3_throwsWhenHighPriorityAndEstimatedValueBelow100() {
        when(solicitationRepository.findByIdAndClientId(1L, 1L))
                .thenReturn(Optional.of(draftSolicitation(2)));

        Step3Request request = new Step3Request();
        request.setPriority(Priority.HIGH);
        request.setPreferredDate(LocalDate.now().plusDays(5));
        request.setEstimatedValue(new BigDecimal("50"));
        request.setTermsAccepted(true);

        assertThatThrownBy(() -> solicitationService.saveStep3(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Para prioridade HIGH, estimatedValue deve ser >= 100.");
    }

    @Test
    void saveStep3_throwsWhenPreferredDateIsInThePast() {
        when(solicitationRepository.findByIdAndClientId(1L, 1L))
                .thenReturn(Optional.of(draftSolicitation(2)));

        Step3Request request = new Step3Request();
        request.setPriority(Priority.LOW);
        request.setPreferredDate(LocalDate.now().minusDays(1));
        request.setEstimatedValue(new BigDecimal("50"));
        request.setTermsAccepted(true);

        assertThatThrownBy(() -> solicitationService.saveStep3(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("preferredDate não pode ser no passado.");
    }

    @Test
    void submit_throwsWhenSolicitationIsNotDraft() {
        Solicitation s = new Solicitation();
        s.setClient(new User());
        s.setStatus(SolicitationStatus.SUBMITTED);
        when(solicitationRepository.findByIdAndClientId(1L, 1L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> solicitationService.submit(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Somente solicitações em DRAFT podem ser editadas.");
    }

    @Test
    void submit_throwsWhenRequiredFieldsAreMissing() {
        when(solicitationRepository.findByIdAndClientId(1L, 1L))
                .thenReturn(Optional.of(draftSolicitation(0)));

        assertThatThrownBy(() -> solicitationService.submit(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solicitação incompleta");
    }

    @Test
    void getById_throwsWhenSolicitationNotFound() {
        when(solicitationRepository.findByIdAndClientId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitationService.getById(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Solicitação não encontrada.");
    }
}
