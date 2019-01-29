package org.rpi.projects.pool.ext

import com.pi4j.component.relay.Relay
import com.pi4j.component.relay.impl.GpioRelayComponent
import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioProvider
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.io.gpio.SimulatedGpioProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.rpi.projects.pool.model.RpiRelay
import org.rpi.projects.pool.model.RpiRuntimeException
import reactor.core.publisher.Flux
import kotlin.coroutines.CoroutineContext

/**
 * @author mehdi.jaqirgranger
 */
fun <A, B> Iterable<A>.parallelMap(context: CoroutineContext, mapFun: suspend (A) -> B): List<B> = runBlocking(context) {
    map { async { mapFun(it) } }.map { it.await() }
}

fun <T> Flux<T>.toList() = mutableListOf<T>().apply {
    addAll(this@toList.toIterable())
}

fun GpioProvider.fullName(): String {
    return "${this.name}${if (this is SimulatedGpioProvider) " (Simulated)" else ""}"
}

inline fun <T> GpioController.withProvisionedPin(relay: RpiRelay, block: (Relay) -> T): T {
    if (relay.enabled) {
        RaspiPin.getPinByName("GPIO " + relay.pinNumber)?.let {
            with(this.provisionDigitalOutputPin(it)) {
                val res = block(GpioRelayComponent(this))
                unprovisionPin(this)
                return res
            }
        } ?: throw RpiRuntimeException("Pin with number " + relay.pinNumber + " not found")
    } else {
        throw RpiRuntimeException("Relay $relay is disabled")
    }
}