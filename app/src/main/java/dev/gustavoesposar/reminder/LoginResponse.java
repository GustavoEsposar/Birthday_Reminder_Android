package dev.gustavoesposar.reminder;

public class LoginResponse {
    private String token;
    private String name;
    private String birth;

    public String getToken() {
        return this.token;
    }

    public String getName() {
        return this.name;
    }

    public String getBirth() {
        return this.birth;
    }
}
