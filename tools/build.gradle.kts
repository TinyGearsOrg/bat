plugins {
    id("module")
    distribution
}

base {
    archivesName.set("bat-commands")
}

distributions {
    main {
        distributionBaseName.set("bat-tools")
        contents {
            into("lib") {
                from(tasks["jar"])
                from(configurations.runtimeClasspath)
            }
            into("") {
                from("${projectDir}/scripts")
                eachFile {
                    fileMode = 0b111101101
                }
            }
        }
    }
}

dependencies {
    implementation(libs.picocli)

    implementation(projects.common)
    implementation(projects.classfile)
    implementation(projects.dexfile)
    implementation(projects.smali)
    implementation(projects.jasm)
    implementation(projects.dexdump)
    implementation(projects.classdump)

    testImplementation(kotlin("test"))
    testImplementation(libs.jupiter.params)
}
