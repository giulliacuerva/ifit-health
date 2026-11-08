package br.ifsp.demo.persistence.sqlite;

import br.ifsp.demo.domain.model.Address;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.enums.Gender;
import br.ifsp.demo.domain.repository.CustomerRepository;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SqliteCustomerRepository implements CustomerRepository {

    private static final String SELECT_CUSTOMER = """
            SELECT id, user_id, cpf, name, birthdate, gender, tel, email,
                   address_cep, address_number, address_street, address_city,
                   address_state, created_at, updated_at
            FROM customers
            """;

    private final DataSource dataSource;

    public SqliteCustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Customer save(Customer customer) {
        final String sql = """
                INSERT INTO customers (
                    id, user_id, cpf, name, birthdate, gender, tel, email,
                    address_cep, address_number, address_street, address_city,
                    address_state
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    user_id = excluded.user_id,
                    cpf = excluded.cpf,
                    name = excluded.name,
                    birthdate = excluded.birthdate,
                    gender = excluded.gender,
                    tel = excluded.tel,
                    email = excluded.email,
                    address_cep = excluded.address_cep,
                    address_number = excluded.address_number,
                    address_street = excluded.address_street,
                    address_city = excluded.address_city,
                    address_state = excluded.address_state,
                    updated_at = CURRENT_TIMESTAMP
                """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getId().toString());
            setNullableUuid(statement, 2, customer.getUserId());
            statement.setString(3, customer.getCpf());
            statement.setString(4, customer.getName());
            statement.setString(5, customer.getBirthdate().toString());
            statement.setString(6, customer.getGender().name());
            statement.setString(7, customer.getTel());
            statement.setString(8, customer.getEmail());
            setAddress(statement, customer.getAddress());

            statement.executeUpdate();
            return customer;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving customer", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        return findOneByUuid(" WHERE id = ? LIMIT 1", customerId,
                "Error finding customer by id");
    }

    @Override
    public Optional<Customer> findByCpf(String cpf) {
        final String sql = SELECT_CUSTOMER + " WHERE cpf = ? LIMIT 1";
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cpf);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapCustomer(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by CPF", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Optional<Customer> findByUserId(UUID userId) {
        return findOneByUuid(" WHERE user_id = ? LIMIT 1", userId,
                "Error finding customer by user id");
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        final String sql = "SELECT 1 FROM customers WHERE email = ? COLLATE NOCASE LIMIT 1";
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking customer email", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public boolean attachUserToExistingCustomer(UUID customerId, UUID userId) {
        final String sql = """
                UPDATE customers
                SET user_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND user_id IS NULL
                """;
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId.toString());
            statement.setString(2, customerId.toString());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error linking user to customer", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private Optional<Customer> findOneByUuid(String whereClause, UUID id, String errorMessage) {
        final String sql = SELECT_CUSTOMER + whereClause;
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapCustomer(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setNullableUuid(PreparedStatement statement, int index, UUID value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.toString());
        }
    }

    private void setAddress(PreparedStatement statement, Address address) throws SQLException {
        if (address == null) {
            statement.setNull(9, Types.VARCHAR);
            statement.setNull(10, Types.VARCHAR);
            statement.setNull(11, Types.VARCHAR);
            statement.setNull(12, Types.VARCHAR);
            statement.setNull(13, Types.VARCHAR);
            return;
        }

        statement.setString(9, address.getCep());
        statement.setString(10, address.getNumber());
        statement.setString(11, address.getStreet());
        statement.setString(12, address.getCity());
        statement.setString(13, address.getState());
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        String userIdValue = rs.getString("user_id");

        return new Customer(
                UUID.fromString(rs.getString("id")),
                userIdValue == null ? null : UUID.fromString(userIdValue),
                rs.getString("cpf"),
                rs.getString("name"),
                LocalDate.parse(rs.getString("birthdate")),
                Gender.valueOf(rs.getString("gender")),
                rs.getString("tel"),
                rs.getString("email"),
                mapAddress(rs),
                parseDateTime(rs.getString("created_at")),
                parseDateTime(rs.getString("updated_at"))
        );
    }

    private Address mapAddress(ResultSet rs) throws SQLException {
        String cep = rs.getString("address_cep");
        String number = rs.getString("address_number");
        String street = rs.getString("address_street");
        String city = rs.getString("address_city");
        String state = rs.getString("address_state");

        if (cep == null && number == null && street == null && city == null && state == null) {
            return null;
        }

        return new Address(cep, number, street, city, state);
    }

    private LocalDateTime parseDateTime(String value) {
        return value == null ? null : LocalDateTime.parse(value.replace(" ", "T"));
    }
}
