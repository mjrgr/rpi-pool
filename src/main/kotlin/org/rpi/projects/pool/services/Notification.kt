package org.rpi.projects.pool.services

import kotlinx.coroutines.Dispatchers
import mu.KLogging
import org.rpi.projects.pool.ext.parallelMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author mehdi.jaqirgranger
 */
interface NotificationService {

    fun notify(message: String)
}

@Component
class NotifierService(@Autowired(required = false) private val notificationServices: List<NotificationService> = listOf()) {

    companion object : KLogging()

    fun notifyAll(message: String) {
        if (notificationServices.isEmpty()) {
            logger.debug { "No notification service available for message: $message" }
        } else {
            notificationServices.parallelMap(Dispatchers.IO) { it.notify(message) }
        }
    }
}