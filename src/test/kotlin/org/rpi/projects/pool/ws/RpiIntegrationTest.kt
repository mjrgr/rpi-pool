package org.rpi.projects.pool.ws

import org.junit.jupiter.api.extension.ExtendWith
import org.rpi.projects.pool.spring.security.JwtTokenService
import org.rpi.projects.pool.starter.RpiPoolStarter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient


@ActiveProfiles("dev", "test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(value = ["Test-Env"], classes = [RpiPoolStarter::class], webEnvironment = RANDOM_PORT)
abstract class RpiIntegrationTest {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var tockenService: JwtTokenService

    val webClient: WebTestClient by lazy {
        WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$port/api/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${tockenService.generateToken("admin")}")
            .build()
    }
}