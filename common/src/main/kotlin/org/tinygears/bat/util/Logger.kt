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

interface Logger {
    val isTraceEnabled: Boolean
    val isDebugEnabled: Boolean
    val isInfoEnabled:  Boolean
    val isWarnEnabled:  Boolean
    val isErrorEnabled: Boolean

    fun trace(obj: Any)
    fun trace(msg: String)
    fun trace(msg: () -> String)

    fun debug(obj: Any)
    fun debug(msg: String)
    fun debug(msg: () -> String)

    fun info(obj: Any)
    fun info(msg: String)
    fun info(msg: () -> String)

    fun warn(obj: Any)
    fun warn(msg: String)
    fun warn(msg: () -> String)

    fun error(obj: Any)
    fun error(msg: String)
    fun error(msg: () -> String)
}

enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF
}