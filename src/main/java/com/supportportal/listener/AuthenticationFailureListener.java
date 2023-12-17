package com.supportportal.listener;

import com.supportportal.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener {
    private final LoginAttemptService loginAttemptService;

    @EventListener(AuthenticationFailureBadCredentialsEvent.class)
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) throws ExecutionException {
        log.info("AuthenticationFailureBadCredentialsEvent Triggered");
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof String) {
            String email = (String) principal;
            loginAttemptService.addUserToLoginAttemptCache(email);
        }
    }
}
