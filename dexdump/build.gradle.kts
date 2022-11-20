plugins {
    id("module")
}

base {
    archivesName.set("bat-dexdump")
}

dependencies {
    implementation(projects.common)
    implementation(projects.dexfile)

    testImplementation(kotlin("test"))
    testImplementation(libs.jupiter.params)
}
