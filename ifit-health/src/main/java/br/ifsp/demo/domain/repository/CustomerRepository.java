package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.model.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID customerId);

    Optional<Customer> findByCpf(String cpf);

    Optional<Customer> findByUserId(UUID userId);

    boolean existsByEmailIgnoreCase(String email);

    boolean attachUserToExistingCustomer(UUID customerId, UUID userId);
}
