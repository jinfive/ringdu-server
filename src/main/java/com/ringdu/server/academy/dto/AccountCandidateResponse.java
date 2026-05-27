package com.ringdu.server.academy.dto;

import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import java.util.List;

public record AccountCandidateResponse(
        List<CandidateDto> candidates
) {
    public record CandidateDto(
            Long userId,
            String name,
            String email,
            String phone,
            Role role
    ) {
        public static CandidateDto from(User user) {
            return new CandidateDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole()
            );
        }
    }
}
