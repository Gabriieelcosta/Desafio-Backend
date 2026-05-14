package com.solicitations.elasticsearch.repository;

import com.solicitations.elasticsearch.document.SolicitationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SolicitationSearchRepository extends ElasticsearchRepository<SolicitationDocument, String> {
}
