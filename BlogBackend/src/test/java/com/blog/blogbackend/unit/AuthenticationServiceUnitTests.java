package com.blog.blogbackend.unit;

import com.blog.blogbackend.models.DTOs.AuthResponseDTO;
import com.blog.blogbackend.models.DTOs.LoginUserDTO;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.UserRepository;
import com.blog.blogbackend.services.AuthService;
import com.blog.blogbackend.services.AuthServiceImpl;
import com.blog.blogbackend.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthenticationServiceUnitTests {

    private AuthService authenticationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthServiceImpl(userRepository, authManager, jwtService);
    }

    @Test
    public void loginReturnsAuthenticationResponseDTOForValidCredentials() {
        LoginUserDTO request = new LoginUserDTO();
        request.setUsername("johndoe");
        request.setPassword("password");

        User user = new User();
        user.setUsername("johndoe");
        user.setPassword(passwordEncoder.encode("password"));

        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiam9obmRvZSIsImV4cCI6MTcwNTQ1MDM0OX0.TmmNUS8VQxKI4K9wc3QTWMP215Zl9zLkgnrltguL0ms";
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "johndoe", "password"
        );

        when(userRepository.findUserByUsernameAndDeleted("johndoe", false))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class)))
                .thenReturn(jwt);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authToken);

        AuthResponseDTO response = authenticationService.loginUser(request);

        assertNotNull(response.getToken());
        assertEquals(jwt, response.getToken());

    }

    @Test
    public void loginThrowsCorrectExceptionForWrongCredentials() {
        LoginUserDTO request = new LoginUserDTO();
        request.setUsername("johndoe");
        request.setPassword("passwordNot");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(""));

        assertThrows(BadCredentialsException.class, () -> authenticationService.loginUser(request));
    }
}
