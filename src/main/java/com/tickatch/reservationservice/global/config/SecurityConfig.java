package com.tickatch.reservationservice.global.config;

import io.github.tickatch.common.security.BaseSecurityConfig;
import io.github.tickatch.common.security.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

  @Bean
  @Override
  protected LoginFilter loginFilterBean() {
    return new LoginFilter();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return build(http);
  }

  @Override
  protected Customizer<
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>
  authorizeHttpRequests() {
    return registry -> registry.anyRequest().permitAll();
  }
}