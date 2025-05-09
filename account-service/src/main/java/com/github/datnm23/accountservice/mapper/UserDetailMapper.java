package com.github.datnm23.accountservice.mapper;

import com.github.datnm23.accountservice.dto.UserDetailDTO;
import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.entity.UserProfile;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class, UserProfileMapper.class})
public interface UserDetailMapper {

    // Map cả User and UserProfile sang UserDetailDto
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "emailVerified", source = "user.emailVerified")
    @Mapping(target = "createdAt", source = "user.createdAt")

    @Mapping(target = "avatarUrl", source = "userProfile.avatarUrl")
    @Mapping(target = "bio", source = "userProfile.bio")
    @Mapping(target = "dateOfBirth", source = "userProfile.dateOfBirth")
    @Mapping(target = "gender", source = "userProfile.gender")
    @Mapping(target = "phone", source = "userProfile.phone")
    @Mapping(target = "address", source = "userProfile.address")
    @Mapping(target = "emailNotifications", source = "userProfile.emailNotifications")
    @Mapping(target = "pushNotifications", source = "userProfile.pushNotifications")
    UserDetailDTO toDetailDto(User user, UserProfile userProfile);

    // Map list users và profiles sang detail DTOs
    default List<UserDetailDTO> toDetailDtoList(List<User> users, List<UserProfile> profiles) {
        // This would need a custom implementation to match users with their profiles
        throw new UnsupportedOperationException("This method requires a custom implementation");
    }
}