package com.github.datnm23.accountservice.repository;

import com.github.datnm23.accountservice.entity.OAuthProvider;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OAuthProviderRepository extends JpaRepository<OAuthProvider, UUID> {

    /* -----Tìm kiếm -----*/

    Optional<OAuthProvider> findByProviderAndExternalId(OAuthProvider provider, String externalId);

    List<OAuthProvider> findByUserUserId(UUID userId);

    boolean existsByProviderAndExternalId(OAuthProvider provider, String externalId);

    /* -----Search nâng cao ----*/

    @Query("""
            SELECT o FROM OAuthProvider o
            WHERE LOWER(o.email)        LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(o.displayName)  LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<OAuthProvider> search(@Param("q") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM oauth_providers WHERE user_id = :userId", nativeQuery = true)
    List<OAuthProvider> findIncludingDeleted(@Param("userId") UUID userId);

    /* ----- Soft-delete & restore – update trực tiếp-----*/

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE OAuthProvider o SET o.deletedAt = :now WHERE o.userId = :id")
    int softDelete(@Param("id") UUID userId, @Param("now") OffsetDateTime now);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE OAuthProvider o SET o.deletedAt = NULL WHERE o.userId = :id")
    int restore(@Param("id") UUID userId);

    /* ----- SHouse-keeping: xoá hẳn bản ghi đã soft-delete quá 30 ngày----*/

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            DELETE FROM OAuthProvider o
            WHERE o.deletedAt IS NOT NULL
              AND o.deletedAt <= :threshold
            """)
    int purgeSoftDeleted(@Param("threshold") OffsetDateTime threshold);
}
