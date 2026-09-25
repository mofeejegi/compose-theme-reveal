# Compose Theme Reveal

[![Kotlin](https://img.shields.io/badge/kotlin-2.4.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.12.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![Author](https://img.shields.io/badge/author-mofeejegi-gray.svg?logo=github)](https://github.com/mofeejegi)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Maven Central](https://img.shields.io/maven-central/v/com.mofeejegi.themereveal/theme-reveal-compose/0.1.0-alpha01)](https://central.sonatype.com/artifact/com.mofeejegi.themereveal/theme-reveal-compose/0.1.0-alpha01)

A Compose Multiplatform library that reveals a new theme *through* the old one. The incoming world is composed alongside the current one and uncovered behind an animated mask: an expanding front or a parting slit, with its own edge, glow, wobble and shake. Each theme can arrive with its own personality.

Runs on **Android**, **iOS**, **Desktop (JVM)** and **Web (Wasm)**.

<img src="banner.png" alt="Compose Theme Reveal">

## How it works

- `ThemeRevealHost` composes your content once with the committed theme and, only while a reveal runs, a second time with the incoming theme, clipped to the animated mask. Both worlds stay live; nothing is snapshotted.
- `RevealController` owns the theme value. `revealTo` suspends until the reveal reaches full coverage, and only then commits the new value.
- The library is generic over the theme type: a Material `ColorScheme`, your own tokens, an enum. It depends only on Compose runtime, foundation and ui.

## Installation

```kotlin
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mofeejegi.themereveal:theme-reveal-compose:0.1.0-alpha01")
        }
    }
}
```

## Usage

```kotlin
@Composable
fun App() {
    val controller = rememberRevealController(LightColors)
    val scope = rememberCoroutineScope()

    ThemeRevealHost(controller = controller, modifier = Modifier.fillMaxSize()) { colors ->
        // Composed twice while a reveal runs: once per world.
        MaterialTheme(colorScheme = colors) {
            Screen(
                onToggleTheme = { tapPosition ->
                    scope.launch {
                        controller.revealTo(
                            target = if (controller.current == LightColors) DarkColors else LightColors,
                            origin = tapPosition, // host coordinates; omit to reveal from the centre
                            style = ArrivalStyle.iris(accent = Color(0xFF6FA8FF)),
                        )
                    }
                },
            )
        }
    }
}
```

## Arrival styles

Six presets ship with the library, each an extension on `ArrivalStyle.Companion`:

| Preset | Character |
|---|---|
| `ArrivalStyle.iris(accent)` | A clean luminous ring with a fine shimmer and a trace of shake |
| `ArrivalStyle.cut(accent)` | Two dead-straight fronts parting from the origin |
| `ArrivalStyle.bloom(accent)` | No front line: a soft halo over counter-rotating petal lobes |
| `ArrivalStyle.rift(accent)` | A broad living wobble, a glowing front and a slight world-shake |
| `ArrivalStyle.chaos(accent)` | Jagged shards, a thick blast line and the hardest shake in the set |
| `ArrivalStyle.paperBurn(ember, char)` | A crackling ember line with a charred rim behind it |

Or build your own:

```kotlin
val style = ArrivalStyle(
    front = FrontShape.Radial,              // or FrontShape.Slit
    edge = EdgeCharacter(
        wobble = listOf(WobbleBand(amplitude = 8.dp, frequency = 7f, speed = 3f)),
        strokeWidth = 2.dp,
        glowWidth = 16.dp,
    ),
    accent = Color(0xFFFF6B3D),
    shake = 2.dp,
    duration = 900,
    envelope = Envelope(rampIn = 0.3f, rampOut = 0.7f),
)
```

## Input

- `InputPolicy.Block` (the default) consumes all input inside the host until the reveal completes. A tap fast-forwards the reveal; it never cancels or rewinds it.
- `InputPolicy.Allow` leaves input alone, for hosts whose interaction lives outside the revealed content (a pager page revealing after it settles, say).

## Things to know

- State remembered inside the host's `content` lambda exists once per world while a reveal runs, so it can drift apart. Hoist anything animated or interactive above the host.
- Calls to `revealTo` made while a reveal is running coalesce: the running reveal finishes, and only the newest request runs after it.
- A running reveal does not recompose anything per frame. Its clocks are read only in draw and placement, and the frame loop exists only inside `revealTo`.

## Sample

The sample shows all six presets, stepping through six palettes. The UI lives in `sample/shared`; each platform has its own entry point:

- **Android:** `./gradlew :sample:androidApp:installDebug`
- **Desktop:** `./gradlew :sample:desktopApp:run`
- **Web:** `./gradlew :sample:webApp:wasmJsBrowserDevelopmentRun`
- **iOS:** open `sample/iosApp/iosApp.xcodeproj` in Xcode and run.

## License

This project is licensed under the [Apache License 2.0](./LICENSE).
