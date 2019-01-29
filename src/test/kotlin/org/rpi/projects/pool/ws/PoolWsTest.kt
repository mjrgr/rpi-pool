package org.rpi.projects.pool.ws

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import java.net.InetAddress

/**
 * @author mehdi.jaqirgranger
 */
class PoolWsTest : RpiIntegrationTest() {

    @Test
    fun `test application home is reachable`() {
        webClient.get().uri("/about").exchange().expectBody<Map<String, String>>()
                .returnResult().apply {
                    Assertions.assertThat(responseBody).isNotNull
                            .extracting("version", "host")
                            .containsExactly("WORKING", InetAddress.getLocalHost().hostName)
                }
    }
}
