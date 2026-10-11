package lk.ac.sliit.se1020.pagenest.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(@Value("${PAGENEST_ADMIN_PASSWORD:}") String adminPassword,
                             @Value("${PAGENEST_CUSTOMER_PASSWORD:}") String customerPassword,
                             PasswordEncoder encoder) {
        if (adminPassword.isBlank() || customerPassword.isBlank()) {
            throw new IllegalStateException("Set PAGENEST_ADMIN_PASSWORD and PAGENEST_CUSTOMER_PASSWORD before starting PageNest.");
        }
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN").build(),
                User.withUsername("customer").password(encoder.encode(customerPassword)).roles("CUSTOMER").build());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/books", "/books/*", "/login", "/access-denied",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .formLogin(form -> form.loginPage("/login").successHandler(this::onLoginSuccess).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
                .exceptionHandling(errors -> errors.accessDeniedPage("/access-denied"))
                .build();
    }

    private void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        requestCache.removeRequest(request, response);

        String destination = admin ? "/admin" : "/books";
        if (admin && savedRequest != null && savedRequest.getRedirectUrl().startsWith(
                request.getRequestURL().toString().replace(request.getRequestURI(), "") + request.getContextPath() + "/admin")) {
            destination = savedRequest.getRedirectUrl();
        }
        response.sendRedirect(destination);
    }
}
