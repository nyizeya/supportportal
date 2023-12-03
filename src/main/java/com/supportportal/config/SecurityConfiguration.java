package com.supportportal.config;

import com.supportportal.security.filter.JwtAccessDeniedHandler;
import com.supportportal.security.filter.JwtAuthenticationEntryPoint;
import com.supportportal.security.filter.JwtAuthorizationFilter;
import com.supportportal.security.manager.SupportPortalAuthenticationProvider;
import com.supportportal.security.manager.SupportportalAuthenticationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final SupportportalAuthenticationManager authenticationManager;
    private final SupportPortalAuthenticationProvider authenticationProvider;

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(requestCustomizer -> {
                    requestCustomizer.anyRequest().permitAll();
//                    requestCustomizer.requestMatchers(SecurityConstant.PUBLIC_URLS).permitAll();
//                    requestCustomizer.anyRequest().authenticated();
                })
                .exceptionHandling(customizer -> {
                    customizer.accessDeniedHandler(jwtAccessDeniedHandler);
                    customizer.authenticationEntryPoint(jwtAuthenticationEntryPoint);
                })
                .authenticationManager(authenticationManager)
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
