package br.ifsp.demo.security.auth;

import br.ifsp.demo.domain.model.Address;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.repository.CustomerRepository;
import br.ifsp.demo.exception.EntityAlreadyExistsException;
import br.ifsp.demo.security.config.JwtService;
import br.ifsp.demo.security.user.JpaUserRepository;
import br.ifsp.demo.security.user.Role;
import br.ifsp.demo.security.user.User;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final JpaUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RegisterUserResponse register(RegisterUserRequest request) {
        String email = normalizeEmail(request.email());
        String cpf = CpfUtils.normalize(request.cpf());

        if (!CpfUtils.hasValidFormat(cpf)) {
            throw new IllegalArgumentException("O CPF deve conter exatamente 11 dígitos.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EntityAlreadyExistsException("Este e-mail já está cadastrado.");
        }

        Customer existingCustomer = customerRepository.findByCpf(cpf).orElse(null);
        if (existingCustomer != null) {
            if (existingCustomer.getUserId() != null) {
                throw new EntityAlreadyExistsException("Este CPF já está associado a uma conta.");
            }
            if (!normalizeEmail(existingCustomer.getEmail()).equals(email)) {
                throw new EntityAlreadyExistsException(
                        "O CPF já está cadastrado com outro e-mail. Procure a administração.");
            }
        } else if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new EntityAlreadyExistsException("Este e-mail já está associado a outro aluno.");
        }

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name(request.name().trim())
                .lastname(request.lastname().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        try {
            userRepository.saveAndFlush(user);
            UUID customerId;
            if (existingCustomer != null) {
                boolean linked = customerRepository.attachUserToExistingCustomer(
                        existingCustomer.getId(), userId);
                if (!linked) {
                    throw new EntityAlreadyExistsException(
                            "O cadastro do aluno já foi associado a outra conta. Tente novamente.");
                }
                customerId = existingCustomer.getId();
            } else {
                Customer customer = new Customer(
                        userId,
                        cpf,
                        request.name().trim() + " " + request.lastname().trim(),
                        request.birthdate(),
                        request.gender(),
                        blankToNull(request.tel()),
                        email,
                        toAddress(request.address())
                );
                customerRepository.save(customer);
                customerId = customer.getId();
            }

            return new RegisterUserResponse(userId, customerId);
        } catch (DataIntegrityViolationException exception) {
            throw new EntityAlreadyExistsException(
                    "Já existe um cadastro com este e-mail, CPF ou vínculo de usuário.");
        }
    }

    public AuthResponse authenticate(AuthRequest request) {
        String email = normalizeEmail(request.username());
        var authentication = new UsernamePasswordAuthenticationToken(email, request.password());
        authenticationManager.authenticate(authentication);

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    private Address toAddress(RegisterUserRequest.AddressRequest request) {
        if (request == null) {
            return null;
        }
        return new Address(
                request.cep().trim(),
                request.number().trim(),
                request.street().trim(),
                request.city().trim(),
                request.state().trim().toUpperCase(Locale.ROOT)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
