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
    fun trace(obj: Object)
    fun trace(msg: String)
    fun trace(msg: () -> String)

    fun debug(obj: Object)
    fun debug(msg: String)
    fun debug(msg: () -> String)

    fun info(obj: Object)
    fun info(msg: String)
    fun info(msg: () -> String)

    fun warn(obj: Object)
    fun warn(msg: String)
    fun warn(msg: () -> String)

    fun error(obj: Object)
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