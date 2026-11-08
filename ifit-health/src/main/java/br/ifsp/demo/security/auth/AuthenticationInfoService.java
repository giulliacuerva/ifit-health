package br.ifsp.demo.security.auth;

import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.repository.CustomerRepository;
import br.ifsp.demo.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationInfoService {

    private final CustomerRepository customerRepository;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Não foi possível identificar o usuário autenticado.");
        }

        return user;
    }

    public UUID getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

    public AuthenticatedUserResponse getAuthenticatedUserInfo() {

        User user = getAuthenticatedUser();
        UUID customerId = customerRepository
                .findByUserId(user.getId())
                .map(Customer::getId)
                .orElse(null);

        return new AuthenticatedUserResponse(
                user.getId(),
                customerId,
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getRole()
        );
    }
}