package com.example.bookingsystem.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //Use stateless sessions (token based)
        //Disable CSRF protection (cross site request forgery)
        //Authorise which endpoints public/private
        http
                .sessionManagement(c ->
                    c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(c -> c
                    // For (manual) api testing
//                     .anyRequest().permitAll()

                    // Production: Only POST /booking is public
                    .requestMatchers(HttpMethod.POST, "/booking").permitAll()
                    .anyRequest().authenticated()
                );

        return http.build();

    }
}
