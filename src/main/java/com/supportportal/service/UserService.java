package com.supportportal.service;

import com.supportportal.domain.User;
import com.supportportal.exception.domain.EmailExistsException;
import com.supportportal.exception.domain.UserNotFoundException;
import com.supportportal.exception.domain.UsernameExistsException;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException;
    List<User> getUsers();

    User findUserByUsername(String username) throws UserNotFoundException;

    User findUserByEmail(String email) throws UserNotFoundException;
    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage);
}
