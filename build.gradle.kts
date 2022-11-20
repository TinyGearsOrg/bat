plugins {
    id("base")
    id("idea")
    id("net.researchgate.release") version "3.0.2"
}

allprojects {
    group = "com.tinygears.bat"
    version = property("version") ?: "undefined"

    repositories {
        mavenCentral()
    }
}
