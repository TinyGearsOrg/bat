plugins {
    id("library")
}

base {
    archivesName.set("bat-core")
}

dependencies {
    api(libs.kotlinx.coroutines)

    testImplementation(kotlin("test"))
    testImplementation(libs.jupiter.params)
}
