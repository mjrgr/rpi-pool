package org.rpi.projects.pool.spring.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import mu.KLogging
import org.rpi.projects.pool.spring.RpiProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*


@Service
class JwtTokenService(private val rpiProperties: RpiProperties) {


    companion object : KLogging() {
        const val BEARER = "Bearer"
    }

    fun generateToken(auth: Authentication) = generateToken(auth.principal as String, auth.authorities)

    fun generateToken(username: String, authorities: Collection<GrantedAuthority> = emptyList()): String = with(Date()) {
        Jwts.builder()
                .setClaims(Jwts
                        .claims()
                        .setSubject(username)
                        .plus("auth" to authorities))
                .setIssuedAt(this)
                .setExpiration(Date(time + Duration.ofMinutes(rpiProperties.jwt.expirationInMinutes.longValueExact()).toMillis()))
                .signWith(SignatureAlgorithm.HS512, rpiProperties.jwt.secret)
                .compact()
    }

    fun resolveToken(request: ServerHttpRequest): String? {
        request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.let {
            if (it.isNotBlank() && it.startsWith(BEARER)) {
                return it.replace("$BEARER ", "")
            }
        }
        return null
    }

    fun getUsername(token: String): String = Jwts.parser()
            .setSigningKey(rpiProperties.jwt.secret)
            .parseClaimsJws(token)
            .body.subject

    fun validateToken(authToken: String) = try {
        Jwts.parser().setSigningKey(rpiProperties.jwt.secret).parseClaimsJws(authToken)
        true
    } catch (ex: Exception) {
        throw BadCredentialsException("invalid JWT token", ex)
    }
}