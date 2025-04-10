package com.github.datnm23.accountservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.datnm23.accountservice.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    //Find user by email (ignoring case)
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    //Find user by name both first name and last name (ignoring case)
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) = LOWER(:firstName) AND LOWER(u.lastName) = LOWER(:lastName)")
    Optional<User> findByName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    //Find user by ID and deletedAt is null
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<User> findByIdAndDeletedAtIsNull(@Param("userId") UUID userId);

    //Check if email exists (ignoring case)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);

    //Search for users by name or email (case-insensitive)
    @Query("SELECT u FROM User u WHERE " +
                  "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                  "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                  "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchByNameOrEmail(@Param("query") String query, Pageable pageable);

    // Find a user by ID including soft-deleted users
    @Query(value = "SELECT * FROM users WHERE user_id = :userId",
            nativeQuery = true)
    Optional<User> findByUserIdIncludingDeleted(@Param("userId") UUID userId);

    //Find users by activation status
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    // Find users by emai     l verification status
    Page<User> findByIsEmailVerified(Boolean isEmailVerified, Pageable pageable);

    //Find users created between two dates
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Page<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    //Update a user's activation status
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.userId = :userId")
    void updateUserActivationStatus(@Param("userId") UUID userId, @Param("isActive") boolean isActive);

    //Update a user's email verification status by email
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = :isEmailVerified WHERE u.email = :email")
    void updateUserEmailVerificationStatus(@Param("email") String email, @Param("isEmailVerified") boolean isEmailVerified);

    //Find recently active users
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :recentDate")
    Page<User> findRecentlyActiveUsers(@Param("recentDate") LocalDateTime recentDate, Pageable pageable);

    //Find recently registered users
    @Query("SELECT u FROM User u WHERE u.createdAt >= :recentDate")
    Page<User> findRecentlyRegisteredUsers(@Param("recentDate") LocalDateTime recentDate, Pageable pageable);

    //Find recently active users between 2 time
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :startDate AND :endDate")
    Page<User> findRecentlyActiveUsersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    //Hard delete users that have been soft deleted for more than 30 days
    @Modifying
    @Query("DELETE FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt <= :thresholdDate")
    void hardDeleteSoftDeletedUsers(@Param("thresholdDate") LocalDateTime thresholdDate);


}