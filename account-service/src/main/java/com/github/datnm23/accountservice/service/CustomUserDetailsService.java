package com.github.datnm23.accountservice.service;

import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.repository.UserRepository;
import com.github.datnm23.accountservice.security.YourCustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("customUserDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );
        return YourCustomUserDetails.create(user);
    }
}
