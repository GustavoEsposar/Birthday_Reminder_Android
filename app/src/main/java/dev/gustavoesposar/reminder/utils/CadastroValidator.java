package dev.gustavoesposar.reminder.utils;

import android.util.Patterns;

public class CadastroValidator {

    public static void validarNome(String nome) throws IllegalArgumentException {
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome não pode estar vazio.");
        }
        if (nome.length() < 3) {
            throw new IllegalArgumentException("O nome deve ter pelo menos 3 caracteres.");
        }
    }

    public static void validarEmail(String email) throws IllegalArgumentException {
        if (email.isEmpty()) {
            throw new IllegalArgumentException("O email não pode estar vazio.");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new IllegalArgumentException("Por favor, insira um email válido.");
        }
    }

    public static void validarSenha(String senhaUm, String senhaDois) throws IllegalArgumentException {
        if (senhaUm.isEmpty() || senhaDois.isEmpty()) {
            throw new IllegalArgumentException("As senhas não podem estar vazias.");
        }
        if (!senhaUm.equals(senhaDois)) {
            throw new IllegalArgumentException("As senhas não são iguais.");
        }
        if (senhaUm.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
    }

    public static void validarData(String data) throws IllegalArgumentException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("A data de nascimento não pode estar vazia.");
        }
    }

    /**
     * Formata a data de nascimento para o formato "yyyy-MM-dd"
     * @param birthdate : "dd/MM/yyyy"
     * @return
     */
    public static String formatBirthdate(String birthdate) {
        String year = birthdate.substring(0, 4);
        String month = birthdate.substring(5, 7);
        String day = birthdate.substring(8, 10);
        return year + "-" + month + "-" + day;
    }
}
