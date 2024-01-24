package com.blog.blogbackend.services;


import com.blog.blogbackend.models.User;
import com.blog.blogbackend.utils.TimeProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    final Environment environment;
    final TimeProvider timeProvider;

    public JwtServiceImpl(Environment environment, TimeProvider timeProvider) {
        this.environment = environment;
        this.timeProvider = timeProvider;
    }

    @Override
    public String extractUsername(String jwt) {
        return extractClaim(jwt, claims -> claims.get("username", String.class));
    }

    @Override
    public Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    @Override
    public Long extractId(String jwt) {
        return extractClaim(jwt, claims -> claims.get("userId", Long.class));
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwt);
        return claimsResolver.apply(claims);
    }

    @Override
    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        try {
            final String username = extractUsername(jwt);
            return (username.equals(userDetails.getUsername())) && isTokenUnexpired(jwt);
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    private boolean isTokenUnexpired(String jwt) {
        return extractExpiration(jwt).after(timeProvider.now());
    }

    private Claims extractAllClaims(String jwt) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(environment.getProperty("jwt.secret-key"));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(User user) {

        final int TWENTY_FOUR_HOURS_IN_MILLISECONDS = 1000 * 60 * 60 * 24;

        return Jwts
                .builder()
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + TWENTY_FOUR_HOURS_IN_MILLISECONDS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
