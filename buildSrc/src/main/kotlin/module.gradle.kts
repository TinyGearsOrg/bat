import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        // set options for log level LIFECYCLE
        events = setOf(FAILED, PASSED, SKIPPED, STANDARD_OUT)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true

        // set options for log level DEBUG and INFO
        debug {
            events = setOf(STARTED, FAILED, PASSED, SKIPPED, STANDARD_ERROR, STANDARD_OUT)
            exceptionFormat = TestExceptionFormat.FULL
        }
        info {
            events = debug.events
            exceptionFormat = debug.exceptionFormat
        }
    }

    addTestListener(object : TestListener {
        override fun beforeTest(desc: TestDescriptor) = Unit
        override fun beforeSuite(desc: TestDescriptor) = Unit
        override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
        override fun afterSuite(desc: TestDescriptor, result: TestResult) {
            printResults(desc, result)
        }
    })
}

fun printResults(desc: TestDescriptor, result: TestResult) {
    if (desc.parent == null) {
        val output = result.run {
            "Results: $resultType (" +
                    "$testCount tests, " +
                    "$successfulTestCount successes, " +
                    "$failedTestCount failures, " +
                    "$skippedTestCount skipped" +
                    ")"
        }
        val testResultLine = "|  $output  |"
        val repeatLength = testResultLine.length
        val separationLine = "-".repeat(repeatLength)
        println(separationLine)
        println(testResultLine)
        println(separationLine)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        // enable invokedynamic generation for lambdas (-Xlambdas=indy)
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all", "-Xlambdas=indy")
        jvmTarget = "1.8"
    }
}

dependencies {
    testImplementation(kotlin("test"))
}