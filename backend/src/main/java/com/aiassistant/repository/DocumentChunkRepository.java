package com.aiassistant.repository;

import com.aiassistant.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentId(Long documentId);

    long countByOrganizationId(Long organizationId);

    @Modifying
    @Query("DELETE FROM DocumentChunk c WHERE c.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    // Native query for MySQL FULLTEXT search
    @Query(value = "SELECT * FROM document_chunks WHERE organization_id = :orgId AND MATCH(content) AGAINST(:query IN NATURAL LANGUAGE MODE) LIMIT :limit", nativeQuery = true)
    List<DocumentChunk> searchFullTextNative(@Param("orgId") Long orgId, @Param("query") String query, @Param("limit") int limit);

    // Fallback search by keywords for H2 / unit tests
    @Query("SELECT c FROM DocumentChunk c WHERE c.organization.id = :orgId AND LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DocumentChunk> searchByKeyword(@Param("orgId") Long orgId, @Param("keyword") String keyword);

    // Fetch all chunks for an organization (used for in-memory token-based scoring and fallback)
    List<DocumentChunk> findByOrganizationId(Long organizationId);
}
