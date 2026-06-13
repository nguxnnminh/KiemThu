package com.smartdental.service;

import com.smartdental.entity.Employee;
import com.smartdental.entity.User;
import com.smartdental.enums.Role;
import com.smartdental.repository.UserRepository;
import com.smartdental.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Tien ich lay thong tin nguoi dung dang dang nhap.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public Optional<CustomUserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            return Optional.empty();
        }
        return Optional.of(details);
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        return getCurrentUserDetails()
                .flatMap(details -> userRepository.findByUsernameIgnoreCase(details.getUsername()));
    }

    public Optional<Role> getCurrentRole() {
        return getCurrentUserDetails().map(details -> details.getUser().getRole());
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getCurrentEmployee() {
        return getCurrentUser().map(User::getEmployee);
    }
}
