package main.config;

import main.security.jwt.JwtConfigurer;
import main.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .cors()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/check",
                        "/api/auth/captcha",
                        "/api/auth/register",
                        "/api/auth/restore",
                        //general
                        "/api/init",
                        "/api/tag",
                        "/api/calendar",
                        "/api/settings",
                        "/image/*",
                        //posts
                        "/api/post",
                        "/api/post/*"
                ).permitAll()
                .antMatchers("/api/post/my").hasRole("USER")
                .antMatchers(HttpMethod.PUT, "/api/post/*").hasRole("USER")
                .antMatchers(HttpMethod.POST, "/api/post/like",
                        "api/post/like",
                        "/api/post").hasRole("USER")
                .antMatchers(
                        "/api/moderation",
                        "/api/post/moderation").hasRole("MODERATOR")
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .apply(new JwtConfigurer(jwtTokenProvider));
    }
}