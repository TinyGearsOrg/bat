plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        // enable invokedynamic generation for lambdas (-Xlambdas=indy)
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all", "-Xlambdas=indy")
        jvmTarget = "1.8"
    }
}
