val groovyVersion: String by project
val spockVersion: String by project
val okHttpVersion: String by project
val apacheLangVersion: String by project

plugins {
    groovy
    idea
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.flywaydb.flyway") version "6.5.5"
}

group = "com.altruist"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.withType(Test::class.java).all {
    this.include("**/*Test.*")
    this.exclude("**/*TestBase.*", "**/*IntegrationTest.*")
}

tasks {
    bootJar {
        archiveFileName.set(project.name + ".jar")
    }
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    wrapper {
        gradleVersion = "6.4.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("com.opentable.components:otj-pg-embedded:0.13.0")

    implementation("org.springdoc:springdoc-openapi-ui:1.5.2")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.codehaus.groovy:groovy-all:$groovyVersion")
    testImplementation("org.spockframework:spock-core:$spockVersion")
    testImplementation("org.spockframework:spock-spring:$spockVersion")
}
