package com.example.gymprojectionic.service;


import com.example.gymprojectionic.model.User;
import com.example.gymprojectionic.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public User addUser(User user) {
        // crypt password
        String encodedPassword = passwordEncoder.encode(user.getPwd());
        user.setPwd(encodedPassword);
        return userRepo.save(user);
    }

    public User findUserById(Long id) {
        // TODO Auto-generated method stub
        Optional<User> user = userRepo.findById(id);
        return user.isPresent() ? user.get() : null;
    }

    public void deleteUser(Long id) {
        // TODO Auto-generated method stub
        userRepo.deleteById(id);

    }

    public User getUserByEmail(String email) {
        // TODO Auto-generated method stub
        return userRepo.findUserByEmail(email);
    }



}

