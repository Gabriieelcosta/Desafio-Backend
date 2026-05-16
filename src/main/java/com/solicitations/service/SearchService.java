package com.solicitations.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.solicitations.dto.search.PagedResponse;
import com.solicitations.dto.search.SolicitationSearchParams;
import com.solicitations.elasticsearch.document.SolicitationDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public PagedResponse<SolicitationDocument> search(SolicitationSearchParams params, List<String> forcedStates) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(buildQuery(params, forcedStates))
                .withPageable(PageRequest.of(params.getPage(), params.getSize()))
                .withSort(s -> s.field(f -> f
                        .field(parseSortField(params.getSort()))
                        .order(parseSortOrder(params.getSort()))))
                .build();

        SearchHits<SolicitationDocument> hits = elasticsearchOperations.search(query, SolicitationDocument.class);

        List<SolicitationDocument> items = hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .toList();

        return PagedResponse.<SolicitationDocument>builder()
                .items(items)
                .page(params.getPage())
                .size(params.getSize())
                .total(hits.getTotalHits())
                .build();
    }

    private Query buildQuery(SolicitationSearchParams params, List<String> forcedStates) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (params.getQ() != null && !params.getQ().isBlank()) {
            bool.must(m -> m.multiMatch(mm -> mm
                    .query(params.getQ())
                    .fields("title", "description")));
        }

        if (params.getStatus() != null && !params.getStatus().isEmpty()) {
            bool.filter(f -> f.terms(t -> t
                    .field("status")
                    .terms(tv -> tv.value(params.getStatus().stream()
                            .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                            .toList()))));
        }

        if (params.getServiceType() != null) {
            bool.filter(f -> f.term(t -> t.field("serviceType").value(params.getServiceType())));
        }

        if (params.getPriority() != null) {
            bool.filter(f -> f.term(t -> t.field("priority").value(params.getPriority())));
        }

        List<String> stateFilter = forcedStates != null ? forcedStates
                : (params.getState() != null ? List.of(params.getState()) : null);

        if (stateFilter != null && !stateFilter.isEmpty()) {
            bool.filter(f -> f.terms(t -> t
                    .field("state")
                    .terms(tv -> tv.value(stateFilter.stream()
                            .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                            .toList()))));
        }

        if (params.getDateFrom() != null) {
            bool.filter(f -> f.range(r -> r
                    .field("submittedAt")
                    .gte(JsonData.of(params.getDateFrom().toString()))));
        }

        if (params.getDateTo() != null) {
            bool.filter(f -> f.range(r -> r
                    .field("submittedAt")
                    .lte(JsonData.of(params.getDateTo().toString()))));
        }

        return Query.of(q -> q.bool(bool.build()));
    }

    private String parseSortField(String sort) {
        if (sort == null || !sort.contains(",")) return "submittedAt";
        return sort.split(",")[0].trim();
    }

    private SortOrder parseSortOrder(String sort) {
        if (sort == null || !sort.contains(",")) return SortOrder.Desc;
        return sort.split(",")[1].trim().equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
    }
}
