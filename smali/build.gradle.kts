plugins {
    id("library")
    id("antlr")
}

base {
    archivesName.set("bat-smali")
}

tasks {
    compileKotlin {
        dependsOn(generateGrammarSource)
    }

    generateGrammarSource {
        maxHeapSize = "64m"
        arguments = arguments + listOf("-package", "org.tinygears.bat.smali.parser", "-visitor")
        outputDirectory = File("${project.buildDir}/generated-src/antlr/main/org/tinygears/bat/smali/parser")
    }
}

tasks.test {
    // propagate the ANDROID_RUNTIMES property to test execution
    systemProperty("ANDROID_RUNTIMES", System.getProperty("ANDROID_RUNTIMES"))
}

configurations[JavaPlugin.API_CONFIGURATION_NAME].let { apiConfiguration ->
  apiConfiguration.setExtendsFrom(apiConfiguration.extendsFrom.filter { it.name != "antlr" })
}

dependencies {
    implementation(projects.common)
    implementation(projects.dexfile)

    // antlr
    antlr(libs.antlr)
    implementation(libs.antlr.runtime)

    testImplementation(libs.jupiter.params)
}
