package org.rpi.projects.pool.spring.security

import org.rpi.projects.pool.spring.RpiProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(private val rpiProperties: RpiProperties,
                     val userDetailsService: RpiUserDetailsService,
                     val jwtTokenService: JwtTokenService) {

    @Bean
    fun passwordEncoder() = Pbkdf2PasswordEncoder(rpiProperties.passwordEncoder.secret, rpiProperties.passwordEncoder.iteration.toInt(), rpiProperties.passwordEncoder.keylength.toInt())

    @Bean
    fun reactiveAuthenticationManager() = RpiAuthenticationManager(jwtTokenService, userDetailsService)

    @Bean
    fun securityContextRepository() = RpiSecurityContextRepository(jwtTokenService, reactiveAuthenticationManager())

    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

        http.httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                .logout().disable()
                .exceptionHandling()
                .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))

        http
                .authenticationManager(reactiveAuthenticationManager())
                .securityContextRepository(securityContextRepository())

        http
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/actuator/**", "/favicon.ico", "/v2/api-docs/**", "/swagger-resources/**", "/swagger-ui.html/**", "/configuration/**", "/webjars/**", "/rpi-pool/**").permitAll()
                .pathMatchers("/api/v1/about", "/api/v1/security/**").permitAll()
                .and()
                .authorizeExchange()
                .anyExchange()
                .authenticated()

        return http.build()
    }

    class RpiAuthenticationManager(private val jwtTokenService: JwtTokenService,
                                   private val userDetailsService: RpiUserDetailsService) : ReactiveAuthenticationManager {

        override fun authenticate(authentication: Authentication): Mono<Authentication> {

            (authentication.credentials as String).let { token ->
                if (jwtTokenService.validateToken(token)) {
                    return userDetailsService.findByUsername(jwtTokenService.getUsername(token)).map {
                        UsernamePasswordAuthenticationToken(it.username, null, it.authorities)
                    }
                }
            }
            return Mono.empty()
        }
    }

    class RpiSecurityContextRepository(private val jwtTokenService: JwtTokenService,
                                       private val authenticationManager: ReactiveAuthenticationManager) : ServerSecurityContextRepository {

        override fun save(swe: ServerWebExchange, sc: SecurityContext): Mono<Void> = throw UnsupportedOperationException("Not supported yet.")

        override fun load(swe: ServerWebExchange): Mono<SecurityContext> = jwtTokenService.resolveToken(swe.request)?.let { token ->
            return authenticationManager.authenticate(UsernamePasswordAuthenticationToken(token, token)).map { SecurityContextImpl(it) }
        } ?: Mono.empty()
    }
}