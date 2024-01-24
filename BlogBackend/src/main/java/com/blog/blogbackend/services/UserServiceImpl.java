package com.blog.blogbackend.services;

import com.blog.blogbackend.models.DTOs.NewUserDTO;
import com.blog.blogbackend.models.Role;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(NewUserDTO userData) throws Exception {
        String username = userData.getUsername();

        try {
            String password = passwordEncoder.encode(userData.getPassword());
            User newUser = new User(username, password);
            newUser.setRole(Role.ROLE_USER);

            return userRepository.save(newUser);
        }catch (Exception e) {
            throw new EntityExistsException("Username " + username + " is already taken.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsernameAndDeleted(username, false)
                .orElseThrow(() -> new UsernameNotFoundException("Username is incorrect"));
    }

    @Override
    public void softDelete(User user) {
        user.setDeleted(true);
        userRepository.save(user);
    }
}
