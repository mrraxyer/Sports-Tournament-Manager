package dev.mrraxyer.sportstournamentmanager.config;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.security.Sha1PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Sha1PasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/usuarios", "/api/usuarios/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint())
            );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                ApiResponse<Object> api = ApiResponseBuilder.error("No autorizado", HttpServletResponse.SC_UNAUTHORIZED)
                        .path(request.getRequestURI())
                        .build();

                String json = String.format("{\"status\":%d,\"message\":\"%s\",\"path\":\"%s\"}",
                        api.getStatus(), api.getMessage(), api.getPath());

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write(json);
            }
        };
    }
}


