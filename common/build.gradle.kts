plugins {
    id("library")
}

base {
    archivesName.set("bat-core")
}

dependencies {
    api(libs.kotlinx.coroutines)

    testImplementation(libs.jupiter.params)
}
