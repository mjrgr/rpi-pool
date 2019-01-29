package org.rpi.projects.pool.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pi4j.component.relay.RelayState
import com.pi4j.io.gpio.GpioController
import mu.KLogging
import org.rpi.projects.pool.ext.withProvisionedPin
import org.rpi.projects.pool.model.*
import org.rpi.projects.pool.model.RpiRelayActionEnum.OFF
import org.rpi.projects.pool.spring.RpiProperties
import org.springframework.stereotype.Service
import reactor.core.publisher.toFlux
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @author mehdi.jaqirgranger
 */
@Service
class RelayService(private val rpiProperties: RpiProperties,
                   private val controller: GpioController,
                   private val objectMapper: ObjectMapper) {

    companion object : KLogging()

    private lateinit var relays: List<RpiRelay>

    @PostConstruct
    fun init() {
        rpiProperties.config.pool.let { cfg ->
            cfg.inputStream.use { stream ->
                relays = objectMapper.readValue<List<RpiRelay>>(stream).filter { it.enabled }
                logger.info { "Config loaded: ${cfg.url}" }
            }
        }


        if (relays.isEmpty()) {
            throw RpiRuntimeException("Pool relays config cannot be null")
        } else {
            relays.firstOrNull { RpiRelayType.PUMP == it.type }
                    ?: RpiRuntimeException("you must declare at least one pump")
            setAllRelaysStates(OFF)
        }
    }

    @PreDestroy
    fun destroy() {
        setAllRelaysStates(OFF)
        TimeUnit.SECONDS.sleep(1L)
    }

    fun setRelayState(id: String, action: RpiRelayActionEnum) {
        setRelayState(getRelayById(id), action)
    }

    private fun setAllRelaysStates(action: RpiRelayActionEnum) {
        relays.forEach { p -> this.setRelayState(p, action) }
    }

    fun setRelayState(rpiRelayDto: RpiRelayDto, type: RpiRelayActionEnum) {
        setRelayState(getRelayById(rpiRelayDto.id), type)
    }

    fun setRelayState(rpiRelay: RpiRelay, type: RpiRelayActionEnum) {
        logger.info { "Firing action $type to module $rpiRelay" }
        controller.withProvisionedPin(rpiRelay) {
            type.fire(it)
        }
    }

    fun getRelayState(rpiRelay: RpiRelay): RelayState = controller.withProvisionedPin(rpiRelay) {
        it.state
    }

    fun getRelayState(rpiRelayDto: RpiRelayDto): RelayState = getRelayState(getRelayById(rpiRelayDto.id))

    fun getRelays() = relays.map { cfg ->
        cfg.toDto(getRelayState(cfg))
    }.toFlux()

    private fun getRelayById(id: String) = relays.firstOrNull { it.id == id }
            ?: throw RpiRuntimeException("Relay $id not found")
}
