package qr_ordering_system.security;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

import qr_ordering_system.config.TenantFilter;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtFilter jwtFilter;
    private final TenantFilter tenantFilter;

    @Value("${app.frontend-urls:http://localhost:3000,http://127.0.0.1:3000}")
    private String frontendUrls;

    public SecurityConfig(JwtFilter jwtFilter, TenantFilter tenantFilter) {
        this.jwtFilter = jwtFilter;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
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

                        .requestMatchers("/api/invitations").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/invitations/verify").permitAll()
                        .requestMatchers("/api/invitations/accept").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")

                        .requestMatchers("/api/owner/**").hasAnyRole("OWNER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "CASHIER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/kitchen/**").hasAnyRole("KITCHEN", "ADMIN", "SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowedOrigins = resolveAllowedOrigins();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setMaxAge(3600L);

        logger.info("Allowed CORS origins: {}", allowedOrigins);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> resolveAllowedOrigins() {
        Set<String> allowedOrigins = new LinkedHashSet<>();

        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://127.0.0.1:3000");

        logger.info("Raw app.frontend-urls value: {}", frontendUrls);

        if (frontendUrls != null && !frontendUrls.isBlank()) {
            for (String origin : frontendUrls.split(",")) {
                String normalizedOrigin = normalizeOrigin(origin);
                if (!normalizedOrigin.isBlank()) {
                    allowedOrigins.add(normalizedOrigin);
                }
            }
        }

        return new ArrayList<>(allowedOrigins);
    }

    private String normalizeOrigin(String origin) {
        if (origin == null) return "";

        String normalized = origin.trim();
        if (normalized.isBlank()) return "";

        if (normalized.startsWith("[") && normalized.contains("](") && normalized.endsWith(")")) {
            int closingBracket = normalized.indexOf("](");
            normalized = normalized.substring(closingBracket + 2, normalized.length() - 1).trim();
        }

        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        String lower = normalized.toLowerCase(Locale.ROOT);

        if (lower.startsWith("https://") || lower.startsWith("http://")) {
            return normalized;
        }

        logger.warn("Ignoring invalid CORS origin value: {}", origin);
        return "";
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}