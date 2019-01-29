package org.rpi.projects.pool.spring

import com.fasterxml.jackson.annotation.JsonIgnore
import org.rpi.projects.pool.model.SensorValue
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
@ConfigurationProperties(prefix = "rpi")
class RpiProperties {
    var realGpio: Boolean = true
    @JsonIgnore
    val config = ConfigProperties()
    val jwt = JwtProperties()
    val passwordEncoder = PasswordEncoderProperties()
    val freemobileNotif = FreeMobileNotificationProperties()
    val dht11 = Dht11Properties()

    class ConfigProperties {
        lateinit var users: Resource
        lateinit var pool: Resource
    }

    class JwtProperties {
        lateinit var secret: String
        lateinit var expirationInMinutes: BigInteger
    }

    class PasswordEncoderProperties {
        lateinit var secret: String
        lateinit var iteration: BigInteger
        lateinit var keylength: BigInteger
    }

    class FreeMobileNotificationProperties {
        lateinit var user: String
        lateinit var pass: String
    }

    class Dht11Properties {
        lateinit var pyScript: String
        lateinit var url: String
        lateinit var temperatureThreshold: SensorValue
    }
}