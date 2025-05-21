package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Account;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    public void register(String firstName, String middleName, String lastName, String email, String password, User.Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .portfolio("{}")
                .kycStatus(User.KYCStatus.НА_ПРОВЕРКЕ)
                .registrationDate(LocalDateTime.now())
                .accounts(new ArrayList<>())
                .build();

        Map<String, Double> balancesMap = new HashMap<>();
        balancesMap.put("USD", 0.0);
        balancesMap.put("RUB", 0.0);
        balancesMap.put("EURO", 0.0);
        balancesMap.put("BYN", 0.0);

        String balancesJson;
        try {
            balancesJson = objectMapper.writeValueAsString(balancesMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize balances: " + e.getMessage());
        }

        Account defaultAccount = Account.builder()
                .user(user)
                .accountNumber(UUID.randomUUID().toString())
                .name("Default Account")
                .balances(balancesJson)
                .createdAt(LocalDateTime.now())
                .accountType(Account.AccountType.ВАЛЮТНЫЙ)
                .build();

        user.getAccounts().add(defaultAccount);
        userRepository.save(user);
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }
}