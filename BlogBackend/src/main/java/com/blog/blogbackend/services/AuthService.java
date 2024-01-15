package com.blog.blogbackend.services;

import com.blog.blogbackend.models.DTOs.AuthResponseDTO;
import com.blog.blogbackend.models.DTOs.LoginUserDTO;

public interface AuthService {

    AuthResponseDTO loginUser(LoginUserDTO loginData);
}
