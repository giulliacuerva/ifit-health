package br.ifsp.demo.support;

import com.github.javafaker.Faker;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class FakeData {
    private static final Faker FAKER = new Faker(new Locale("pt-BR"));

    private FakeData() {
    }

    public static Faker faker() {
        return FAKER;
    }

    public static String firstName() {
        return FAKER.name().firstName();
    }

    public static String lastName() {
        return FAKER.name().lastName();
    }

    public static String email() {
        String user = FAKER.name().username().replaceAll("[^a-zA-Z0-9.]", "");
        return (user.isBlank() ? "aluno" : user) + "." + UUID.randomUUID().toString().substring(0, 8) + "@ifit-test.com";
    }

    public static String password() {
        return FAKER.internet().password(8, 20, true, true);
    }

    public static String cpf() {
        int[] digits = new int[11];
        do {
            for (int i = 0; i < 9; i++) {
                digits[i] = FAKER.number().numberBetween(0, 10);
            }
        } while (allEqual(digits));
        digits[9] = checkDigit(digits, 9);
        digits[10] = checkDigit(digits, 10);
        StringBuilder cpf = new StringBuilder();
        for (int digit : digits) {
            cpf.append(digit);
        }
        return cpf.toString();
    }

    public static String formattedCpf(String cpf) {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public static LocalDate birthdate() {
        return FAKER.date().birthday(18, 70).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate futureDate() {
        return FAKER.date().future(365, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .plusDays(1);
    }

    public static String gender() {
        return FAKER.bool().bool() ? "MALE" : "FEMALE";
    }

    public static String phone() {
        return "169" + FAKER.number().digits(8);
    }

    public static Map<String, Object> address() {
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("cep", FAKER.number().digits(5) + "-" + FAKER.number().digits(3));
        address.put("number", FAKER.address().buildingNumber());
        address.put("street", FAKER.address().streetName());
        address.put("city", FAKER.address().city());
        address.put("state", "SP");
        return address;
    }

    public static Map<String, Object> registerRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", firstName());
        body.put("lastname", lastName());
        body.put("email", email());
        body.put("password", password());
        body.put("cpf", cpf());
        body.put("birthdate", birthdate().toString());
        body.put("gender", gender());
        body.put("tel", phone());
        return body;
    }

    private static boolean allEqual(int[] digits) {
        for (int i = 1; i < 9; i++) {
            if (digits[i] != digits[0]) {
                return false;
            }
        }
        return true;
    }

    private static int checkDigit(int[] digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * (length + 1 - i);
        }
        int rest = (sum * 10) % 11;
        return rest == 10 ? 0 : rest;
    }
}
