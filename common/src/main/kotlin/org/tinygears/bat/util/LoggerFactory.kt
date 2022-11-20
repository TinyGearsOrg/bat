/*
 *  Copyright (c) 2020-2022 Thomas Neidhart.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tinygears.bat.util

object LoggerFactory {
    val loggingEnabled: Boolean
        get() = LoggingConfiguration.defaultLevel < LogLevel.OFF

    fun <T: Any> getLogger(clazz: Class<T>): Logger {
        return if (loggingEnabled) {
            ConsoleLogger(clazz.simpleName, LoggingConfiguration.level(clazz.name))
        } else {
            nopLogger()
        }
    }

    fun nopLogger(): Logger {
        return NopLogger
    }
}

private object NopLogger: Logger {
    override val isTraceEnabled: Boolean
        get() = false
    override val isDebugEnabled: Boolean
        get() = false
    override val isInfoEnabled: Boolean
        get() = false
    override val isWarnEnabled: Boolean
        get() = false
    override val isErrorEnabled: Boolean
        get() = false

    override fun trace(obj: Any) {}
    override fun trace(msg: String) {}
    override fun trace(msg: () -> String) {}

    override fun debug(obj: Any) {}
    override fun debug(msg: String) {}
    override fun debug(msg: () -> String) {}

    override fun info(obj: Any) {}
    override fun info(msg: String) {}
    override fun info (msg: () -> String) {}

    override fun warn(obj: Any) {}
    override fun warn(msg: String) {}
    override fun warn (msg: () -> String) {}

    override fun error(obj: Any) {}
    override fun error(msg: String) {}
    override fun error(msg: () -> String) {}
}

private class ConsoleLogger constructor(private val loggingClass: String, minimumLevel: LogLevel): Logger {

    override val isTraceEnabled = minimumLevel == LogLevel.TRACE
    override val isDebugEnabled = minimumLevel <= LogLevel.DEBUG
    override val isInfoEnabled  = minimumLevel <= LogLevel.INFO
    override val isWarnEnabled  = minimumLevel <= LogLevel.WARN
    override val isErrorEnabled = minimumLevel <= LogLevel.ERROR

    override fun trace(obj: Any) {
        if (isTraceEnabled) {
            log(LogLevel.TRACE, obj.toString())
        }
    }

    override fun trace(msg: String) {
        if (isTraceEnabled) {
            log(LogLevel.TRACE, msg)
        }
    }

    override fun trace(msg: () -> String) {
        if (isTraceEnabled) {
            log(LogLevel.TRACE, msg())
        }
    }

    override fun debug(obj: Any) {
        if (isDebugEnabled) {
            log(LogLevel.DEBUG, obj.toString())
        }
    }

    override fun debug(msg: String) {
        if (isDebugEnabled) {
            log(LogLevel.DEBUG, msg)
        }
    }

    override fun debug(msg: () -> String) {
        if (isDebugEnabled) {
            log(LogLevel.DEBUG, msg())
        }
    }

    override fun info(obj: Any) {
        if (isInfoEnabled) {
            log(LogLevel.INFO, obj.toString())
        }
    }

    override fun info(msg: String) {
        if (isInfoEnabled) {
            log(LogLevel.INFO, msg)
        }
    }

    override fun info(msg: () -> String) {
        if (isInfoEnabled) {
            log(LogLevel.INFO, msg())
        }
    }

    override fun warn(obj: Any) {
        if (isWarnEnabled) {
            log(LogLevel.WARN, obj.toString())
        }
    }

    override fun warn(msg: String) {
        if (isWarnEnabled) {
            log(LogLevel.WARN, msg)
        }
    }

    override fun warn(msg: () -> String) {
        if (isWarnEnabled) {
            log(LogLevel.WARN, msg())
        }
    }

    override fun error(obj: Any) {
        if (isErrorEnabled) {
            log(LogLevel.ERROR, obj.toString())
        }
    }

    override fun error(msg: String) {
        if (isErrorEnabled) {
            log(LogLevel.ERROR, msg)
        }
    }

    override fun error(msg: () -> String) {
        if (isErrorEnabled) {
            log(LogLevel.ERROR, msg())
        }
    }

    private fun log(level: LogLevel, msg: String) {
        when (level) {
            LogLevel.TRACE,
            LogLevel.DEBUG,
            LogLevel.INFO -> println("${loggingClass}: $msg")

            LogLevel.WARN,
            LogLevel.ERROR -> System.err.println("${loggingClass}: $msg")

            else -> error("unexpected log level")
        }
    }
}