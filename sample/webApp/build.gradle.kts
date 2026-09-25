import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// The sample's web (Wasm) entry point. The UI itself lives in :sample:shared.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":sample:shared"))
            implementation(libs.compose.ui)
        }
    }
}
