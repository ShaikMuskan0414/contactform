
package com.example.contactform;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Allow REST API requests without CSRF tokens.
            // For a local internship demo only.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/contacts/**")
            )

            // Configure access permissions
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/index.html", 
                    "/style.css", 
                    "/success.html"
                ).permitAll()

                // Allow the public contact form to submit
                .requestMatchers("/contact").permitAll()

                // Only ADMIN can access contact management
                .requestMatchers(
                    "/contacts", 
                    "/contacts/**", 
                    "/contacts.html"
                ).hasRole("ADMIN")

                // All other requests require login
                .anyRequest().authenticated()
            )

            // Login configuration
            .formLogin(form -> form
                .defaultSuccessUrl("/contacts.html", true)
                .permitAll()
            )

            // Logout configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    // Create the ADMIN user
    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("Admin@123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }

    // Encrypt the password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}