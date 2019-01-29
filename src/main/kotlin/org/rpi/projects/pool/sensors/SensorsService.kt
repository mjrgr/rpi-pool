package org.rpi.projects.pool.sensors

import kotlinx.coroutines.Dispatchers
import mu.KLogging
import org.rpi.projects.pool.ext.parallelMap
import org.rpi.projects.pool.model.SensorValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux

/**
 * @author mehdi.jaqirgranger
 */
interface SensorReader {
    val name: String
    fun readSensorValues(): List<SensorValue>
}

data class ReadSensorsValuesEvent(val src: Any, val sensorsValues: List<SensorValue>) : ApplicationEvent(src)

@Service
class SensorsService(private val applicationEventPublisher: ApplicationEventPublisher,
                     @Autowired(required = false) private val sensors: List<SensorReader> = emptyList()) {

    companion object : KLogging()

    private var lastReadSensorsValues: List<SensorValue> = emptyList()

    @Scheduled(initialDelay = 5000, fixedDelayString = "#{\${rpi.read-delay-seconds}*1000}")
    fun readSensorsValues() {
        if (sensors.isNotEmpty()) {
            lastReadSensorsValues = sensors.parallelMap(Dispatchers.IO) { sensor ->
                sensor.readSensorValues().apply {
                    logger.info { "Information from ${sensor.name}: $this" }
                }
            }.flatten()
            applicationEventPublisher.publishEvent(ReadSensorsValuesEvent(this, lastReadSensorsValues))
        } else {
            logger.info { "No sensors available" }
        }
    }

    fun getLastReadSensorsValues(): Flux<SensorValue> = lastReadSensorsValues.toFlux()
}
