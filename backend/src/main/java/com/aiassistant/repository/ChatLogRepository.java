package com.aiassistant.repository;

import com.aiassistant.entity.ChatLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    long countByOrganizationId(Long organizationId);
    long countByOrganizationIdAndStatus(Long organizationId, String status);
    List<ChatLog> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId, Pageable pageable);
}
