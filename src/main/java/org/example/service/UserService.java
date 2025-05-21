package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void updateUser(User user) {
        // Implementation to update user in the database (e.g., using JPA Repository)
        userRepository.save(user);
    }

    public void deleteUser(Integer userId) {
        // Implementation to delete user by ID (e.g., using JPA Repository)
        userRepository.deleteById(userId);
    }
}