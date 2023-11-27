package com.supportportal.listener;

import com.supportportal.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener {
    private final LoginAttemptService loginAttemptService;

    @EventListener(AuthenticationSuccessEvent.class)
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        log.info("AuthenticationSuccessEvent Triggered");
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails user) {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }
}
