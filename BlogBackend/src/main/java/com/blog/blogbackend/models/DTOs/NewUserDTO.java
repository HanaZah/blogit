package com.blog.blogbackend.models.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NewUserDTO {

    @NotNull (message = "Username is required.")
    @NotBlank (message = "Username is required.")
    private String username;

    @NotNull (message = "Password is required.")
    @NotBlank (message = "Password is required.")
    @Size(min = 6, message = "Password must contain at least 6 characters.")
    private String password;

    public NewUserDTO() {
    }

    public NewUserDTO(String username, String password) {
        this.username = username;
        this.password = password;
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
