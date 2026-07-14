package com.example.carforum.models;

import jakarta.validation.constraints.NotBlank;

public class LoginDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public LoginDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}