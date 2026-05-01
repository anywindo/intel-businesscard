package org.example.businesscard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Business Card System.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        /*
                         * SHALLOW MODEL (Vulnerable)
                         * INSECURE-3 - Overly permissive access control allowing anonymous access to all API endpoints.
                         */
                        /*
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                        */

                        /*
                         * DEEP MODEL (Fixed INSECURE-3)
                         * - /api/token, /api/employees   → permitAll() (insecure demo, if uncommented in controller)
                         * - /api/auth/**                 → permitAll() (secure login endpoint)
                         * - /api/secure/**               → permitAll() (auth handled by domain primitives)
                         * - /h2-console/**               → permitAll() (dev tool)
                         * - everything else              → denyAll()
                         */
                        // Insecure demo endpoints
                        .requestMatchers("/api/token").permitAll()
                        .requestMatchers("/api/employees").permitAll()
                        // Secure login endpoint
                        .requestMatchers("/api/auth/**").permitAll()
                        // Secure endpoints — auth enforced by domain primitives in controller
                        .requestMatchers("/api/secure/**").permitAll()
                        // H2 console for development
                        .requestMatchers("/h2-console/**").permitAll()
                        // Deny everything else
                        .anyRequest().denyAll()
                )
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

