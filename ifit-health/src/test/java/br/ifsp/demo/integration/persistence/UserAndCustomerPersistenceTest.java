package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.domain.model.Address;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.enums.Gender;
import br.ifsp.demo.security.user.JpaUserRepository;
import br.ifsp.demo.security.user.Role;
import br.ifsp.demo.security.user.User;
import br.ifsp.demo.support.DbFixtures;
import br.ifsp.demo.support.FakeData;
import br.ifsp.demo.support.PersistenceTestBase;
import br.ifsp.demo.support.SeedData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("PersistenceTest")
@Tag("IntegrationTest")
@DisplayName("Persistência de contas (JpaUserRepository) e clientes (SqliteCustomerRepository)")
class UserAndCustomerPersistenceTest extends PersistenceTestBase {
    @Autowired
    private JpaUserRepository userRepository;

    private User newUser(String email) {
        return User.builder().id(UUID.randomUUID()).name(FakeData.firstName()).lastname(FakeData.lastName())
                .email(email).password("hash").role(Role.USER).build();
    }

    private Customer newCustomer(UUID userId) {
        Address address = new Address("13560-000", "10", FakeData.faker().address().streetName(), "São Carlos", "SP");
        return new Customer(userId, FakeData.cpf(), FakeData.firstName() + " " + FakeData.lastName(),
                FakeData.birthdate(), Gender.FEMALE, FakeData.phone(), FakeData.email(), address);
    }

    @Test
    @DisplayName("encontra a conta pelo e-mail sem diferenciar maiúsculas")
    void shouldFindUserIgnoringCase() {
        assertThat(userRepository.findByEmailIgnoreCase("ADMIN@iFit.com"))
                .get()
                .extracting(User::getId, User::getRole)
                .containsExactly(SeedData.ADMIN_USER_ID, Role.ADMIN);
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase responde true só para e-mails existentes")
    void shouldCheckIfEmailExists() {
        assertThat(userRepository.existsByEmailIgnoreCase(SeedData.ANA_EMAIL.toUpperCase())).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase(FakeData.email())).isFalse();
    }

    @Test
    @DisplayName("grava uma nova conta com saveAndFlush")
    void shouldSaveUser() {
        User user = newUser(FakeData.email());

        userRepository.saveAndFlush(user);

        assertThat(DbFixtures.text("SELECT role FROM app_user WHERE id = ?", user.getId().toString())).isEqualTo("USER");
        assertThat(userRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("falha ao gravar e-mail que só muda nas maiúsculas")
    void shouldRejectDuplicatedEmail() {
        assertThatThrownBy(() -> userRepository.saveAndFlush(newUser(SeedData.ADMIN_EMAIL.toUpperCase())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("mantém todos os campos do cliente, inclusive o endereço")
    void shouldSaveAndFindCustomer() {
        Customer customer = newCustomer(null);

        customerRepository.save(customer);
        Customer found = customer(customer.getId());

        assertThat(found.getCpf()).isEqualTo(customer.getCpf());
        assertThat(found.getName()).isEqualTo(customer.getName());
        assertThat(found.getEmail()).isEqualTo(customer.getEmail());
        assertThat(found.getBirthdate()).isEqualTo(customer.getBirthdate());
        assertThat(found.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(found.getAddress().getCity()).isEqualTo("São Carlos");
        assertThat(found.getUserId()).isNull();
    }

    @Test
    @DisplayName("atualiza cliente existente ao salvar (ON CONFLICT)")
    void shouldUpdateExistingCustomer() {
        Customer ana = customer(SeedData.ANA_CUSTOMER_ID);
        Customer changed = new Customer(ana.getId(), ana.getUserId(), ana.getCpf(), "Ana Souza Lima", ana.getBirthdate(),
                ana.getGender(), ana.getTel(), ana.getEmail(), ana.getAddress(), ana.getCreatedAt(), ana.getUpdatedAt());

        customerRepository.save(changed);

        assertThat(customer(SeedData.ANA_CUSTOMER_ID).getName()).isEqualTo("Ana Souza Lima");
        assertThat(DbFixtures.count("SELECT COUNT(*) FROM customers")).isEqualTo(2);
    }

    @Test
    @DisplayName("retorna vazio ao buscar cliente com id desconhecido")
    void shouldReturnEmptyForUnknownId() {
        assertThat(customerRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("encontra o cliente pelo CPF")
    void shouldFindByCpf() {
        assertThat(customerRepository.findByCpf(SeedData.ANA_CPF)).get()
                .extracting(Customer::getId).isEqualTo(SeedData.ANA_CUSTOMER_ID);
        assertThat(customerRepository.findByCpf(FakeData.cpf())).isEmpty();
    }

    @Test
    @DisplayName("encontra o cliente vinculado à conta")
    void shouldFindByUserId() {
        assertThat(customerRepository.findByUserId(SeedData.ANA_USER_ID)).get()
                .extracting(Customer::getId).isEqualTo(SeedData.ANA_CUSTOMER_ID);
        assertThat(customerRepository.findByUserId(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("ignora maiúsculas ao verificar e-mail de cliente")
    void shouldCheckEmailIgnoringCase() {
        assertThat(customerRepository.existsByEmailIgnoreCase(SeedData.ANA_EMAIL.toUpperCase())).isTrue();
        assertThat(customerRepository.existsByEmailIgnoreCase(FakeData.email())).isFalse();
    }

    @Test
    @DisplayName("vincula conta só a cliente que ainda não tem conta")
    void shouldAttachUserOnlyOnce() {
        UUID customerId = DbFixtures.insertCustomer();
        User user = userRepository.saveAndFlush(newUser(FakeData.email()));
        User other = userRepository.saveAndFlush(newUser(FakeData.email()));

        assertThat(customerRepository.attachUserToExistingCustomer(customerId, user.getId())).isTrue();
        assertThat(customerRepository.attachUserToExistingCustomer(customerId, other.getId())).isFalse();
        assertThat(customer(customerId).getUserId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("falha ao gravar cliente com CPF que já existe")
    void shouldRejectDuplicatedCpf() {
        Customer duplicated = newCustomer(null);
        Customer copy = new Customer(null, SeedData.ANA_CPF, duplicated.getName(), duplicated.getBirthdate(),
                duplicated.getGender(), null, FakeData.email(), null);

        assertThatThrownBy(() -> customerRepository.save(copy)).isInstanceOf(RuntimeException.class);
        assertThat(DbFixtures.count("SELECT COUNT(*) FROM customers")).isEqualTo(2);
    }
}
