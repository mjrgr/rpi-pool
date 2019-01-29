package org.rpi.projects.pool.services.fm

import mu.KLogging
import org.rpi.projects.pool.services.NotificationService
import org.rpi.projects.pool.spring.RpiProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@ConditionalOnProperty(value = ["rpi.freemobile-notif.enabled"], havingValue = "true")
class FreeMobileNotificationService(private val rpiProperties: RpiProperties,
                                    private val restTemplate: RestTemplate) : NotificationService {

    companion object : KLogging() {
        private const val API_URL = "https://smsapi.free-mobile.fr/sendmsg?user={user}&pass={pass}&msg={msg}"
    }

    override fun notify(message: String) {
        restTemplate.getForEntity(API_URL, Any::class.java, mapOf("user" to rpiProperties.freemobileNotif.user, "pass" to rpiProperties.freemobileNotif.pass, "msg" to message))
        logger.info { "SMS notification sent with message: $message" }
    }
}
