package com.github.datnm23.accountservice.mapper;

import com.github.datnm23.accountservice.dto.UserDetailDTO;
import com.github.datnm23.accountservice.dto.UserProfileDTO;
import com.github.datnm23.accountservice.dto.UserProfileUpdateDTO;
import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.entity.UserProfile;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserProfileMapper {

    // Convert entity sang DTO
    UserProfileDTO toUserProfileDTO(UserProfile userProfile);

    // Convert entity list sang DTO list
    List<UserProfileDTO> toDtoList(List<UserProfile> userProfiles);
    
    // Tạo profile mới cho user
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "emailNotifications", constant = "true")
    @Mapping(target = "pushNotifications", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    UserProfile createProfileFromUser(User user);

    // Update entity từ update DTO
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateUserProfileFromDto(UserProfileUpdateDTO dto, @MappingTarget UserProfile userProfile);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "createdAt", source = "user.createdAt")
    UserDetailDTO toUserDetailDTO(User user, UserProfile userProfile);
}