package com.example.carforum.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserUpdateDto {
    @NotBlank
    private String password;

    @NotBlank
    @Size(min = 4, max = 32)
    private String firstName;

    @NotBlank
    @Size(min = 4, max = 32)
    private String lastName;

    @NotBlank
    private String email;

    @Pattern(
            regexp = "^$|^\\+?[0-9\\-\\s()]{7,20}$",
            message = "Invalid phone number format."
    )
    private String phoneNumber;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


}
