import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar

val compileKotlin: KotlinCompile by tasks
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val arrow_version: String by project

plugins {
    application
    kotlin("jvm") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "net.bmgames"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://dl.bintray.com/arrow-kt/arrow-kt/") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}



dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.129-kotlin-1.4.20")
    implementation("io.ktor:ktor-server-netty:$ktor_version")

    implementation("com.google.code.gson:gson:2.8.6")

    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")

    //Serialization (JSON)
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("io.ktor:ktor-client-json:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")

    //Auth0
    implementation("com.auth0:mvc-auth-commons:1.+")

    //Utilities
    implementation("io.arrow-kt:arrow-optics:$arrow_version")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrow_version")
    implementation("io.arrow-kt:arrow-syntax:$arrow_version")
    kapt("io.arrow-kt:arrow-meta:$arrow_version")

    //Command parser
    implementation("com.xenomachina:kotlin-argparser:2.0.7")


}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "BM Games Prototyp"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "net.bmgames.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
