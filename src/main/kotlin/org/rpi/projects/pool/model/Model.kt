package org.rpi.projects.pool.model

import com.pi4j.component.relay.Relay
import com.pi4j.component.relay.RelayState

/**
 * @author mehdi.jaqirgranger
 */

class RpiRelayDto(val id: String,
                  val type: RpiRelayType,
                  val label: String,
                  val state: Boolean)

data class RpiRelay(val pinNumber: Int,
                    val id: String,
                    val label: String,
                    val type: RpiRelayType = RpiRelayType.CUSTOM,
                    val enabled: Boolean = true) {
    fun toDto(state: RelayState) = RpiRelayDto(id, type, label, state == RelayState.OPEN)
}

enum class RpiRelayActionEnum(val fire: (Relay) -> RelayState) {

    TOGGLE({
        it.toggle()
        it.state
    }),
    ON({
        it.open()
        it.state
    }),
    OFF({
        it.close()
        it.state
    })
}

enum class RpiRelayType {
    PUMP, LIGHT, CUSTOM;
}

data class SensorValue(val name: String, val value: Long, val unit: String) {
    operator fun compareTo(other: SensorValue): Int = value.compareTo(other.value)
    override fun toString(): String {
        return "[$value$unit]"
    }
}

data class RpiUser(val username: String, val password: String, val admin: Boolean)

data class UserCredentials(val username: String, val password: String)

data class UserWithToken(val token: String, val tokenType: String = "Bearer")
