import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
}

group = "net.square"
version = project.property("project.version")!!
description = "Visage"

repositories {
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.squarecode.de/repository/visage/")
    maven("https://repo.squarecode.de/repository/funkemunky/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.10")
    annotationProcessor("org.projectlombok:lombok:1.18.10")
    compileOnly("org.spigotmc:spigot:1.8.8")
    compileOnly("cc.funkemunky.plugins:Atlas:1.8.3.1")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    val outputDirectoryPath = System.getProperty("output.directory")
    if (outputDirectoryPath != null) {
        destinationDirectory.set(file(outputDirectoryPath))
    }
}

tasks.withType<ProcessResources> {
    this.filter<ReplaceTokens>(
        "tokens" to mapOf("project.version" to project.property("project.version")!!)
    )
}