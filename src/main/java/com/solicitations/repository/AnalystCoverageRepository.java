package com.solicitations.repository;

import com.solicitations.domain.entity.AnalystCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnalystCoverageRepository extends JpaRepository<AnalystCoverage, Long> {
    List<AnalystCoverage> findByAnalystId(Long analystId);
    void deleteByAnalystId(Long analystId);

    @Query("SELECT ac.state FROM AnalystCoverage ac WHERE ac.analyst.id = :analystId")
    List<String> findStatesByAnalystId(Long analystId);
}
