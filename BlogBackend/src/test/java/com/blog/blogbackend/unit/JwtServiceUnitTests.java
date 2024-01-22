package com.blog.blogbackend.unit;

import com.blog.blogbackend.models.User;
import com.blog.blogbackend.services.JwtService;
import com.blog.blogbackend.services.JwtServiceImpl;
import com.blog.blogbackend.utils.TimeProvider;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class JwtServiceUnitTests {

    @Mock
    private Environment environment;
    @Mock
    private TimeProvider timeProvider;
    private JwtService jwtService;
    private final String VALID_UNEXPIRED_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiam9obmRvZSIsImV4cCI6MTczNzQ5Njc4MX0.ezr18iElPN89rVKmsIsa2QIlJw3UAiOJr3XnkvdXzIQ";
    private final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsIm5hbWUiOiJKb2huIiwiZW1haWwiOiJqb2hueUB0ZXN0LmNvbSJ9.pX23_nCwePfDIpKervoALxNqft0hCtUPPZqKVl0qp-k";
    private final long TIME_BEFORE_EXPIRATION_IN_MILLIS = 1705364622525L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtServiceImpl(environment, timeProvider);
        when(environment.getProperty("jwt.secret-key")).thenReturn("Pdy1yzZW1lja+T+zI3IeHkiWaH0sqYoUlIl9VKy8sio=");
        when(timeProvider.now()).thenReturn(new Date(TIME_BEFORE_EXPIRATION_IN_MILLIS));
    }

    @Test
    public void extractUsernameReturnsCorrectUsernameForValidToken() {
        String expectedUsername = "johndoe";
        String extractedUsername = jwtService.extractUsername(VALID_UNEXPIRED_TOKEN);
        assertEquals(expectedUsername, extractedUsername);
    }

    @Test
    public void extractUsernameThrowsExceptionForInvalidToken() {
        assertThrows(SignatureException.class, () -> jwtService.extractUsername(INVALID_TOKEN));
    }

    @Test
    public void extractUsernameReturnsNullForMissingClaim() {
        String tokenWithoutUsernameClaim = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImV4cCI6MTczNzQ5Njk1Nn0.yjMdfyWzygevdYtFlPRPukmlCpV1IilNu1laZgEQkrw";
        String extractedEmail = jwtService.extractUsername(tokenWithoutUsernameClaim);
        assertNull(extractedEmail);
    }

    @Test
    public void extractExpirationReturnsValidDate() {
        Date extractedExpiration = jwtService.extractExpiration(VALID_UNEXPIRED_TOKEN);
        assertNotNull(extractedExpiration);
        assertTrue(extractedExpiration.after(new Date(TIME_BEFORE_EXPIRATION_IN_MILLIS)));
    }

    @Test
    public void extractIdReturnsCorrectUsernameForValidToken() {
        Long expectedUserId = 1L;
        Long extractedUserId = jwtService.extractId(VALID_UNEXPIRED_TOKEN);
        assertEquals(expectedUserId, extractedUserId);
    }

    @Test
    public void extractIdThrowsExceptionForInvalidToken() {
        assertThrows(SignatureException.class, () -> jwtService.extractId(INVALID_TOKEN));
    }

    @Test
    public void extractIdReturnsNullForMissingClaim() {
        String tokenWithoutUserIdClaim = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImpvaG5kb2UiLCJleHAiOjE3Mzc0OTY4ODl9.ebG8GUDGvLBe8KhJw0VHxez829n2Egvic3lljfT7Bfo";
        Long extractedUserId = jwtService.extractId(tokenWithoutUserIdClaim);
        assertNull(extractedUserId);
    }

    @Test
    public void generateTokenReturnsTokenWithCorrectClaims() {
        User user = new User();
        user.setId(1L);
        user.setUsername("johndoe");

        String token = jwtService.generateToken(user);
        Long userId = jwtService.extractId(token);
        String username = jwtService.extractUsername(token);

        assertNotNull(token);
        assertEquals(1L, userId ); // Implement extractUserId method
        assertEquals("johndoe", username); // Implement extractName method
    }

    @Test
    public void isTokenValidReturnsTrueForValidToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("johndoe");

        assertTrue(jwtService.isTokenValid(VALID_UNEXPIRED_TOKEN, user));
    }

    @Test
    public void isTokenValidReturnsFalseForExpiredToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("johndoe");

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsIm5hbWUiOiJKb2huIiwiZW1haWwiOiJqb2huQHRlc3QuY29tIiwiZXhwIjoxNjk1NzY3MzcyfQ.ictYg8v0sBHICK-ys_9TGW3mGBs5iGTWiKrC_o5q67U";

        assertFalse(jwtService.isTokenValid(expiredToken, user));
    }

}
