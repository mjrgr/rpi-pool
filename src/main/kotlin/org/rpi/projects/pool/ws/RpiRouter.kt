package org.rpi.projects.pool.ws

import mu.KLogging
import org.rpi.projects.pool.ws.handlers.AboutHandler
import org.rpi.projects.pool.ws.handlers.CheckPinHandler
import org.rpi.projects.pool.ws.handlers.PoolHandler
import org.rpi.projects.pool.ws.handlers.SecurityHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.BodyInserters.fromResource
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@EnableWebFlux
@Configuration
class RpiRouter {

    companion object : KLogging()

    @Bean
    @DependsOn("apiRouter")
    fun resourceRouter(): RouterFunction<ServerResponse> = resources("/rpi-pool/**", ClassPathResource("static/"))

    @Bean
    fun apiRouter(aboutHandler: AboutHandler,
                  securityHandler: SecurityHandler,
                  checkPinHandler: CheckPinHandler,
                  poolHandler: PoolHandler) =
            router {

                ("/favicon.ico").nest {
                    GET("") { ok().body(fromResource(ClassPathResource("/static/favicon.ico"))) }
                }
                ("/api/v1").nest {
                    OPTIONS("/**") { ok().build() }
                    "/about".nest {
                        GET("", aboutHandler::about)
                    }
                    "/security".nest {
                        POST("/signin", securityHandler::login)
                    }
                    "/pin/{number}".nest {
                        GET("", checkPinHandler::getState)
                        PUT("/toggle", checkPinHandler::toggleState)
                        PUT("/low", checkPinHandler::lowState)
                        PUT("/high", checkPinHandler::highState)
                    }
                    "/pool".nest {
                        GET("/relays", poolHandler::relays)
                        GET("/sensors", poolHandler::sensors)
                        "/{relayId}".nest {
                            PUT("/toggle", poolHandler::toggle)
                            PUT("/low", poolHandler::low)
                            PUT("/high", poolHandler::high)
                        }
                    }
                }
            }
}