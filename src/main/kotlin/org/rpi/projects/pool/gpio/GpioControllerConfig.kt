package org.rpi.projects.pool.gpio

import com.pi4j.io.gpio.*
import mu.KLogging
import org.rpi.projects.pool.ext.fullName
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * @author mehdi.jaqirgranger
 */
@Configuration
class GpioControllerConfig {

    companion object : KLogging()

    @Bean
    fun gpioController(provider: GpioProvider): GpioController {
        logger.info { "Creating GPIO controller with provider: ${provider.fullName()}" }
        GpioFactory.setDefaultProvider(provider)
        return GpioFactory.getInstance()
    }

    @Bean
    @ConditionalOnProperty(name = ["rpi.real-gpio"], havingValue = "false", matchIfMissing = true)
    fun gpioSimulatedProvider() = SimulatedGpioProvider().apply {
        SimulatedGpioProvider.NAME = RaspiGpioProvider.NAME
    }

    @Bean
    @ConditionalOnProperty(name = ["rpi.real-gpio"])
    fun gpioRealProvider() = RaspiGpioProvider()
}