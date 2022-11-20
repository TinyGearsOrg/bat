plugins {
    id("library")
}

base {
    archivesName.set("bat-classfile")
}

tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}

dependencies {
    implementation(projects.common)
    testImplementation(kotlin("test"))
}
