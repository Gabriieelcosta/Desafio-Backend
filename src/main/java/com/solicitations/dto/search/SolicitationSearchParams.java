package com.solicitations.dto.search;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SolicitationSearchParams {

    private String q;
    private List<String> status;
    private String serviceType;
    private String priority;
    private String state;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private int page = 0;
    private int size = 20;
    private String sort = "submittedAt,desc";
}
