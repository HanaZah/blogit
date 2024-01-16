package com.blog.blogbackend.services;

import com.blog.blogbackend.models.DTOs.NewUserDTO;
import com.blog.blogbackend.models.Role;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.UserRepository;
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
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsernameAndDeleted(username, false).orElse(null);
    }

    @Override
    public User createNewUser(NewUserDTO userData) throws Exception {

        String username = userData.getUsername();

        if(findUserByUsername(username) != null) {
            throw new Exception("Username is already taken.");
        }

        String password = passwordEncoder.encode(userData.getPassword());
        User newUser = new User(username, password);
        newUser.setRole(Role.ROLE_USER);

        return userRepository.save(newUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsernameAndDeleted(username, false)
                .orElseThrow(() -> new UsernameNotFoundException("Username is incorrect"));
    }
}
