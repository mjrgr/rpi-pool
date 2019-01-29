package org.rpi.projects.pool.ws.handlers

import org.rpi.projects.pool.model.RpiRelayDto
import org.rpi.projects.pool.model.RpiRelayActionEnum
import org.rpi.projects.pool.model.SensorValue
import org.rpi.projects.pool.sensors.SensorsService
import org.rpi.projects.pool.services.RelayService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class PoolHandler(private val relayService: RelayService,
                  private val sensorsService: SensorsService) {

    fun relays(sr: ServerRequest): Mono<ServerResponse> = ServerResponse.ok().body(relayService.getRelays(), RpiRelayDto::class.java)

    fun sensors(sr: ServerRequest): Mono<ServerResponse> = ServerResponse.ok().body(sensorsService.getLastReadSensorsValues(), SensorValue::class.java)

    fun toggle(sr: ServerRequest): Mono<ServerResponse> = withRequest(sr) {
        relayService.setRelayState(it, RpiRelayActionEnum.TOGGLE)
    }

    fun low(sr: ServerRequest): Mono<ServerResponse> = withRequest(sr) {
        relayService.setRelayState(it, RpiRelayActionEnum.OFF)
    }

    fun high(sr: ServerRequest): Mono<ServerResponse> = withRequest(sr) {
        relayService.setRelayState(it, RpiRelayActionEnum.ON)
    }

    private fun withRequest(request: ServerRequest, action: (String) -> Unit): Mono<ServerResponse> {
        action(request.pathVariable("relayId"))
        return ServerResponse.ok().build()
    }
}
