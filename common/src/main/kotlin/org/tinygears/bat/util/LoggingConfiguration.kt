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

import java.nio.file.Paths
import java.util.Properties
import kotlin.io.path.exists
import kotlin.io.path.inputStream

object LoggingConfiguration {
    private const val CONFIG_FILE = "bat-logging.properties"

    private const val LEVEL_KEY             = "level"
    private const val FILTER_CLASSNAME_KEY  = "filter.classname"
    private const val FILTER_METHODNAME_KEY = "filter.methodname"

    private val properties: Properties by lazy {
        val properties = Properties()

        val configFile = Paths.get(CONFIG_FILE)
        if (configFile.exists()) {
            configFile.inputStream().use { properties.load(it) }
        }

        properties
    }

    private val levelMatchers: Map<StringMatcher, LogLevel> by lazy {
        val map = mutableMapOf<StringMatcher, LogLevel>()

        for ((key, value) in properties.entries) {
            val keyString = key as String
            if (keyString.startsWith("logger.")) {
                val classNamePattern = keyString.removePrefix("logger.") + "**"
                map[classNameMatcher(classNamePattern)] = LogLevel.valueOf(value as String)
            }
        }
        map
    }

    private val levelPerClass: MutableMap<String, LogLevel> = mutableMapOf()

    val defaultLevel: LogLevel
        get() {
            val levelString = properties.getProperty(LEVEL_KEY, LogLevel.OFF.toString())
            return LogLevel.valueOf(levelString)
        }

    fun level(className: String): LogLevel {
        return levelPerClass.computeIfAbsent(className) { key ->
            var classLevel = defaultLevel
            for ((matcher, level) in levelMatchers) {
                if (matcher.matches(key)) {
                    classLevel = level
                }
            }

            classLevel
        }
    }

    val classNameFilter: String
        get() = properties.getProperty(FILTER_CLASSNAME_KEY, "**")

    val methodNameFilter: String
        get() = properties.getProperty(FILTER_METHODNAME_KEY, "**")
}