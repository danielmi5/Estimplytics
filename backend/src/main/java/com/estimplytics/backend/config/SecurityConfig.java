package com.estimplytics.backend.config;

import com.estimplytics.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
				.requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers("/api/v1/redmine-credentials/**").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers(HttpMethod.GET, "/api/requests/**", "/api/projects/**", "/api/components/**", "/api/estimation-histories/**", "/api/impact-analysis-histories/**", "/api/impact-analyses/**", "/api/component-analyses/**", "/api/estimations/**").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers(HttpMethod.POST, "/api/requests/**", "/api/projects/**", "/api/impact-analyses/**", "/api/component-analyses/**", "/api/estimations/**").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers(HttpMethod.PUT, "/api/requests/**", "/api/projects/**", "/api/impact-analyses/**", "/api/component-analyses/**", "/api/estimations/**").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers(HttpMethod.DELETE, "/api/requests/**", "/api/projects/**", "/api/impact-analyses/**", "/api/component-analyses/**").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers(HttpMethod.POST, "/api/components/**").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/components/**").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/api/components/**").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/redmine/sync").hasAnyAuthority("ADMIN", "ANALYST")
				.requestMatchers("/api/**").hasAuthority("ADMIN")
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
