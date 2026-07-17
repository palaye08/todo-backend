package com.todo.module.auth.service;

import com.todo.module.auth.dto.AuthResponse;
import com.todo.module.auth.dto.LoginRequest;
import com.todo.module.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
