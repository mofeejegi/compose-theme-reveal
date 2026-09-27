import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// ThemeReveal — dual-composition clip-reveal theme transitions. Depends only on
// Compose runtime/foundation/ui — deliberately token-generic, no Material dependency.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.mofeejegi.themereveal"
version = "0.1.0-alpha01"

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    android {
        namespace = "com.mofeejegi.themereveal"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {
        }
    }
    iosArm64()
    iosSimulatorArm64()
    js {
        browser()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "theme-reveal-compose", version.toString())

    pom {
        name = "Compose Theme Reveal"
        description = "A Compose Multiplatform library that reveals a new theme through the old one, with a dual-composition clip mask."
        inceptionYear = "2026"
        url = "https://github.com/mofeejegi/compose-theme-reveal"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "mofeejegi"
                name = "Mofe Ejegi"
                url = "https://mofeejegi.com"
            }
        }
        scm {
            url = "https://github.com/mofeejegi/compose-theme-reveal"
            connection = "scm:git:git://github.com/mofeejegi/compose-theme-reveal.git"
            developerConnection = "scm:git:ssh://git@github.com/mofeejegi/compose-theme-reveal.git"
        }
    }
}
