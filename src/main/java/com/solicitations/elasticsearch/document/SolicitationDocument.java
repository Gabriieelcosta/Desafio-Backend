package com.solicitations.elasticsearch.document;

import com.solicitations.domain.entity.Solicitation;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(indexName = "solicitations")
public class SolicitationDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long clientId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String serviceType;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String priority;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime submittedAt;

    public static SolicitationDocument from(Solicitation s) {
        return SolicitationDocument.builder()
                .id(String.valueOf(s.getId()))
                .clientId(s.getClient().getId())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .serviceType(s.getServiceType() != null ? s.getServiceType().name() : null)
                .title(s.getTitle())
                .description(s.getDescription())
                .state(s.getState())
                .city(s.getCity())
                .priority(s.getPriority() != null ? s.getPriority().name() : null)
                .createdAt(s.getCreatedAt())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
