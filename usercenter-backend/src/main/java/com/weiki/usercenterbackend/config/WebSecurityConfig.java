package com.weiki.usercenterbackend.config;

import com.weiki.usercenterbackend.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static com.weiki.usercenterbackend.constant.UserConstant.ADMIN_ROLE;

/**
 * Web安全配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 注册JWT认证过滤器
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
    
    /**
     * Actuator安全配置
     */
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .antMatcher("/actuator/**")
            .authorizeRequests()
                .antMatchers("/actuator/health/**").permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
            .httpBasic();
        
        return http.build();
    }
    
    /**
     * 主应用安全配置
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .authorizeRequests()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/", "/swagger-ui/**", "/v3/api-docs/**", "/doc.html").permitAll()
                .anyRequest().permitAll()
                .and()
            .csrf().disable();
        
        return http.build();
    }
    
    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 内存用户服务，用于Actuator访问
     */
    @Bean 
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("admin")
            .password(passwordEncoder().encode("adminPassword"))
            .roles("ADMIN")
            .build());
        return manager;
    }
} 