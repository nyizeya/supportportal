package com.supportportal.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = 432_000_000; // equivalent to 5 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String GET_ARRAYS_LLC = "SIP, LLC";
    public static final String GET_ARRAY_ADMINISTRATION = "User Management Portal";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to login to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String NO_USER_FOUND_BY_EMAIL = "No user found by email.";
    public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "/user/reset-password", "/user/image/**"};
    
}
