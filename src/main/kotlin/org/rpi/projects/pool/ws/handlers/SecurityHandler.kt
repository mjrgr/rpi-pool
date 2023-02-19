package org.rpi.projects.pool.ws.handlers

import org.rpi.projects.pool.model.UserCredentials
import org.rpi.projects.pool.model.UserWithToken
import org.rpi.projects.pool.spring.security.JwtTokenService
import org.rpi.projects.pool.spring.security.RpiUserDetailsService
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class SecurityHandler(val userDetailsService: RpiUserDetailsService,
                      private val passwordEncoder: PasswordEncoder,
                      val jwtTokenService: JwtTokenService) {

    fun login(sr: ServerRequest): Mono<ServerResponse> = sr.bodyToMono(UserCredentials::class.java).flatMap { uc ->
        userDetailsService.findByUsername(uc.username).map {
            if (passwordEncoder.matches(uc.password, it.password)) {
                UsernamePasswordAuthenticationToken(it.username, it.password, it.authorities)
            } else {
                throw BadCredentialsException("Invalid credentials")
            }
        }
    }.flatMap {
        ServerResponse.ok().bodyValue(UserWithToken(jwtTokenService.generateToken(it)))
    }
}
