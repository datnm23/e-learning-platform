package com.github.datnm23.accountservice.repository;

import com.github.datnm23.accountservice.statics.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.github.datnm23.accountservice.entity.UserProfile;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile,UUID> {

    //Find a profile by user ID including soft deleted profiles
    @Query(value = "SELECT * FROM user_profiles WHERE user_id = :userId",
            nativeQuery = true)
    Optional<UserProfile> findByUserIdIncludingDeleted(@Param("userId") UUID userId);



    //Find a profile by gender including soft deleted profiles
    Page<UserProfile> findByGender(Gender gender, Pageable pageable);

    //Find a profile by phone
    Optional<UserProfile> findByPhone(String phone);

    // check if a profile exists by user ID
    boolean existsByUserId(UUID userId);

    // check if a profile exists by phone
    boolean existsByPhone(String phone);
}
