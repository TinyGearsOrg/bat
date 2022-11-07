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
import org.tinygears.bat.classfile.Method
import org.tinygears.bat.classfile.attribute.AttributeType
import org.tinygears.bat.classfile.attribute.CodeAttribute
import org.tinygears.bat.classfile.attribute.visitor.MethodAttributeVisitor
import org.tinygears.bat.util.simpleNameMatcher
import org.tinygears.bat.visitor.AbstractCollector

fun filterMethodsByName(nameExpression: String, visitor: MethodVisitor): MethodVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return MethodVisitor { classFile, index, method ->
        if (nameMatcher.matches(method.getName(classFile))) {
            method.accept(classFile, index, visitor)
        }
    }
}

fun filterMethodsByNameAndDescriptor(nameExpression: String, descriptor: String, visitor: MethodVisitor): MethodVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return MethodVisitor { classFile, index, method ->
        if (nameMatcher.matches(method.getName(classFile)) &&
            descriptor == method.getDescriptor(classFile)) {
            method.accept(classFile, index, visitor)
        }
    }
}

fun allCode(visitor: MethodAttributeVisitor): MethodVisitor {
    return MethodVisitor { classFile, _, method ->
        method.attributeMap.get<CodeAttribute>(AttributeType.CODE)?.accept(classFile, method, visitor)
    }
}

fun methodCollector(): MethodCollector {
    return MethodCollector()
}

fun interface MethodVisitor {
    fun visitMethod(classFile: ClassFile, index: Int, method: Method)
}

class MethodCollector: AbstractCollector<Method>(), MethodVisitor {
    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        addItem(method)
    }
}
