package dev.gustavoesposar.reminder;

public class RegisterRequest {
    private String name;
    private String email;
    private String passwordOne;
    private String passwordTwo;
    private String birth;

    public RegisterRequest(String name, String email, String passwordOne, String passwordTwo, String birth) {
        this.name = name;
        this.email = email;
        this.passwordOne = passwordOne;
        this.passwordTwo = passwordTwo;
        this.birth = birth;
    }
}
