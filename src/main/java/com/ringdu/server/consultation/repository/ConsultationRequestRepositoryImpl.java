package com.ringdu.server.consultation.repository;

import com.ringdu.server.consultation.entity.ConsultationRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.entity.ConsultationRequestType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsultationRequestRepositoryImpl implements ConsultationRequestRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ConsultationRequest> findAcademyRequests(
            Long academyId,
            ConsultationRequestStatus status,
            LocalDate from,
            LocalDate to,
            ConsultationRequestType type,
            Long studentProfileId
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ConsultationRequest> query = criteriaBuilder.createQuery(ConsultationRequest.class);
        Root<ConsultationRequest> root = query.from(ConsultationRequest.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(root.get("academyId"), academyId));
        if (status != null) {
            predicates.add(criteriaBuilder.equal(root.get("status"), status));
        }
        if (from != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("requestedDate"), from));
        }
        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("requestedDate"), to));
        }
        if (type != null) {
            predicates.add(criteriaBuilder.equal(root.get("consultationType"), type));
        }
        if (studentProfileId != null) {
            predicates.add(criteriaBuilder.equal(root.get("studentProfileId"), studentProfileId));
        }

        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(
                criteriaBuilder.desc(root.get("createdAt")),
                criteriaBuilder.desc(root.get("id"))
        );
        return entityManager.createQuery(query).getResultList();
    }
}
