package com.todo.module.auth.controller;

import com.todo.dto.ApiResponse;
import com.todo.module.auth.dto.AuthResponse;
import com.todo.module.auth.dto.LoginRequest;
import com.todo.module.auth.dto.RegisterRequest;
import com.todo.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Inscription et authentification des utilisateurs")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Créer un nouveau compte utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authentifier un utilisateur existant")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
