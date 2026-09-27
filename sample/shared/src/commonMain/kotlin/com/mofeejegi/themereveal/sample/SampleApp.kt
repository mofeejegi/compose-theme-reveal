package com.mofeejegi.themereveal.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mofeejegi.themereveal.ThemeRevealHost
import com.mofeejegi.themereveal.rememberRevealController
import kotlinx.coroutines.launch

/**
 * The sample: one [ThemeRevealHost] over the whole screen. Each button reveals the next
 * palette from its own centre, arriving with one of the library's six presets.
 */
@Composable
fun SampleApp() {
    val controller = rememberRevealController(Palettes.first())
    val scope = rememberCoroutineScope()
    // The reveal's origin is in host coordinates; the buttons report theirs in the root.
    var hostInRoot by remember { mutableStateOf(Offset.Zero) }

    ThemeRevealHost(
        controller = controller,
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { hostInRoot = it.positionInRoot() },
    ) { palette ->
        // Composed once per world while a reveal runs: a function of the palette alone.
        PaletteTheme(palette) {
            SampleScreen(
                palette = palette,
                onArrival = { arrival, centerInRoot ->
                    val next = Palettes.after(controller.current)
                    scope.launch {
                        controller.revealTo(
                            target = next,
                            origin = centerInRoot - hostInRoot,
                            style = arrival.style(next),
                        )
                    }
                },
            )
        }
    }
}

/** Seats [palette] as the Material theme and paints its background. */
@Composable
private fun PaletteTheme(palette: Palette, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette.colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}

@Composable
private fun SampleScreen(
    palette: Palette,
    onArrival: (Arrival, centerInRoot: Offset) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "ThemeReveal", style = MaterialTheme.typography.displaySmall)
            Text(
                text = palette.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Each button reveals the next palette from where you tapped. " +
                    "Tap during a reveal to fast-forward it.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Arrival.entries.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pair.forEach { arrival ->
                        ArrivalButton(
                            arrival = arrival,
                            onArrival = onArrival,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // A lone last button keeps its half of the row.
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ArrivalButton(
    arrival: Arrival,
    onArrival: (Arrival, centerInRoot: Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    var centerInRoot by remember { mutableStateOf(Offset.Unspecified) }
    Button(
        onClick = { onArrival(arrival, centerInRoot) },
        modifier = modifier.onGloballyPositioned { centerInRoot = it.boundsInRoot().center },
    ) {
        Text(arrival.label)
    }
}
