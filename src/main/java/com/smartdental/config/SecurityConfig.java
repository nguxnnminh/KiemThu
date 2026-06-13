package com.smartdental.config;

import com.smartdental.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cau hinh bao mat tong the.
 * - Form login session-based, mat khau BCrypt.
 * - Phan quyen URL theo role cho tung nhom chuc nang.
 * - Trang loi 403/500 dung chung.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/favicon.svg").permitAll()
                        .requestMatchers("/login", "/error").permitAll()

                        // Nhom 1 - Quan ly he thong
                        .requestMatchers("/system/users/**").hasRole("ADMIN")
                        .requestMatchers("/system/employees/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/system/services/**", "/system/service-categories/**", "/system/service-prices/**").hasAnyRole("ADMIN", "MANAGER")

                        // Nhom 2 - Quan ly lich kham
                        .requestMatchers("/schedule/holidays/**", "/schedule/work-shifts/**", "/schedule/rooms/**")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/schedule/doctor-shifts/**")
                            .hasAnyRole("ADMIN", "MANAGER", "DOCTOR")
                        .requestMatchers("/schedule/appointments/**")
                            .hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST", "DOCTOR")
                        .requestMatchers("/schedule/patients/**")
                            .hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

                        // Nhom 3 - Tiep don va kham benh
                        .requestMatchers("/clinical/checkin/**", "/clinical/queue/**")
                            .hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST", "DOCTOR")
                        .requestMatchers("/clinical/examination/**", "/clinical/medical-records/**", "/clinical/tooth-chart/**", "/clinical/services/**")
                            .hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers("/clinical/invoices/**", "/clinical/payments/**")
                            .hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST", "PATIENT")
                        .requestMatchers("/clinical/revenue/**")
                            .hasAnyRole("ADMIN", "MANAGER")

                        // Nhom 4 - Tinh luong bac si
                        .requestMatchers("/payroll/hourly-rates/**", "/payroll/shift-coefficients/**")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/payroll/complex-cases/my/**")
                            .hasAnyRole("ADMIN", "MANAGER", "DOCTOR")
                        .requestMatchers("/payroll/complex-cases/**")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/payroll/slips/**")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/payroll/my-slips/**", "/payroll/my-yearly-report/**")
                            .hasAnyRole("ADMIN", "MANAGER", "DOCTOR")
                        .requestMatchers("/payroll/reports/monthly/**", "/payroll/reports/doctor-yearly/**", "/payroll/reports/yearly/**")
                            .hasAnyRole("ADMIN", "MANAGER")

                        // Khu vuc benh nhan
                        .requestMatchers("/patient/**").hasRole("PATIENT")

                        .requestMatchers("/", "/dashboard", "/account/**", "/coming-soon").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .exceptionHandling(handling -> handling
                        .accessDeniedPage("/error/403")
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }
}
