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

package org.tinygears.bat.classfile.visitor

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.Field
import org.tinygears.bat.util.simpleNameMatcher
import org.tinygears.bat.visitor.AbstractCollector

fun filterFieldsByName(nameExpression: String, visitor: FieldVisitor): FieldVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return FieldVisitor { classFile, index, field ->
        if (nameMatcher.matches(field.getName(classFile))) {
            field.accept(classFile, index, visitor)
        }
    }
}

fun fieldCollector(): FieldCollector {
    return FieldCollector()
}

fun interface FieldVisitor {
    fun visitField(classFile: ClassFile, index: Int, field: Field)
}

class FieldCollector: AbstractCollector<Field>(), FieldVisitor {
    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        addItem(field)
    }
}
