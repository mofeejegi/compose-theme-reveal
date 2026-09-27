package com.mofeejegi.themereveal.sample

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.mofeejegi.themereveal.ArrivalStyle
import com.mofeejegi.themereveal.styles.aperture
import com.mofeejegi.themereveal.styles.bloom
import com.mofeejegi.themereveal.styles.cascade
import com.mofeejegi.themereveal.styles.chaos
import com.mofeejegi.themereveal.styles.curtain
import com.mofeejegi.themereveal.styles.cut
import com.mofeejegi.themereveal.styles.flipboard
import com.mofeejegi.themereveal.styles.iris
import com.mofeejegi.themereveal.styles.paperBurn
import com.mofeejegi.themereveal.styles.rift
import com.mofeejegi.themereveal.styles.shutters
import com.mofeejegi.themereveal.styles.slash

/** One world the sample can reveal. ThemeReveal is generic over this type: use your own theme. */
@Immutable
data class Palette(
    val name: String,
    val dark: Boolean,
    val background: Color,
    val content: Color,
    val accent: Color,
    val onAccent: Color,
) {
    val colorScheme: ColorScheme =
        if (dark) {
            darkColorScheme(
                primary = accent,
                onPrimary = onAccent,
                background = background,
                onBackground = content,
                surface = background,
                onSurface = content,
            )
        } else {
            lightColorScheme(
                primary = accent,
                onPrimary = onAccent,
                background = background,
                onBackground = content,
                surface = background,
                onSurface = content,
            )
        }
}

/** The palettes the sample steps through, alternating light and dark so every reveal reads. */
val Palettes: List<Palette> = listOf(
    Palette("Paper", dark = false, background = Color(0xFFF4F1EA), content = Color(0xFF1F1B16), accent = Color(0xFF8C6E4A), onAccent = Color.White),
    Palette("Midnight", dark = true, background = Color(0xFF0F1A2B), content = Color(0xFFE6EDF7), accent = Color(0xFF6FA8FF), onAccent = Color(0xFF0B1320)),
    Palette("Mint", dark = false, background = Color(0xFFE3F4EC), content = Color(0xFF0F2E22), accent = Color(0xFF1F8A5B), onAccent = Color.White),
    Palette("Plum", dark = true, background = Color(0xFF24122E), content = Color(0xFFF3E8FA), accent = Color(0xFFC77DFF), onAccent = Color(0xFF1E0F27)),
    Palette("Citrus", dark = false, background = Color(0xFFFFF4D6), content = Color(0xFF3A2A00), accent = Color(0xFFC77700), onAccent = Color.White),
    Palette("Ember", dark = true, background = Color(0xFF1C0F0B), content = Color(0xFFFBE9E1), accent = Color(0xFFFF6B3D), onAccent = Color(0xFF1C0F0B)),
)

/** The palette after [palette], wrapping round. */
fun List<Palette>.after(palette: Palette): Palette = this[(indexOf(palette) + 1) % size]

/** The library's preset arrivals, each dressed in the incoming palette's accent. */
enum class Arrival(val label: String) {
    Iris("Iris"),
    Cut("Cut"),
    Bloom("Bloom"),
    Rift("Rift"),
    Chaos("Chaos"),
    PaperBurn("Paper burn"),
    Curtain("Curtain"),
    Slash("Slash"),
    Aperture("Aperture"),
    Flipboard("Flipboard"),
    Cascade("Cascade"),
    Shutters("Shutters");

    fun style(incoming: Palette): ArrivalStyle = when (this) {
        Iris -> ArrivalStyle.iris(incoming.accent)
        Cut -> ArrivalStyle.cut(incoming.accent)
        Bloom -> ArrivalStyle.bloom(incoming.accent)
        Rift -> ArrivalStyle.rift(incoming.accent)
        Chaos -> ArrivalStyle.chaos(incoming.accent)
        PaperBurn -> ArrivalStyle.paperBurn(ember = BurnEmber, char = BurnChar)
        Curtain -> ArrivalStyle.curtain(incoming.accent)
        Slash -> ArrivalStyle.slash(incoming.accent)
        Aperture -> ArrivalStyle.aperture(incoming.accent)
        Flipboard -> ArrivalStyle.flipboard(incoming.accent)
        Cascade -> ArrivalStyle.cascade(incoming.accent)
        Shutters -> ArrivalStyle.shutters(incoming.accent)
    }
}

private val BurnEmber = Color(0xFFE8873F)
private val BurnChar = Color(0xFF2B1B10)
