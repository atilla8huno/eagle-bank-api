plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.eaglebank"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation("org.springframework.boot:spring-boot-h2console")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation(platform("io.cucumber:cucumber-bom:8.0.4"))
    testImplementation("io.cucumber:cucumber-java")
    testImplementation("io.cucumber:cucumber-spring")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("org.junit.platform:junit-platform-suite")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}

tasks.test {
    description = "Runs unit tests."
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
    filter {
        excludeTestsMatching("com.eaglebank.EagleBankApiApplicationTest")
        excludeTestsMatching("com.eaglebank.ConcurrencyTransactionTest")
        excludeTestsMatching("*IntegrationTest")
        excludeTestsMatching("*ControllerTest")
        excludeTestsMatching("*RepositoryTest")
        excludeTestsMatching("*FeatureTest")
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
    filter {
        includeTestsMatching("com.eaglebank.EagleBankApiApplicationTest")
        includeTestsMatching("com.eaglebank.ConcurrencyTransactionTest")
        includeTestsMatching("*IntegrationTest")
        includeTestsMatching("*ControllerTest")
        includeTestsMatching("*RepositoryTest")
        excludeTestsMatching("*FeatureTest")
    }
}

/*
val acceptanceTest = tasks.register<Test>("acceptanceTest") {
	description = "Runs acceptance tests."
	group = "verification"
	testClassesDirs = sourceSets.test.get().output.classesDirs
	classpath = sourceSets.test.get().runtimeClasspath
	shouldRunAfter(integrationTest)
	useJUnitPlatform {
		includeEngines("junit-platform-suite")
	}
	filter {
		includeTestsMatching("*FeatureTest")
	}
}
*/

val jacocoExecFiles = files(
    layout.buildDirectory.file("jacoco/test.exec"),
    layout.buildDirectory.file("jacoco/integrationTest.exec"),
    layout.buildDirectory.file("jacoco/acceptanceTest.exec")
)

tasks.jacocoTestReport {
    dependsOn(tasks.test, integrationTest) //, acceptanceTest)
    executionData.setFrom(jacocoExecFiles)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
}

tasks.check {
    dependsOn(integrationTest, tasks.jacocoTestReport) //, acceptanceTest)
}
