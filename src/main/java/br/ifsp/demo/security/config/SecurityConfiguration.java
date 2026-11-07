package br.ifsp.demo.security.config;

import br.ifsp.demo.security.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final String[] WHITE_LIST_URL = {
            "/api/v1/authenticate",
            "/api/v1/register",
            "/api/v1/api-docs",
            "/api/v1/api-docs/**",
            "/api/v1/openapi",
            "/api/v1/openapi/**",
            "/api/v1/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private static final String USER = Role.USER.name();
    private static final String ADMIN = Role.ADMIN.name();
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me")
                        .hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/activity-classes",
                                "/api/v1/activity-classes/*")
                        .hasAnyAuthority(USER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/activity-classes")
                        .hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/activity-classes/*")
                        .hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/sports",
                                "/api/v1/rooms",
                                "/api/v1/trainers")
                        .hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/activity-reports")
                        .hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/me/enrollment")
                        .hasAuthority(USER)
                        .requestMatchers(HttpMethod.POST, "/api/v1/me/enrollment")
                        .hasAuthority(USER)
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/me/enrollment/activities/*")
                        .hasAuthority(USER)
                        .requestMatchers(HttpMethod.GET, "/api/v1/hello")
                        .hasAnyAuthority(USER, ADMIN)
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(it -> it
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );
        return http.build();
    }
}
