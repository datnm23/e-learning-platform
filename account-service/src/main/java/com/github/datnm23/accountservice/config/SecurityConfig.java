package com.github.datnm23.accountservice.config;

import com.github.datnm23.accountservice.security.YourCustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collection;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.security.jwt.authorities-claim-name:authorities}")
    private String authoritiesClaimName;

    @Value("${app.security.jwt.userid-claim-name:user_id}")
    private String userIdClaimName;

    @Value("${app.security.jwt.email-claim-name:email}")
    private String emailClaimName;

    @Value("${app.security.jwt.active-claim-name:active}")
    private String activeClaimName;

    @Value("${app.security.jwt.emailverified-claim-name:email_verified}")
    private String emailVerifiedClaimName;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(this.corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users/verify-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users/resend-verification-email").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(customUserDetailsJwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

    // Converter to transform JWT into YourCustomUserDetails
    private Converter<Jwt, YourCustomUserDetails> jwtToCustomUserDetailsConverter() {
        return jwt -> {
            // Extract authorities/roles
            JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
            authoritiesConverter.setAuthoritiesClaimName(authoritiesClaimName);
            authoritiesConverter.setAuthorityPrefix("");
            Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

            // Extract user info
            String userIdString = jwt.getClaimAsString(userIdClaimName);
            // Fallback to subject if user_id claim not present
            if (userIdString == null) {
                userIdString = jwt.getClaimAsString(JwtClaimNames.SUB);
            }

            UUID userId = null;
            if (userIdString != null) {
                try {
                    userId = UUID.fromString(userIdString);
                } catch (IllegalArgumentException e) {
                    throw new OAuth2AuthenticationException("Invalid user_id format in token");
                }
            } else {
                throw new OAuth2AuthenticationException("Missing user_id claim in token");
            }

            String email = jwt.getClaimAsString(emailClaimName);
            if (email == null) {
                email = jwt.getSubject(); // Fallback to subject for email
                if (email == null) {
                    throw new OAuth2AuthenticationException("Missing email claim in token");
                }
            }

            // Get optional claims with defaults
            Boolean active = jwt.getClaimAsBoolean(activeClaimName);
            Boolean emailVerified = jwt.getClaimAsBoolean(emailVerifiedClaimName);

            return new YourCustomUserDetails(
                userId,
                email,
                "[PROTECTED_JWT_PASSWORD]",
                active != null ? active : true,
                emailVerified != null ? emailVerified : true,
                authorities
            );
        };
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> customUserDetailsJwtAuthenticationConverter() {
        Converter<Jwt, YourCustomUserDetails> userDetailsConverter = jwtToCustomUserDetailsConverter();

        return jwt -> {
            YourCustomUserDetails customUserDetails = userDetailsConverter.convert(jwt);
            if (customUserDetails == null) {
                throw new OAuth2AuthenticationException("Failed to extract user details from JWT");
            }
            return new CustomUserDetailsAuthenticationToken(
                jwt, 
                customUserDetails, 
                customUserDetails.getAuthorities()
            );
        };
    }

    // Custom authentication token that returns YourCustomUserDetails as principal
    public static class CustomUserDetailsAuthenticationToken extends JwtAuthenticationToken {
        private final YourCustomUserDetails userDetailsPrincipal;

        public CustomUserDetailsAuthenticationToken(Jwt jwt, YourCustomUserDetails principal, 
                                                   Collection<? extends GrantedAuthority> authorities) {
            super(jwt, authorities, principal.getUsername());
            this.userDetailsPrincipal = principal;
        }

        @Override
        public Object getPrincipal() {
            return this.userDetailsPrincipal;
        }

        @Override
        public String getName() {
            return this.userDetailsPrincipal.getUsername();
        }
    }
}
