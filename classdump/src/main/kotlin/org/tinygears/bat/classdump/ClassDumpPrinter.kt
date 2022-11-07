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

package org.tinygears.bat.classdump

import org.tinygears.bat.classfile.ClassFile
import org.tinygears.bat.classfile.io.ClassFileReader
import org.tinygears.bat.io.DataEntry
import org.tinygears.bat.io.DataEntryReader
import org.tinygears.bat.io.DataEntryWriter
import org.tinygears.bat.io.IndentingPrinter
import java.io.BufferedInputStream
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClassDumpPrinter constructor(private val writer:      DataEntryWriter,
                                   private val printHeader: Boolean = true): DataEntryReader {
    override fun read(entry: DataEntry) {
        BufferedInputStream(entry.getInputStream()).use { `is` ->
            `is`.mark(entry.size.toInt())

            val classFile = ClassFile.empty()
            val reader    = ClassFileReader(`is`)
            reader.visitClassFile(classFile)

            val os      = writer.createOutputStream(entry)
            val printer = IndentingPrinter(OutputStreamWriter(os), 2)

            printer.use {
                if (printHeader) {
                    printer.println("Classfile ${entry.fullName}")
                    printer.levelUp()

                    val formatter = DateTimeFormatter.ofPattern("LLL d, yyyy").withZone(ZoneId.systemDefault())
                    val lastModified = entry.lastModifiedTime.toInstant()
                    val fileSize     = entry.size
                    printer.println("Last modified ${formatter.format(lastModified)}; size $fileSize bytes")

                    val sha256Checksum = MessageDigest.getInstance("SHA-256")!!
                    `is`.reset()
                    val checksum = sha256Checksum.digest(`is`.readBytes()).joinToString(separator = "") { "%02x".format(it) }
                    printer.println("SHA-256 checksum $checksum")

                    val sourceFile = classFile.sourceFile
                    if (sourceFile != null) {
                        printer.println("Compiled from \"$sourceFile\"")
                    }

                    printer.levelDown()
                    printer.flush()
                }

                classFile.accept(ClassFilePrinter(os))
            }
        }
    }
}