package org.rpi.projects.pool.spring.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.rpi.projects.pool.model.RpiUser
import org.rpi.projects.pool.services.RelayService
import org.rpi.projects.pool.spring.RpiProperties
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

const val ROLE_PREFIX = "ROLE_"
const val ROLE_USER_STR = "USER"
const val ROLE_ADMIN_STR = "ADMIN"

val ROLE_USER = SimpleGrantedAuthority("$ROLE_PREFIX$ROLE_USER_STR")
val ROLE_ADMIN = SimpleGrantedAuthority("$ROLE_PREFIX$ROLE_ADMIN_STR")

val USER = listOf<GrantedAuthority>(ROLE_USER)
val ADMIN = listOf<GrantedAuthority>(ROLE_USER, ROLE_ADMIN)

@Service
class RpiUserDetailsService(rpiProperties: RpiProperties,
                            private val objectMapper: ObjectMapper) : ReactiveUserDetailsService {

    private val users: Map<String, UserDetails> = rpiProperties.config.users.let { users ->
        users.inputStream.use { stream ->
            RelayService.logger.info { "loading users: ${users.url}" }
            return@let objectMapper.readValue<List<RpiUser>>(stream).map { it.username to User(it.username, it.password, if (it.admin) ADMIN else USER) }.toMap()
        }
    }

    override fun findByUsername(username: String): Mono<UserDetails> = users[username]?.let {
        Mono.just(it)
    } ?: throw UsernameNotFoundException("User not found by name: $username")
}