plugins {
    id("library")
    id("antlr")
}

base {
    archivesName.set("bat-jasm")
}

tasks {
    compileKotlin {
        dependsOn(generateGrammarSource)
    }

    generateGrammarSource {
        maxHeapSize = "64m"
        arguments = arguments + listOf("-package", "org.tinygears.bat.jasm.parser", "-visitor")
        outputDirectory = File("${project.buildDir}/generated-src/antlr/main/org/tinygears/bat/jasm/parser")
    }
}

configurations[JavaPlugin.API_CONFIGURATION_NAME].let { apiConfiguration ->
  apiConfiguration.setExtendsFrom(apiConfiguration.extendsFrom.filter { it.name != "antlr" })
}

dependencies {
    implementation(projects.common)
    implementation(projects.classfile)

    // antlr
    antlr(libs.antlr)
    implementation(libs.antlr.runtime)

    testImplementation(libs.jupiter.params)
}
