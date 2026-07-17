package com.todo.security;

import com.todo.exception.BadRequestException;
import com.todo.module.auth.model.User;
import com.todo.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the currently authenticated {@link User} from the Spring Security
 * context populated by {@link JwtAuthFilter}. Callers (services) must never
 * accept a userId from the request itself; it always comes from the JWT.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Aucun utilisateur authentifié");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Utilisateur authentifié introuvable"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
