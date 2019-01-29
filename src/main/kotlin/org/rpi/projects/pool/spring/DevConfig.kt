package org.rpi.projects.pool.spring

import mu.KLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import java.io.File


@Configuration
@Profile("dev")
class DevConfig(val rpiProperties: RpiProperties) : WebFluxConfigurer, CommandLineRunner {
    companion object : KLogging()

    override fun run(vararg args: String?) {
        rpiProperties.dht11.let {
            with(File(it.pyScript)) {
                if (!exists()) {
                    javaClass.getResourceAsStream("/script/dht11.py").copyTo(outputStream())
                    logger.info { "Created mocked DHT11 mocked script: ${it.pyScript}" }
                }
            }
        }
    }

    override fun addCorsMappings(corsRegistry: CorsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .maxAge(3600)
    }
}