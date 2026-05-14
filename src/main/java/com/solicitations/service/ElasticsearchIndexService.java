package com.solicitations.service;

import com.solicitations.domain.entity.Solicitation;
import com.solicitations.elasticsearch.document.SolicitationDocument;
import com.solicitations.elasticsearch.repository.SolicitationSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchIndexService {

    private final SolicitationSearchRepository searchRepository;

    public void index(Solicitation solicitation) {
        searchRepository.save(SolicitationDocument.from(solicitation));
    }
}
