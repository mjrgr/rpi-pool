package org.rpi.projects.pool.spring

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.rpi.projects.pool.model.ApiError
import org.rpi.projects.pool.model.SensorValue
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ConversionServiceFactoryBean
import org.springframework.core.Ordered
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.core.codec.Hints
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@Configuration
@EnableWebFlux
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
class AppSpringConfig : AsyncConfigurer {

    companion object : KLogging()

    @Bean
    fun conversionService(): ConversionService = ConversionServiceFactoryBean().apply {
        setConverters(setOf(Converter<Int, SensorValue> {
            SensorValue("", it.toLong(), "")
        }))
        afterPropertiesSet()
    }.`object`!!

    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
            .findAndRegisterModules()

    @Bean
    fun taskScheduler() = ThreadPoolTaskScheduler().apply {
        setWaitForTasksToCompleteOnShutdown(false)
        poolSize = 20
        setErrorHandler { logger.error(it) { "An error occurred in scheduled task" } }
    }

    override fun getAsyncExecutor() = ThreadPoolTaskExecutor().apply {
        corePoolSize = Runtime.getRuntime().availableProcessors()
        maxPoolSize = Runtime.getRuntime().availableProcessors()
        threadNamePrefix = "RpiAsync-"
        initialize()
    }

    override fun getAsyncUncaughtExceptionHandler() = AsyncUncaughtExceptionHandler { t, _, _ ->
        logger.error(t) { "An error occurred in async function" }
    }


    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    class ExceptionHandler : WebExceptionHandler {
        override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
            when (ex) {
                is NoSuchElementException -> exchange.response.statusCode = HttpStatus.NOT_FOUND
                is UsernameNotFoundException -> exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                is BadCredentialsException -> exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                is AccessDeniedException -> exchange.response.statusCode = HttpStatus.FORBIDDEN
                else -> exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
            }
            return writeBodyJson(ex, exchange)
        }

        fun writeBodyJson(ex: Throwable, exchange: ServerWebExchange): Mono<Void> = with(ApiError(exchange.response.statusCode!!, exchange.request.uri.toString(), ex.message!!)) {
            logger.error { "Handled error: $this" }
            exchange.response.writeWith(
                    Jackson2JsonEncoder().encode(
                            Mono.just(this),
                            exchange.response.bufferFactory(),
                            ResolvableType.forInstance(this),
                            MediaType.APPLICATION_JSON_UTF8,
                            Hints.from(Hints.LOG_PREFIX_HINT, exchange.logPrefix))
            )
        }
    }

    @Component
    class CustomWebFilter : WebFilter {
        override fun filter(exchange: ServerWebExchange, chain: WebFilterChain) = if (exchange.request.uri.path == "/rpi-pool") {
            chain.filter(exchange.mutate().request(exchange.request.mutate().path("/rpi-pool/index.html").build()).build())
        } else chain.filter(exchange)
    }
}
