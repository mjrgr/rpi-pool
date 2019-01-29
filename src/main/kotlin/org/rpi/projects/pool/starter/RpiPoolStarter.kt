package org.rpi.projects.pool.starter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@EnableConfigurationProperties
@SpringBootApplication
@ComponentScan("org.rpi.projects")
class RpiPoolStarter

fun main(args: Array<String>) {
    runApplication<RpiPoolStarter>(*args)
}
