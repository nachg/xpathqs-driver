/*
 * Copyright (c) 2021 Nikita A. Chegodaev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1.8"

plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jetbrains.dokka") version "1.4.32"
    `java-library`
    jacoco
 //   maven
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

jacoco {
    toolVersion = "0.8.7"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.xpathqs:core:0.1.6")
    api("org.xpathqs:log:0.1.6")
    api("org.xpathqs:prop:0.2.3")
    api("org.xpathqs:gwt:0.2.6")

    api("org.jgrapht:jgrapht-core:1.5.1")
    implementation ("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
    implementation(kotlin("stdlib"))

    implementation("org.apache.commons:commons-lang3:3.12.0")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")

    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23.1")
}

publishing {
    publications {
        beforeEvaluate {
            signing.sign(this@publications)
        }
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("XpathQS Driver")
                description.set("Framework for the interaction with drivers for the xpathqs-core")
                url.set("https://xpathqs.org/")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("nachg")
                        name.set("Nikita A. Chegodaev")
                        email.set("nikchgd@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/xpathqs/driver.git")
                    developerConnection.set("scm:git:ssh://github.com/xpathqs/driver.git")
                    url.set("https://xpathqs.org/")
                }
            }
            groupId = "org.xpathqs"
            artifactId = "driver"

            from(components["java"])
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = project.property("ossrhUsername").toString()
                password = project.property("ossrhPassword").toString()
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        )
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = false
        csv.isEnabled = true
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            samples.from("src/test/kotlin/org/xpathqs/driver", "src/main/kotlin/org/xpathqs/driver")
        }
    }
}