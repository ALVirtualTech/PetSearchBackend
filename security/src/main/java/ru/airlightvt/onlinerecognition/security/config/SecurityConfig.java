package ru.airlightvt.onlinerecognition.security.config;

import org.springframework.context.annotation.ComponentScan;
import ru.airlightvt.onlinerecognition.security.rest.CustomSavedRequestAwareAuthenticationSuccessHandler;
import ru.airlightvt.onlinerecognition.security.rest.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@ComponentScan("ru.airlightvt.onlinerecognition.security")
@EnableWebSecurity
public class SecurityConfig  extends WebSecurityConfigurerAdapter {
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            @Qualifier("customUserDetailsService") UserDetailsService userDetailsService)
    {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder());
    }

    @Bean
    public PasswordEncoder encoder() {
        return org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CustomSavedRequestAwareAuthenticationSuccessHandler customSuccessHandler() {
        return new CustomSavedRequestAwareAuthenticationSuccessHandler();
    }

    // return 401 instead 302
    @Bean
    public SimpleUrlAuthenticationFailureHandler customFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable() // !!! enable in production mode
                .exceptionHandling()
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .authorizeRequests()
                    .antMatchers("/api/web").authenticated()
                    .antMatchers("/api/web/admin/**").hasRole("ADMIN")
                .and()
                // authentication processing filter: org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
                // formLogin create this filter and provide additional methods (onSuccess and onFailure)
                // so we can use URL "/login" to log in by REST
                .formLogin()
                    .successHandler(customSuccessHandler())
                    .failureHandler(customFailureHandler())
                .and()
                .logout();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
