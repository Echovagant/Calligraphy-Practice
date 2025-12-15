package com.calligraphy.platform.repository;

import com.calligraphy.platform.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {

    List<FileMetadata> findByUserIdOrderByCreatedAtDesc(String userId);

    List<FileMetadata> findByFilePurposeOrderByCreatedAtDesc(String filePurpose);

    List<FileMetadata> findByCreatedAtBefore(LocalDateTime createdAt);

    long countByUserId(String userId);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.userId = :userId")
    long sumFileSizeByUserId(@Param("userId") String userId);

    Optional<FileMetadata> findByFileNameAndUserId(String fileName, String userId);

    List<FileMetadata> findByUserIdAndFilePurpose(String userId, String filePurpose);

    List<FileMetadata> findByRelatedEntityId(String relatedEntityId);

    List<FileMetadata> findByIsPublicTrue();

    List<FileMetadata> findByUserIdAndIsPublicTrue(String userId);
}


