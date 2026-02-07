plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("com.google.devtools.ksp") version "2.3.5"
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.bigboss"
version = "0.0.1-SNAPSHOT"
description = "millkbot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.1.2"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // spring
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // database
    implementation("org.postgresql:postgresql")
    ksp("org.babyfish.jimmer:jimmer-ksp:0.10.6")
    implementation("org.babyfish.jimmer:jimmer-spring-boot-starter:0.10.6")

    // quartz
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    // milky
    implementation("org.ntqqrev:milky-kt-sdk:1.1.0")
    implementation("io.ktor:ktor-client-cio:3.4.0")

}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
