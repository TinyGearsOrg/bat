plugins {
    id("module")
}

base {
    archivesName.set("bat-classdump")
}

dependencies {
    implementation(projects.common)
    implementation(projects.classfile)
}
