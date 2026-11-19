package com.aiassistant.repository;

import com.aiassistant.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findBySlug(String slug);
    Optional<Organization> findByAssistantToken(String assistantToken);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}
