package br.ifsp.demo.security.auth;

public final class CpfUtils {

    private CpfUtils() {
    }

    public static String normalize(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public static boolean hasValidFormat(String cpf) {
        String normalized = normalize(cpf);
        return normalized != null && normalized.matches("\\d{11}");
    }
}
