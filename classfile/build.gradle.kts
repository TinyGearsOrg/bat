plugins {
    id("library")
}

base {
    archivesName.set("bat-classfile")
}

dependencies {
    implementation(projects.common)
    testImplementation(kotlin("test"))
}
