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

package org.tinygears.bat.smali.disassemble

import org.tinygears.bat.dexfile.ClassDef
import org.tinygears.bat.dexfile.DexFile
import org.tinygears.bat.dexfile.annotation.Annotation
import org.tinygears.bat.dexfile.annotation.AnnotationSet
import org.tinygears.bat.dexfile.annotation.FieldAnnotation
import org.tinygears.bat.dexfile.annotation.MethodAnnotation
import org.tinygears.bat.dexfile.annotation.ParameterAnnotation
import org.tinygears.bat.dexfile.annotation.visitor.AnnotationSetVisitor
import org.tinygears.bat.dexfile.annotation.visitor.AnnotationVisitor
import org.tinygears.bat.dexfile.value.AnnotationElement
import org.tinygears.bat.dexfile.value.visitor.AnnotationElementVisitor
import org.tinygears.bat.io.IndentingPrinter

internal class AnnotationPrinter(private val printer:      IndentingPrinter,
                                 var printParameterInfo:   Boolean = false,
                                 var currentRegisterIndex: Int     = 0,
                                 var currentParameterType: String? = null) : AnnotationSetVisitor, AnnotationVisitor, AnnotationElementVisitor {

    override fun visitAnyAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {}

    override fun visitClassAnnotationSet(dexFile: DexFile, classDef: ClassDef, annotationSet: AnnotationSet) {
        if (!annotationSet.isEmpty) {
            printer.println()
            printer.println()
            printer.println("# annotations")
            annotationSet.accept(dexFile, this.joinedByAnnotationConsumer { _, _ -> printer.println() })
        }
    }

    override fun visitFieldAnnotationSet(dexFile: DexFile, classDef: ClassDef, fieldAnnotation: FieldAnnotation, annotationSet: AnnotationSet) {
        if (!annotationSet.isEmpty) {
            printer.levelUp()
            annotationSet.accept(dexFile, this.joinedByAnnotationConsumer { _, _ -> printer.println() })
            printer.levelDown()
            printer.println(".end field")
        }
    }

    override fun visitMethodAnnotationSet(dexFile: DexFile, classDef: ClassDef, methodAnnotation: MethodAnnotation, annotationSet: AnnotationSet) {
        annotationSet.accept(dexFile, this.joinedByAnnotationConsumer { _, _ -> printer.println() } )
    }

    override fun visitParameterAnnotationSet(dexFile: DexFile, classDef: ClassDef, parameterAnnotation: ParameterAnnotation, parameterIndex: Int, annotationSet: AnnotationSet) {
        if (!annotationSet.isEmpty) {
            if (printParameterInfo) {
                printer.println(".param p%d    # %s".format(currentRegisterIndex, currentParameterType))
            }
            printer.levelUp()
            annotationSet.accept(dexFile, this.joinedByAnnotationConsumer { _, _ -> printer.println() })
            printer.levelDown()
            printer.println(".end param")
        }
    }

    override fun visitAnnotation(dexFile: DexFile, annotation: Annotation) {
        printer.print(".annotation " + annotation.visibility.simpleName + " ")
        val annotationValue = annotation.annotationValue
        printer.println(annotationValue.getType(dexFile))
        printer.levelUp()
        annotationValue.annotationElementsAccept(dexFile, this)
        printer.levelDown()
        printer.println(".end annotation")
    }

    override fun visitAnnotationElement(dexFile: DexFile, element: AnnotationElement) {
        printer.print(element.getName(dexFile))
        printer.print(" = ")
        element.value.accept(dexFile, EncodedValuePrinter(printer, this))
        printer.println()
    }
}