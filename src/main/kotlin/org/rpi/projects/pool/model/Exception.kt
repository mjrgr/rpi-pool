package org.rpi.projects.pool.model

import org.springframework.http.HttpStatus

data class ApiError(val status: HttpStatus, val uri: String, val msg: String)

class RpiException(msg: String, t: Throwable? = null) : Exception(msg, t)

class RpiRuntimeException(msg: String, t: Throwable? = null) : RuntimeException(msg, t)