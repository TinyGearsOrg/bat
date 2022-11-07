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
import org.tinygears.bat.classfile.Member
import org.tinygears.bat.classfile.Method
import org.tinygears.bat.util.simpleNameMatcher

fun filterMembersByName(nameExpression: String, visitor: MemberVisitor): MemberVisitor {
    val nameMatcher = simpleNameMatcher(nameExpression)
    return MemberVisitor { classFile, index, member ->
        if (nameMatcher.matches(member.getName(classFile))) {
            member.accept(classFile, index, visitor)
        }
    }
}

fun interface MemberVisitor: FieldVisitor, MethodVisitor {
    fun visitAnyMember(classFile: ClassFile, index: Int, member: Member)

    override fun visitField(classFile: ClassFile, index: Int, field: Field) {
        visitAnyMember(classFile, index, field)
    }

    override fun visitMethod(classFile: ClassFile, index: Int, method: Method) {
        visitAnyMember(classFile, index, method)
    }
}
