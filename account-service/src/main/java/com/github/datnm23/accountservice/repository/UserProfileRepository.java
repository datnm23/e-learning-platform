package com.github.datnm23.accountservice.repository;

import com.github.datnm23.accountservice.statics.Gender;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.github.datnm23.accountservice.entity.UserProfile;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /* -----Tìm kiếm -----*/

    Optional<UserProfile> findByUserId(UUID userId);

    Optional<UserProfile> findByPhone(String phone);

    Page<UserProfile> findByGender(Gender gender, Pageable pageable);

    boolean existsByUserId(UUID userId);

    boolean existsByPhone(String phone);

    @Query(value = "SELECT * FROM user_profiles WHERE user_id = :userId",
            nativeQuery = true)
    Optional<UserProfile> findIncludingDeleted(@Param("userId") UUID userId);

    /* ----- Search nâng cao -----*/

    @Query("""
            SELECT p FROM UserProfile p
            WHERE LOWER(p.bio)      LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.city)     LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.country)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.language) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<UserProfile> search(@Param("q") String keyword, Pageable pageable);

    /* ----- Soft-delete & restore – update trực tiếp-----*/

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE UserProfile p SET p.deletedAt = :now WHERE p.userId = :userId")
    int softDelete(@Param("userId") UUID userId,
                   @Param("now") OffsetDateTime now);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE UserProfile p SET p.deletedAt = NULL WHERE p.userId = :userId")
    int restore(@Param("userId") UUID userId);

    /* ----- SHouse-keeping: xoá hẳn bản ghi đã soft-delete quá 30 ngày----*/

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            DELETE FROM UserProfile p
            WHERE p.deletedAt IS NOT NULL
              AND p.deletedAt <= :threshold
            """)
    int purgeSoftDeleted(@Param("threshold") OffsetDateTime threshold);

}
