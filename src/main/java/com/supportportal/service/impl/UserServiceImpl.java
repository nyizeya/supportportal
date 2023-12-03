package com.supportportal.service.impl;

import com.supportportal.domain.User;
import com.supportportal.domain.UserPrincipal;
import com.supportportal.enumeration.Role;
import com.supportportal.exception.domain.EmailExistsException;
import com.supportportal.exception.domain.UserNotFoundException;
import com.supportportal.exception.domain.UsernameExistsException;
import com.supportportal.repository.UserRepository;
import com.supportportal.service.EmailService;
import com.supportportal.service.LoginAttemptService;
import com.supportportal.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    @Override
    public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        User user = new User();
        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setJoinDate(new Date());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_USER.name());
        user.setAuthorities(Role.ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl());
        userRepository.save(user);
        log.info("created user {}", user);
        log.info("New User Password {}", password);
        emailService.sendNewPasswordEmail(firstName, password, email);
        return user;
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String getTemporaryProfileImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/profile/temp").toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username " + username));
    }

    @Override
    public User findUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email " + email));
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(username).orElseThrow(() -> {
            log.error("User not found by username: {}", username);
            return new UsernameNotFoundException("User not found by username: " + username);
        });

        validateLoginAttempts(user);
        user.setLastLoginDateDisplay(user.getLastLoginDate());
        user.setLastLoginDate(new Date());
        userRepository.save(user);
        log.info("Returning found user by username: {}.", username);
        return new UserPrincipal(user);
    }

    private void validateLoginAttempts(User user) {
        if (user.isNotLocked()) {
            if (loginAttemptService.hasExceededMaxAttempts(user.getEmail())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getEmail());
        }
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        if (StringUtils.isNotEmpty(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            User userByUsername = findUserByUsername(newUsername);

            if (!userByUsername.getId().equals(currentUser.getId())) {
                throw new UsernameExistsException("Username already exists!");
            }

            User userByEmail = findUserByEmail(email);

            if (!userByEmail.getId().equals(currentUser.getId())) {
                throw new EmailExistsException("Email already exists!");
            }

            return currentUser;
        } else {
            try {
                User userByUsername = findUserByUsername(newUsername);
                if (null != userByUsername) {
                    throw new UsernameExistsException("Username already exists!");
                }

                User userByEmail = findUserByEmail(email);
                if (null != userByEmail) {
                    throw new EmailExistsException("Email already exists!");
                }
            } catch (UserNotFoundException exception) {
            }
            return null;
        }
    }
}
