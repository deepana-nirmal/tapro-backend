package qr_ordering_system.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import qr_ordering_system.config.TenantFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final TenantFilter tenantFilter;

    public SecurityConfig(JwtFilter jwtFilter, TenantFilter tenantFilter) {
        this.jwtFilter = jwtFilter;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .authorizeHttpRequests(auth -> auth

                        // H2 Console
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public Authentication
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public customer menu and menu images
                        .requestMatchers(HttpMethod.GET, "/api/menu-items/restaurant/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-items/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tables/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/qr/tables/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/public/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/logos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()

                        // Invitation Flow
                        .requestMatchers("/api/invitations").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/invitations/verify").permitAll()
                        .requestMatchers("/api/invitations/accept").permitAll()

                        // Admin / Super Admin
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")

                        // Owner
                        .requestMatchers("/api/owner/**")
                        .hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")

                        // Staff / Cashier
                        .requestMatchers("/api/staff/**")
                        .hasAnyRole("STAFF", "CASHIER", "ADMIN", "SUPER_ADMIN")

                        // Kitchen
                        .requestMatchers("/api/kitchen/**")
                        .hasAnyRole("KITCHEN", "ADMIN", "SUPER_ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(
                tenantFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
