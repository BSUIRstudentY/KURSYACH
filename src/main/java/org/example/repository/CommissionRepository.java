package org.example.repository;

import org.example.entity.Commission;
import org.example.entity.CommissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommissionRepository extends JpaRepository<Commission, Integer> {
    Optional<Commission> findByCommissionType(CommissionType commissionType); // Исправлено с findAllByCommisionType на findByCommissionType
    List<Commission> findByIsActive(boolean isActive);
}