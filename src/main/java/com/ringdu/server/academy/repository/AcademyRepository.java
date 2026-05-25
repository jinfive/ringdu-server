package com.ringdu.server.academy.repository;

import com.ringdu.server.academy.entity.Academy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyRepository extends JpaRepository<Academy, Long> {

    boolean existsByUserId(Long userId);

    Optional<Academy> findByUserId(Long userId);
}
