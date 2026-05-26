package com.ringdu.server.academy.repository;

import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.entity.AcademySignupApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademySignupApplicationRepository extends JpaRepository<AcademySignupApplication, Long> {

    List<AcademySignupApplication> findAllByStatusOrderByCreatedAtAsc(AcademySignupApplicationStatus status);
}
