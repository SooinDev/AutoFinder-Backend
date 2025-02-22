package com.example.autofinder.service;

import com.example.autofinder.model.Role;
import com.example.autofinder.model.User;
import com.example.autofinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username, String password) { // role 제거
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole(Role.USER); // role 값 강제 설정

        return userRepository.save(user);
    }

    public User authenticateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user.get();
        }
        return null;
    }
}