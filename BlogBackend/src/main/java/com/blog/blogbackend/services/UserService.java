package com.blog.blogbackend.services;

import com.blog.blogbackend.models.DTOs.NewUserDTO;
import com.blog.blogbackend.models.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User create(NewUserDTO userData) throws Exception;

    void softDelete(User user);
}
