import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-library`
    alias(libs.plugins.protobuf)
}

dependencies {
    api(libs.protobuf.java)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.3"
    }
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

sourceSets {
    named("main") {
        proto {
            srcDir("proto")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
