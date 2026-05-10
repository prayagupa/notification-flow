import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":notification-contracts"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation(libs.pulsar.client)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.wiremock:wiremock-standalone:3.12.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(
            JavaLanguageVersion.of(
                providers.gradleProperty("jdk.language.version").getOrElse("25").toInt(),
            ),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.register<JavaExec>("replayDlq") {
    group = "delivery"
    description = "Read up to N messages from the DLQ topic and republish them to the dispatch topic (same protobuf payload and key)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.pratyabhi.notification.delivery.tools.ReplayDlqMain")
    if (project.hasProperty("dlqLimit")) {
        args((project.property("dlqLimit") as String).toInt().toString())
    } else {
        args("50")
    }
}
