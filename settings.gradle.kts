rootProject.name = "OtusHomework"
include("hw01-gradle")
include("hw03-generics")
include("hw06-annotations")
include("hw08-gc")
include("hw10-byteCode")
include("hw12-solid")
include("hw15-structuralPatterns")
include("hw16-io")
include("hw18-jdbc")
include("hw21-jpql")
include("hw22-cache")
include("hw25-di")
include("hw31-executors")
include("hw32-concurrentCollections")
include("hw34-multiprocess")


pluginManagement {
    val jgitver: String by settings
    val dependencyManagement: String by settings
    val springframeworkBoot: String by settings
    val johnrengelmanShadow: String by settings
    val jib: String by settings
    val protobufVer: String by settings
    val sonarlint: String by settings
    val spotless: String by settings

    plugins {
        id("fr.brouillard.oss.gradle.jgitver") version jgitver
        id("io.spring.dependency-management") version dependencyManagement
        id("org.springframework.boot") version springframeworkBoot
        id("com.github.johnrengelman.shadow") version johnrengelmanShadow
        id("com.google.cloud.tools.jib") version jib
        id("com.google.protobuf") version protobufVer
        id("name.remal.sonarlint") version sonarlint
        id("com.diffplug.spotless") version spotless
    }
}
