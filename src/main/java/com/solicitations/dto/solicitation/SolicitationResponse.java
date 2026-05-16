package com.solicitations.dto.solicitation;

import com.solicitations.domain.entity.Solicitation;
import com.solicitations.domain.enums.Priority;
import com.solicitations.domain.enums.ServiceType;
import com.solicitations.domain.enums.SolicitationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SolicitationResponse {

    private Long id;
    private Long clientId;
    private SolicitationStatus status;
    private int currentStep;

    private ServiceType serviceType;
    private String title;
    private String description;

    private String cep;
    private String number;
    private String complement;
    private String street;
    private String neighborhood;
    private String city;
    private String state;

    private Priority priority;
    private LocalDate preferredDate;
    private BigDecimal estimatedValue;
    private Boolean termsAccepted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime analyzedAt;
    private Long analyzedById;
    private String analysisComment;

    public static SolicitationResponse from(Solicitation s) {
        return SolicitationResponse.builder()
                .id(s.getId())
                .clientId(s.getClient().getId())
                .status(s.getStatus())
                .currentStep(s.getCurrentStep())
                .serviceType(s.getServiceType())
                .title(s.getTitle())
                .description(s.getDescription())
                .cep(s.getCep())
                .number(s.getNumber())
                .complement(s.getComplement())
                .street(s.getStreet())
                .neighborhood(s.getNeighborhood())
                .city(s.getCity())
                .state(s.getState())
                .priority(s.getPriority())
                .preferredDate(s.getPreferredDate())
                .estimatedValue(s.getEstimatedValue())
                .termsAccepted(s.getTermsAccepted())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .submittedAt(s.getSubmittedAt())
                .analyzedAt(s.getAnalyzedAt())
                .analyzedById(s.getAnalyzedBy() != null ? s.getAnalyzedBy().getId() : null)
                .analysisComment(s.getAnalysisComment())
                .build();
    }
}
