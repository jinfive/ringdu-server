package com.ringdu.server.admin.service;

import com.ringdu.server.academy.service.AcademyService;
import com.ringdu.server.admin.dto.AcademyAccountResponse;
import com.ringdu.server.admin.dto.CreateAcademyAccountRequest;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminAcademyAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AcademyService academyService;

    @Transactional
    public AcademyAccountResponse createAcademyAccount(CreateAcademyAccountRequest request) {
        validateDuplicatedEmail(request.email());
        validateDuplicatedPhone(request.phone());

        User academyUser = User.createLocalUser(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phone(),
                Role.ACADEMY
        );

        User savedUser = userRepository.save(academyUser);
        academyService.createForDirectAccount(savedUser, request.name(), request.phone());

        return AcademyAccountResponse.from(savedUser);
    }

    private void validateDuplicatedEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }
    }

    private void validateDuplicatedPhone(String phone) {
        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.DUPLICATED_PHONE);
        }
    }
}
