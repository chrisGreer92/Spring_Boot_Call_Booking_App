package com.example.bookingsystem.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${admin.username}") private String adminUsername;
    @Value("${admin.password}") private String adminPassword;

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

                    // Only Patch request and Defined GET for api is permitted
                    .requestMatchers(HttpMethod.PATCH, "/booking/request/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/booking/public").permitAll()
                    // Plus any static resources
                    .requestMatchers("/*.html", "/*.js", "/*.css", "/*.ico").permitAll()
                    // Everything else requires auth
                    .anyRequest().authenticated()
                ).httpBasic(Customizer.withDefaults());

        return http.build();

    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(encoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build()
        );
    }
}
