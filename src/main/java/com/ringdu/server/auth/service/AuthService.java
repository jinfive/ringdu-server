package com.ringdu.server.auth.service;

import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.repository.AcademySignupApplicationRepository;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.dto.AcademySignupResponse;
import com.ringdu.server.auth.dto.LoginRequest;
import com.ringdu.server.auth.dto.LoginResponse;
import com.ringdu.server.auth.dto.LoginResult;
import com.ringdu.server.auth.dto.MeResponse;
import com.ringdu.server.auth.dto.RefreshTokenIssue;
import com.ringdu.server.auth.dto.SignupRequest;
import com.ringdu.server.auth.dto.SignupResponse;
import com.ringdu.server.auth.dto.TokenRefreshResponse;
import com.ringdu.server.auth.dto.TokenRefreshResult;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.global.security.jwt.JwtTokenProvider;
import com.ringdu.server.user.entity.AuthProvider;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AcademySignupApplicationRepository academySignupApplicationRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicatedEmail(request.email());
        validateSignupRole(request.role());
        validatePasswordConfirm(request.password(), request.passwordConfirm());

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

    @Transactional
    public AcademySignupResponse signupAcademy(AcademySignupRequest request) {
        validateDuplicatedEmail(request.email());
        validatePasswordConfirm(request.password(), request.passwordConfirm());

        User academyUser = User.createLocalUser(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.representativeName(),
                request.phone(),
                Role.ACADEMY,
                UserStatus.PENDING_APPROVAL
        );
        User savedUser = userRepository.save(academyUser);

        AcademySignupApplication application = AcademySignupApplication.create(
                savedUser,
                request.academyName(),
                request.representativeName(),
                request.phone(),
                request.postalCode(),
                request.address(),
                request.detailAddress()
        );

        return AcademySignupResponse.from(academySignupApplicationRepository.save(application));
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateLoginUser(user);
        validatePassword(request.password(), user.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(user);
        RefreshTokenIssue refreshTokenIssue = refreshTokenService.createRefreshToken(user);

        return new LoginResult(LoginResponse.of(accessToken, user), refreshTokenIssue.refreshToken());
    }

    @Transactional
    public TokenRefreshResult refresh(String refreshToken) {
        var rotation = refreshTokenService.rotateRefreshToken(refreshToken);
        Long userId = rotation.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateActiveUser(user);

        String accessToken = jwtTokenProvider.createAccessToken(user);

        return new TokenRefreshResult(TokenRefreshResponse.of(accessToken), rotation.refreshToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return MeResponse.from(user);
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

    private void validatePasswordConfirm(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
    }

    private void validateLoginUser(User user) {
        validateActiveUser(user);

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BusinessException(ErrorCode.LOCAL_LOGIN_NOT_ALLOWED);
        }
    }

    private void validateActiveUser(User user) {
        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new BusinessException(ErrorCode.ACADEMY_APPROVAL_PENDING);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || !passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }
}
