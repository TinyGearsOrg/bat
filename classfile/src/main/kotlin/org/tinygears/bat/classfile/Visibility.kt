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
package org.tinygears.bat.classfile

import org.tinygears.bat.util.toHexStringWithPrefix

/**
 * An enum to represent the mutually exclusive visibility flag.
 */
enum class Visibility(val flagValue: Int) {
    PRIVATE        (ACC_PRIVATE),
    PACKAGE_PRIVATE(0x0),
    PROTECTED      (ACC_PROTECTED),
    PUBLIC         (ACC_PUBLIC);

    companion object {
        fun of(accessFlags: Int): Visibility {
            val maskedValue = accessFlags and 0x7
            for (visibility in values()) {
                if (maskedValue == visibility.flagValue) {
                    return visibility
                }
            }
            throw IllegalArgumentException("unexpected accessFlags ${toHexStringWithPrefix(accessFlags)}")
        }
    }
}