package com.supportportal.resource;

import com.supportportal.constant.FileConstant;
import com.supportportal.domain.User;
import com.supportportal.domain.UserPrincipal;
import com.supportportal.exception.domain.EmailExistsException;
import com.supportportal.exception.domain.EmailNotFoundException;
import com.supportportal.exception.domain.UserNotFoundException;
import com.supportportal.exception.domain.UsernameExistsException;
import com.supportportal.security.utility.TokenProvider;
import com.supportportal.service.UserService;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.supportportal.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static com.supportportal.constant.SecurityConstant.TOKEN_PREFIX;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @PostMapping("login")
    public ResponseEntity<User> login(@RequestBody User user) throws UserNotFoundException {
        User loggedInUser = userService.findUserByEmail(user.getEmail());
        authenticate(user.getEmail(), user.getPassword());
        UserPrincipal userPrincipal = new UserPrincipal(loggedInUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loggedInUser, jwtHeader, HttpStatus.OK);
    }
    
    @PostMapping("register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
        User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("add")
    public ResponseEntity<User> addNewUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String userName,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam boolean isActive,
            @RequestParam boolean isNonLocked,
            @RequestParam MultipartFile profileImage
    ) {
        User newUser = userService.addNewUser(firstName, lastName, userName, email, role, isActive, isNonLocked, profileImage);
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("update")
    public ResponseEntity<User> updateUser(
            @RequestParam String currentUsername,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String userName,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam boolean isActive,
            @RequestParam boolean isNonLocked,
            @RequestParam MultipartFile profileImage
    ) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        User updatedUser = userService.updateUser(currentUsername, firstName, lastName, userName, email, role, isActive, isNonLocked, profileImage);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) throws UserNotFoundException {
        return ResponseEntity.ok(userService.findUserByUsername(username));
    }

    @GetMapping("list")
    public ResponseEntity<List<User>> getAllUser() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("resetPassword/{email}")
    public ResponseEntity resetPassword(@PathVariable String email) throws EmailNotFoundException, MessagingException {
        userService.resetPassword(email);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("updateProfileImage")
    public ResponseEntity<User> updateProfileImage(
            @RequestParam String username,
            @RequestParam MultipartFile profileImage
    ) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
        User user = userService.updateProfileImage(username, profileImage);
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "/image/{username}/{fileName}", produces = {MediaType.IMAGE_JPEG_VALUE})
    public byte[] getProfileImage(
            @PathVariable String username,
            @PathVariable String fileName
    ) throws IOException {
        return Files.readAllBytes(Paths.get(FileConstant.USER_FOLDER).resolve(username).resolve(fileName).toAbsolutePath());
    }

    @GetMapping(path = "/image/{profile}/{username}", produces = {MediaType.IMAGE_JPEG_VALUE})
    public byte[] getTempProfileImage(
            @PathVariable String profile,
            @PathVariable String username
    ) throws IOException {
        URL url = new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(JWT_TOKEN_HEADER, TOKEN_PREFIX + tokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }
}
