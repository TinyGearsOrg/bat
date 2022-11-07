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
package org.tinygears.bat.classfile.attribute.annotation.visitor

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.attribute.annotation.Annotation
import org.tinygears.bat.classfile.attribute.annotation.TypeAnnotation

fun interface AnnotationVisitorIndexed {
    fun visitAnyAnnotation(classFile: ClassFile, index: Int, annotation: Annotation)

    fun visitAnnotation(classFile: ClassFile, index: Int, annotation: Annotation) {
        visitAnyAnnotation(classFile, index, annotation)
    }

    fun visitTypeAnnotation(classFile: ClassFile, index: Int, typeAnnotation: TypeAnnotation) {
        visitAnyAnnotation(classFile, index, typeAnnotation)
    }
}
