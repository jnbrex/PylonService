package com.pylon.pylonservice.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Map;

import static com.pylon.pylonservice.constants.EnvironmentConstants.BETA_ENVIRONMENT_NAME;
import static com.pylon.pylonservice.constants.EnvironmentConstants.LOCAL_ENVIRONMENT_NAME;
import static com.pylon.pylonservice.constants.EnvironmentConstants.PROD_ENVIRONMENT_NAME;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Map<String, String> ENVIRONMENT_NAME_TO_ALLOWED_ORIGIN_MAPPING = Map.of(
        LOCAL_ENVIRONMENT_NAME, "*",
        BETA_ENVIRONMENT_NAME, "*",
        PROD_ENVIRONMENT_NAME, "https://pylon.gg"
    );

    @Value("${environment.name}")
    private String environmentName;

    @Autowired
    private AccessTokenAuthenticationEntryPoint accessTokenAuthenticationEntryPoint;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AccessTokenRequestFilter accessTokenRequestFilter;
    @Autowired
    private CSRFProtectionRequestFilter csrfProtectionRequestFilter;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(ENVIRONMENT_NAME_TO_ALLOWED_ORIGIN_MAPPING.get(environmentName)));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Cache-Control",
            "Content-Type",
            "X-Requested-With"
        ));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // We don't need CSRF because all authenticated endpoints require a JWT.
            .csrf().disable()
            // Enable CORS for all routes
            .cors().and()
            // Don't authenticate the following <HttpMethod, antPattern> tuples
            .authorizeRequests()
                .antMatchers(HttpMethod.POST,
                    "/authenticate",
                    "/collectemail",
                    "/password/**",
                    "/refresh",
                    "/register"
                ).permitAll()
                .antMatchers(HttpMethod.GET,
                    "/health",
                    "/post/**",
                    "/profile/**",
                    "/shard/**",
                    "/user/**"
                ).permitAll()
            // all other requests need to be authenticated
            .anyRequest().authenticated().and()
            .exceptionHandling().authenticationEntryPoint(accessTokenAuthenticationEntryPoint).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            // Add a filter to validate the tokens with every request
            .addFilterBefore(accessTokenRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(csrfProtectionRequestFilter, CsrfFilter.class)
            // A hack so that the real logout url can be /logout
            .logout().logoutUrl("/defaultLogout");
    }
}
