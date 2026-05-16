package com.solicitations.controller;

import com.solicitations.aop.Audit;
import com.solicitations.dto.admin.AssignCoverageRequest;
import com.solicitations.dto.admin.CreateUserRequest;
import com.solicitations.dto.admin.UserResponse;
import com.solicitations.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "Gerenciamento de usuários e cobertura de UFs por analista")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Criar usuário (ANALYST ou ADMIN)")
    @Audit(action = "CREATE_USER")
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @Operation(summary = "Listar todos os usuários")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @Operation(summary = "Buscar usuário por ID")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @Operation(summary = "Atribuir cobertura de UFs a um analista")
    @PutMapping("/users/{id}/coverage")
    public ResponseEntity<UserResponse> assignCoverage(@PathVariable Long id,
                                                       @Valid @RequestBody AssignCoverageRequest request) {
        return ResponseEntity.ok(adminService.assignCoverage(id, request));
    }
}
