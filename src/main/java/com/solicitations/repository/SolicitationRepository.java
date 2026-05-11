package com.solicitations.repository;

import com.solicitations.domain.entity.Solicitation;
import com.solicitations.domain.enums.SolicitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitationRepository extends JpaRepository<Solicitation, Long> {
    List<Solicitation> findByClientId(Long clientId);
    Optional<Solicitation> findByIdAndClientId(Long id, Long clientId);
    List<Solicitation> findByStateInAndStatus(List<String> states, SolicitationStatus status);
    List<Solicitation> findByStateIn(List<String> states);
}
