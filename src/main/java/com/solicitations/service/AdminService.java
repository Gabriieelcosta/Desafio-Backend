package com.solicitations.service;

import com.solicitations.domain.entity.AnalystCoverage;
import com.solicitations.domain.entity.User;
import com.solicitations.domain.enums.Role;
import com.solicitations.dto.admin.AssignCoverageRequest;
import com.solicitations.dto.admin.CreateUserRequest;
import com.solicitations.dto.admin.UserResponse;
import com.solicitations.exception.BusinessException;
import com.solicitations.exception.NotFoundException;
import com.solicitations.repository.AnalystCoverageRepository;
import com.solicitations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AnalystCoverageRepository analystCoverageRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(CreateUserRequest request) {
        if (request.getRole() == Role.CLIENT) {
            throw new BusinessException("Admins não podem criar usuários com role CLIENT.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);

        return UserResponse.from(user, List.of());
    }

    @Transactional
    public UserResponse assignCoverage(Long analystId, AssignCoverageRequest request) {
        User analyst = userRepository.findById(analystId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        if (analyst.getRole() != Role.ANALYST) {
            throw new BusinessException("Cobertura de UFs só pode ser atribuída a ANALISTs.");
        }

        List<String> normalizedStates = request.getStates().stream()
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .toList();

        analystCoverageRepository.deleteByAnalystId(analystId);

        List<AnalystCoverage> coverages = normalizedStates.stream().map(state -> {
            AnalystCoverage coverage = new AnalystCoverage();
            coverage.setAnalyst(analyst);
            coverage.setState(state);
            return coverage;
        }).toList();

        analystCoverageRepository.saveAll(coverages);

        return UserResponse.from(analyst, normalizedStates);
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    List<String> states = user.getRole() == Role.ANALYST
                            ? analystCoverageRepository.findStatesByAnalystId(user.getId())
                            : List.of();
                    return UserResponse.from(user, states);
                })
                .toList();
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        List<String> states = user.getRole() == Role.ANALYST
                ? analystCoverageRepository.findStatesByAnalystId(id)
                : List.of();

        return UserResponse.from(user, states);
    }
}
