package org.rpi.projects.pool.sensors.dht11

import mu.KLogging
import org.rpi.projects.pool.ext.toList
import org.rpi.projects.pool.model.RpiRelayActionEnum
import org.rpi.projects.pool.model.RpiRelayType
import org.rpi.projects.pool.model.SensorValue
import org.rpi.projects.pool.sensors.ReadSensorsValuesEvent
import org.rpi.projects.pool.sensors.SensorReader
import org.rpi.projects.pool.services.NotifierService
import org.rpi.projects.pool.services.RelayService
import org.rpi.projects.pool.spring.RpiProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux


/**
 * @author mehdi.jaqirgranger
 */
const val DHT11 = "DHT11"
const val CELCIUS = "°C"
const val PERCENT = "%"
const val TEMPERATURE = "temperature"
const val HUMIDITY = "humidity"

@Component
@ConditionalOnProperty(value = ["rpi.dht11.enabled"], havingValue = "true")
class Dht11SensorReader(val rpiProperties: RpiProperties) : SensorReader {

    companion object : KLogging()

    val webClient: WebClient by lazy {
        WebClient
                .builder()
//                .clientConnector(ReactorClientHttpConnector(HttpClient.create()
//                        .tcpConfiguration { client ->
//                            client.option(ChannelOption.SO_TIMEOUT, 10000)
//                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
//                                    .doOnConnected { conn -> conn.addHandlerLast(ReadTimeoutHandler(10)).addHandlerLast(WriteTimeoutHandler(10)) }
//                        }))
                .baseUrl(rpiProperties.dht11.url)
                .build()
    }

    val dht11FallbackCmd: ProcessBuilder by lazy {
        ProcessBuilder().command("python2", rpiProperties.dht11.pyScript, "--no-ws", if (rpiProperties.realGpio) "--no-mock" else "--mock")
    }

    override val name: String = DHT11

    override fun readSensorValues(): List<SensorValue> = webClient.get().uri("/").retrieve()
            .bodyToMono(String::class.java)
            .flatMapMany(::fromDHT11Value)
            .onErrorResume { dht11FallbackCmd.start().inputStream.reader().use { s -> fromDHT11Value(s.readText().trim()) } }
            .doOnError { logger.error { "Unable to execute dht11 script: ${it.message}" } }
            .toList()

    private fun fromDHT11Value(value: String): Flux<SensorValue> = try {
        value.split('|').let {
            return Flux.just(SensorValue(TEMPERATURE, it[0].toInt(), CELCIUS),
                    SensorValue(HUMIDITY, it[1].toInt(), PERCENT))
        }
    } catch (ex: Exception) {
        throw IllegalArgumentException("DHT11 value $value has not the expected format %d|%d", ex)
    }
}


@Component
class Dht11SensorListener(private val rpiProperties: RpiProperties,
                          private val relayService: RelayService,
                          private val notifierService: NotifierService, private val passwordEncoder: PasswordEncoder) {

    companion object : KLogging()

    @EventListener
    fun onValuesRead(event: ReadSensorsValuesEvent) = event.sensorsValues.asSequence().filter { it.name == TEMPERATURE }.forEach(::handleTemperature)

    private fun handleTemperature(temp: SensorValue) {
        logger.info { "Temperature value: $temp | threshold is ${rpiProperties.dht11.temperatureThreshold}" }
        relayService.getRelays().filter { RpiRelayType.PUMP == it.type }.subscribe {
            if (temp.value < rpiProperties.dht11.temperatureThreshold.toInt()) {
                if (it.state) {
                    logger.info { "Pump is already on, nothing to do" }
                } else {
                    relayService.setRelayState(it, RpiRelayActionEnum.ON)
                    notifierService.notifyAll("The ambient temperature $temp is below the threshold of ${rpiProperties.dht11.temperatureThreshold}. The pump was turned on.")
                }
            }
        }
    }
}