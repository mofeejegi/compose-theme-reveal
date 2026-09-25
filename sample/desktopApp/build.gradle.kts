// The sample's desktop entry point. The UI itself lives in :sample:shared.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.mofeejegi.themereveal.sample.MainKt"
    }
}
