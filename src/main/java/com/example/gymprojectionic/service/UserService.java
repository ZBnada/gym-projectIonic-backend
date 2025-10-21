package com.example.gymprojectionic.service;


import com.example.gymprojectionic.model.User;
import com.example.gymprojectionic.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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



    public User getUserByEmail(String email) {
        // TODO Auto-generated method stub
        return userRepo.findUserByEmail(email);
    }


    public List<User> getAllUsers() {
        return userRepo.findAll();
    }


    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new EntityNotFoundException("Utilisateur non trouvé avec id " + id);
        }
        userRepo.deleteById(id);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec id " + id));

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPwd() != null && !updatedUser.getPwd().isEmpty()) {
            existingUser.setPwd(passwordEncoder.encode(updatedUser.getPwd()));
        }

        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setPhoto(updatedUser.getPhoto());

        return userRepo.save(existingUser);
    }
}

