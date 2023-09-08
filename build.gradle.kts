import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("multiplatform") version "1.9.0"
	kotlin("plugin.serialization") version "1.9.0"
	id("maven-publish")
//    id("dev.petuska.npm.publish") version "3.4.1"

}

group = "com.withertech.yasdpl"
version = "2.0.0"

repositories {
    mavenCentral()
}
//
//tasks.withType<KotlinNativeCompile>().configureEach {
//	kotlinOptions {
//		freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
//	}
//}



kotlin {
    js("frontend", IR) {
        binaries.library()
	    generateTypeScriptDefinitions()
        useCommonJs()
        browser {
            commonWebpackConfig(Action {
				export = true
                sourceMaps = true
                cssSupport {
                    enabled.set(true)
                }
            })
	        testTask(Action {
                useKarma {
                    useFirefox()
                }
            })
        }
    }
    linuxX64("backend") {
        binaries {
			staticLib("yasdpl")
        }
    }
    sourceSets {
		val ktor_version = "2.3.2"
	    val commonMain by getting {
			dependencies {
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
				implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("io.github.oshai:kotlin-logging:5.1.0")
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
                implementation("io.ktor:ktor-client-websockets:$ktor_version")
                implementation("io.ktor:ktor-client-js:$ktor_version")
                implementation(npm("decky-frontend-lib", "^3.21.0"))

            }
	    }
	    val frontendTest by getting
	    val backendMain by getting {
			dependencies {
//                implementation(kotlin("stdlib-common"))
				implementation("io.ktor:ktor-server-core:$ktor_version")
				implementation("io.ktor:ktor-server-websockets:$ktor_version")
				implementation("io.ktor:ktor-server-cio:$ktor_version")
				implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-client-cio:$ktor_version")
                implementation("io.ktor:ktor-server-cors:$ktor_version")
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
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/EmuDeck/yasdpl-kt")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
	}
}

//val secrets = Properties().apply {
//    load(FileInputStream(File(rootProject.rootDir, "secrets.properties")))
//}
//
//npmPublish {
//    registries {
//        register("npmjs") {
//            uri.set("https://registry.npmjs.org")
//            authToken.set(secrets.getProperty("NPM_TOKEN"))
//        }
//    }
//}