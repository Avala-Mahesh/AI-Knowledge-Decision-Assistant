package com.aiassistant.repository;

import com.aiassistant.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOrganizationIdOrderByUploadedAtDesc(Long organizationId);
    Optional<Document> findByIdAndOrganizationId(Long id, Long organizationId);
    long countByOrganizationId(Long organizationId);
    void deleteByIdAndOrganizationId(Long id, Long organizationId);
}
