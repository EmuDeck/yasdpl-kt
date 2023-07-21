import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmNativeCompileTaskConfigurator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    kotlin("multiplatform") version "1.9.0"
	kotlin("plugin.serialization") version "1.9.0"
	id("maven-publish")

}

group = "usdpl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<KotlinNativeCompile>().configureEach {
	kotlinOptions {
		freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
	}
}



kotlin {
    js("frontend", IR) {
        binaries.library()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
	        testTask {
                useKarma {
                    useFirefox()
                }
            }
        }
    }
    linuxX64("backend") {
        binaries {
			staticLib("usdpl")
        }
    }
    sourceSets {
		val ktor_version = "2.3.2"
	    val commonMain by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

			}
	    }
	    val commonTest by getting {
		    dependencies {
			    implementation(kotlin("test"))
		    }
	    }
	    val frontendMain by getting {
		    dependencies {
			    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
			    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
			    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
		    }
	    }
	    val frontendTest by getting
	    val backendMain by getting {
			dependencies {
				implementation("io.ktor:ktor-server-core:$ktor_version")
				implementation("io.ktor:ktor-server-websockets:$ktor_version")
				implementation("io.ktor:ktor-server-cio:$ktor_version")
			}
	    }
	    val backendTest by getting
    }
}

publishing {
	repositories {
		maven {
			mavenLocal()
			url = uri(layout.buildDirectory.dir("repo"))

		}
	}
}