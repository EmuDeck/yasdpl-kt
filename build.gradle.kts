import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("multiplatform") version "1.9.0"
	kotlin("plugin.serialization") version "1.9.0"
	id("maven-publish")
//    id("dev.petuska.npm.publish") version "3.4.1"

}

group = "io.github.emudeck.yasdpl"
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
        val okioVersion = "3.4.0"
        val ktorVersion = "2.3.2"
	    val commonMain by getting {
			dependencies {
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")

                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
				api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                api("io.github.oshai:kotlin-logging:5.1.0")
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
                api("io.ktor:ktor-client-websockets:$ktorVersion")
                api("io.ktor:ktor-client-js:$ktorVersion")
//                implementation(npm("decky-frontend-lib", "^3.21.0"))

            }
	    }
	    val frontendTest by getting
	    val backendMain by getting {
			dependencies {
//                implementation(kotlin("stdlib-common"))
				implementation("io.ktor:ktor-server-core:$ktorVersion")
				implementation("io.ktor:ktor-server-websockets:$ktorVersion")
				implementation("io.ktor:ktor-server-cio:$ktorVersion")
				implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-server-cors:$ktorVersion")
                implementation("com.squareup.okio:okio:$okioVersion")
            }
	    }
	    val backendTest by getting
    }
}

val secrets = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "secrets.properties")))
}

publishing {
	repositories {
		maven {
			mavenLocal()
			url = uri(layout.buildDirectory.dir("repo"))
		}
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/EmuDeck/yasdpl-kt")
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
        maven {
            name = "Forgejo"
            url = uri("https://codeberg.org/api/packages/Witherking25/maven")

            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                value = "token ${secrets["FORGEJO_TOKEN"]}"
            }

            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
	}
}
//
//npmPublish {
//    registries {
//        register("npmjs") {
//            uri.set("https://registry.npmjs.org")
//            authToken.set(secrets.getProperty("NPM_TOKEN"))
//        }
//    }
//}