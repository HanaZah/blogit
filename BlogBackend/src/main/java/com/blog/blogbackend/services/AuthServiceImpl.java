package com.blog.blogbackend.services;

import com.blog.blogbackend.models.DTOs.AuthResponseDTO;
import com.blog.blogbackend.models.DTOs.LoginUserDTO;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;

@Service
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDTO loginUser(LoginUserDTO loginData) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginData.getUsername(), loginData.getPassword())
        );

        User user = userRepository.findUserByUsernameAndDeleted(loginData.getUsername(), false)
                .orElseThrow(() -> new InputMismatchException("Username not found"));

        String token = jwtService.generateToken(user);

        System.out.println("username: " + user.getUsername() + "\ntoken: " + token);

        return new AuthResponseDTO(user.getUsername(), token);
    }
}
