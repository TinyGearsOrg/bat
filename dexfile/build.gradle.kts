plugins {
    id("library")
}

base {
    archivesName.set("bat-dexfile")
}

dependencies {
    implementation(projects.common)

    testImplementation(kotlin("test"))
}
