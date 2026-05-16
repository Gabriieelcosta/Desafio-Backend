package com.solicitations.controller;

import com.solicitations.dto.admin.AssignCoverageRequest;
import com.solicitations.dto.admin.CreateUserRequest;
import com.solicitations.dto.admin.UserResponse;
import com.solicitations.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @PutMapping("/users/{id}/coverage")
    public ResponseEntity<UserResponse> assignCoverage(@PathVariable Long id,
                                                       @Valid @RequestBody AssignCoverageRequest request) {
        return ResponseEntity.ok(adminService.assignCoverage(id, request));
    }
}
