package com.blog.blogbackend.services;


import com.blog.blogbackend.models.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {

    String extractUsername(String jwt);

    Date extractExpiration(String jwt);

    boolean isTokenValid(String jwt, UserDetails userDetails);

    String generateToken(User user);
}
