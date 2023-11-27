package com.supportportal.security.manager;

import com.supportportal.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportPortalAuthenticationProvider implements AuthenticationProvider {
    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("Authenticating user with SupportPortalAuthenticationProvider.");
        UserDetails userDetails = userService.loadUserByUsername(String.valueOf(authentication.getPrincipal()));
        if (passwordEncoder.matches(String.valueOf(authentication.getCredentials()), userDetails.getPassword())) {
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }

        throw new BadCredentialsException("Username / password incorrect.");
    }

    @Override
    public boolean supports(Class<?> authType) {
        return authType.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
