package com.ringdu.server.auth.service;

import com.ringdu.server.auth.dto.SignupRequest;
import com.ringdu.server.auth.dto.SignupResponse;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<Role> LOCAL_SIGNUP_ROLES = EnumSet.of(
            Role.TEACHER,
            Role.PARENT,
            Role.STUDENT
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicatedEmail(request.email());
        validateSignupRole(request.role());

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createLocalUser(
                request.email(),
                encodedPassword,
                request.name(),
                request.phone(),
                request.role()
        );

        User savedUser = userRepository.save(user);
        return SignupResponse.from(savedUser);
    }

    private void validateDuplicatedEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private void validateSignupRole(Role role) {
        if (!LOCAL_SIGNUP_ROLES.contains(role)) {
            throw new BusinessException(ErrorCode.SIGNUP_ROLE_NOT_ALLOWED);
        }
    }
}
