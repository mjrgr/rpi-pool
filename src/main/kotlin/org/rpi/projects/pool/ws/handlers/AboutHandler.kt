package org.rpi.projects.pool.ws.handlers

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import reactor.core.publisher.Mono
import java.net.InetAddress
import java.net.URL
import java.util.jar.Manifest

@Component
class AboutHandler {

    fun about(sr: ServerRequest): Mono<ServerResponse> = ServerResponse.ok().body(BodyInserters.fromObject(getData()))

    internal fun getData(): Map<Any, Any> {
        val classPath = javaClass.getResource(javaClass.simpleName + ".class").toString()
        val data = mutableMapOf("host" to InetAddress.getLocalHost().hostName)
        data.putAll(if (classPath.startsWith("jar")) {
            Manifest(URL(classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF").openStream()).mainAttributes as Map<String, String>
        } else {
            mapOf("version" to "WORKING")
        })
        return data as Map<Any, Any>
    }
}
