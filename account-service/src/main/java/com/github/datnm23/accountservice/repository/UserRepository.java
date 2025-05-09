package com.github.datnm23.accountservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.datnm23.accountservice.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    /* -----Tìm kiếm -----*/

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByActive(boolean active, Pageable pageable);

    Page<User> findByEmailVerified(boolean verified, Pageable pageable);

    /* ----- Search nâng cao ------*/

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.firstName)    LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.lastName)     LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email)        LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<User> searchByKeyword(@Param("q") String q, Pageable pageable);

    /* -----Thời gian hoạt động------*/

    Page<User> findByLastLoginAtAfter(OffsetDateTime since, Pageable pageable);

    Page<User> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<User> findByLastLoginAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    /* -----Cập nhật trạng thái------*/

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.active = :active WHERE u.userId = :id")
    void updateActive(@Param("id") UUID id, @Param("active") boolean active);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = :verified WHERE LOWER(u.email) = LOWER(:email)")
    void updateEmailVerified(@Param("email") String email, @Param("verified") boolean verified);

    /* ----- Hard-delete -----*/

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt <= :threshold")
    int purgeSoftDeleted(@Param("threshold") OffsetDateTime threshold);

    /* -----Truy vấn bao gồm bản ghi đã xoá -----*/

    @Query(value = "SELECT * FROM users WHERE user_id = :id", nativeQuery = true)
    Optional<User> findIncludingDeleted(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " + 
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

}