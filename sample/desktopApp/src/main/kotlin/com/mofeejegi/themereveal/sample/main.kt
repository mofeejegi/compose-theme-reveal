package com.mofeejegi.themereveal.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ThemeReveal Sample",
    ) {
        SampleApp()
    }
}
