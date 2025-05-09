package com.github.datnm23.accountservice.security;

import com.github.datnm23.accountservice.exception.ActionNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityUtils {
    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    public static Optional<UUID> getCurrentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserIdFromAuthentication(authentication);
    }

    public static UUID getRequiredCurrentUserId() {
        return getCurrentUserIdOptional().orElseThrow(() ->
                new ActionNotAllowedException("User not authenticated or user ID could not be determined")
        );
    }

    public static Optional<UUID> getUserIdFromPrincipalObject(Object principal) {
        if (principal == null) return Optional.empty();

        if (principal instanceof YourCustomUserDetails) {
            return Optional.ofNullable(((YourCustomUserDetails) principal).getUserId());
        } else if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            // Try user_id claim first, then fallback to sub
            String userIdString = jwt.getClaimAsString("user_id");
            if (userIdString == null) {
                userIdString = jwt.getClaimAsString("sub");
            }
            
            if (userIdString != null) {
                try {
                    return Optional.of(UUID.fromString(userIdString));
                } catch (IllegalArgumentException e) {
                    // Invalid UUID format
                    return Optional.empty();
                }
            }
        } else if (principal instanceof String) {
            try {
                return Optional.of(UUID.fromString((String) principal));
            } catch (IllegalArgumentException e) {
                // Not a UUID string
                return Optional.empty();
            }
        }
        
        return Optional.empty();
    }

    public static Optional<UUID> getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return getUserIdFromPrincipalObject(authentication.getPrincipal());
    }

    public static UUID getRequiredUserIdFromPrincipal(Object principal) {
        return getUserIdFromPrincipalObject(principal)
               .orElseThrow(() -> new ActionNotAllowedException("User ID could not be determined from principal."));
    }
    
    public static boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return hasAdminRole(authentication);
    }
    
    public static boolean hasAdminRole(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
    
    public static boolean isOwner(UUID targetUserId, Object principalObject) {
        if (targetUserId == null || principalObject == null) {
            return false;
        }
        Optional<UUID> authenticatedUserIdOpt = getUserIdFromPrincipalObject(principalObject);
        return authenticatedUserIdOpt.map(id -> id.equals(targetUserId)).orElse(false);
    }
}

