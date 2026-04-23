package com.example.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authorization.AuthorizationManagerFactories;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;

import javax.sql.DataSource;
import java.io.IOException;

import static org.springaicommunity.mcp.security.authorizationserver.config.McpAuthorizationServerConfigurer.mcpAuthorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    Customizer<HttpSecurity> httpSecurityCustomizer() {
        var amf = AuthorizationManagerFactories.multiFactor() //
                .requireFactors(FactorGrantedAuthority.PASSWORD_AUTHORITY, FactorGrantedAuthority.OTT_AUTHORITY) //
                .build(); //

        return http -> http//
                .oauth2AuthorizationServer(a -> a.oidc(Customizer.withDefaults()))
                .with(mcpAuthorizationServer(), a -> a
                        .authorizationServer(a1 -> a1.oidc(Customizer.withDefaults())))
                .authorizeHttpRequests(a -> a.requestMatchers("/admin").access(amf.hasRole("ADMIN")))
                .webAuthn(a -> a //
                        .rpId("localhost") //
                        .rpName("bootiful") //
                        .allowedOrigins("http://localhost:8080") //
                )
                .oneTimeTokenLogin(ott -> ott.tokenGenerationSuccessHandler(new OneTimeTokenGenerationSuccessHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
                            throws IOException, ServletException {

                        response.getWriter().println("you've got console mail!");
                        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

                        IO.println("please go to http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue());

                    }
                }));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        var u = new JdbcUserDetailsManager(dataSource);
        u.setEnableUpdatePassword(true);
        return u;
    }

}