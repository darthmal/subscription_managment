package com.registration.platform.config;

import java.util.Arrays; // Import the filter
import java.util.List; // Import Lombok annotation

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; // Import standard filter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.registration.platform.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Add Lombok annotation for constructor injection
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Inject the custom JWT filter

    // Define public routes that don't require authentication
    private static final String[] PUBLIC_MATCHERS = {
            "/api/auth/**", // Authentication endpoints (login, register)
            "/oauth2/**",   // OAuth2 callback URIs
            "/public/**",   // Any other public information endpoints
            // Add Swagger/OpenAPI endpoints if needed later
            // "/v3/api-docs/**",
            // "/swagger-ui/**",
            // "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS configuration
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless APIs
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(PUBLIC_MATCHERS).permitAll() // Allow public access to specified paths
                        // Secure Applicant endpoints
                        .requestMatchers("/api/applicant/**").hasRole("APPLICANT") // Require APPLICANT role
                        // Secure Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")       // Require ADMIN role
                        // Any other request must be authenticated (might be redundant now, but safe)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions (JWT)
                )
                .oauth2Login(withDefaults()); // Enable OAuth2 login with default configuration
                // .httpBasic(withDefaults()); // We might disable basic auth later if only using JWT/OAuth

        // Add JWT filter before the standard UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Configure allowed origins, methods, headers carefully for production
        // For development, allowing all might be acceptable, but restrict in production.
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // Allow Angular dev server
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(true); // Allow credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Apply CORS to /api/** paths
        return source;
    }
}