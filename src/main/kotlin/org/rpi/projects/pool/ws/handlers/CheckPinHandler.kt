package org.rpi.projects.pool.ws.handlers

import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.RaspiPin
import org.rpi.projects.pool.spring.security.ROLE_ADMIN_STR
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
@PreAuthorize("hasRole('$ROLE_ADMIN_STR')")
class CheckPinHandler(private val controller: GpioController) {

    fun getState(sr: ServerRequest) = withRequest(sr) {
        ServerResponse.ok().body(BodyInserters.fromValue(it.state))
    }

    fun toggleState(sr: ServerRequest) = withRequest(sr) {
        it.toggle()
        ServerResponse.ok().body(BodyInserters.fromValue("Toggle of ${it.name} ok"))
    }

    fun lowState(sr: ServerRequest) = withRequest(sr) {
        it.low()
        ServerResponse.ok().body(BodyInserters.fromValue("Low of ${it.name} ok"))
    }

    fun highState(sr: ServerRequest) = withRequest(sr) {
        it.high()
        ServerResponse.ok().body(BodyInserters.fromValue("High of ${it.name} ok"))
    }

    private fun withRequest(request: ServerRequest, block: (GpioPinDigitalOutput) -> Mono<ServerResponse>): Mono<ServerResponse> {
        val number = request.pathVariable("number")
        RaspiPin.getPinByName("GPIO $number")?.let {
                return block(controller.provisionDigitalOutputPin(it))
        } ?: return ServerResponse.status(HttpStatus.NOT_FOUND).body(BodyInserters.fromValue("Pin with number $number not found"))
    }
}
