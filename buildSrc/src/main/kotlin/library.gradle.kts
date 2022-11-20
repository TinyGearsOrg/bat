import gradle.kotlin.dsl.accessors._1482f3bc90e008ec156be50c0d0dc3ac.publishing
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `java-library`
    id("module")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
            groupId    = project.group.toString()
            artifactId = project.name
            version    = project.version.toString()
        }
    }

    repositories {
        mavenLocal()
    }
}
