package com.ringdu.server.academy.service;

import com.ringdu.server.academy.dto.AccountCandidateResponse;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademyAccountCandidateService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AccountCandidateResponse searchCandidates(Role role, String email, String phone) {
        String normalizedEmail = StringUtils.hasText(email) ? email.trim() : null;
        String normalizedPhone = StringUtils.hasText(phone) ? phone.trim() : null;

        if (normalizedEmail == null && normalizedPhone == null) {
            return new AccountCandidateResponse(List.of());
        }

        List<User> candidates = userRepository.findDistinctCandidatesByRoleAndStatusAndEmailOrPhone(
                role,
                UserStatus.ACTIVE,
                normalizedEmail,
                normalizedPhone
        );

        return new AccountCandidateResponse(
                candidates.stream()
                        .map(AccountCandidateResponse.CandidateDto::from)
                        .toList()
        );
    }
}
