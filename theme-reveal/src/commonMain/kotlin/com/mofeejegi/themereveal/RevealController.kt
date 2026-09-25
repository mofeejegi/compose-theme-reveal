package com.mofeejegi.themereveal

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns one host's theme value and drives its reveals.
 *
 * State-flip contract: [current] commits to the target only at full coverage.
 * Until then the incoming world exists solely behind the animated mask.
 * Callers update their underlying state immediately and independently; the
 * controller governs only what is displayed.
 *
 * [revealTo] is mutex-guarded; calls made while a reveal is in flight coalesce
 * to the latest target — the in-flight reveal completes, queued stale requests
 * are dropped, and only the newest runs.
 */
@Stable
class RevealController<T>(initial: T) {

    /** The committed theme value — what a non-transitioning host displays. */
    var current: T by mutableStateOf(initial)
        private set

    /** The reveal target while a transition is in flight, else null. */
    var incoming: T? by mutableStateOf(null)
        private set

    val isRevealing: Boolean get() = incoming != null

    // The two clocks. Read these ONLY inside draw/layout lambdas — never in
    // composition — so a running reveal recomposes nothing.
    // Eased progress drives reveal geometry; raw time drives sinusoidal life
    // so organic motion doesn't decelerate with the ease.
    internal val progress = mutableFloatStateOf(0f)
    internal val timeSeconds = mutableFloatStateOf(0f)

    internal var origin: Offset = Offset.Unspecified
        private set
    internal var style: ArrivalStyle = ArrivalStyle()
        private set
    internal var inputPolicy: InputPolicy = InputPolicy.Block
        private set

    private val mutex = Mutex()
    private var latestRequest: Any? = null
    private var accelerated = false

    /**
     * Reveals [target] from [origin] (host coordinates; [Offset.Unspecified]
     * means the host's center). Suspends until the reveal commits. A no-op if
     * [target] equals the committed value.
     *
     * The frame loop exists only inside this call — an idle controller costs
     * nothing.
     */
    suspend fun revealTo(
        target: T,
        origin: Offset = Offset.Unspecified,
        style: ArrivalStyle = ArrivalStyle(),
        input: InputPolicy = InputPolicy.Block,
    ) {
        val request = Any()
        latestRequest = request
        mutex.withLock {
            if (latestRequest !== request) return // superseded while queued
            if (target == current) return
            this.origin = origin
            this.style = style
            this.inputPolicy = input
            accelerated = false
            progress.floatValue = 0f
            incoming = target
            try {
                var lastNanos = withFrameNanos { it }
                var fraction = 0f
                while (fraction < 1f) {
                    withFrameNanos { now ->
                        val dtMillis = (now - lastNanos) / 1_000_000f
                        lastNanos = now
                        timeSeconds.floatValue += dtMillis / 1000f
                        val pace = if (accelerated) ACCELERATED_MILLIS else style.duration.toFloat()
                        fraction = (fraction + dtMillis / pace).coerceAtMost(1f)
                        progress.floatValue = Ease.transform(fraction)
                    }
                }
            } finally {
                // Full coverage (or cancellation): commit and drop the second
                // composition. At coverage the mask is full-bleed, so swapping
                // the committed world for the incoming one is pixel-identical.
                current = target
                incoming = null
                progress.floatValue = 0f
            }
        }
    }

    /**
     * Sets the committed value with no transition. For hosts that reset when
     * off-screen (a carousel page re-arming its reveal for the next visit).
     */
    suspend fun snapTo(value: T) {
        latestRequest = null
        mutex.withLock {
            current = value
        }
    }

    /**
     * Fast-forwards an in-flight reveal to completion at accelerated pace.
     * Never cancels, never rewinds — the reveal ends at the same place, only
     * sooner. Called by the host on tap under [InputPolicy.Block]; also
     * callable directly.
     */
    fun accelerate() {
        accelerated = true
    }

    private companion object {
        const val ACCELERATED_MILLIS = 150f

        // Ease-in, linear-out: the front leaves the origin gently, then sweeps
        // at constant speed to full coverage. The second control point sits on
        // the diagonal, which makes the tail genuinely linear — the reveal
        // never decelerates into its own envelope ramp-out.
        val Ease = CubicBezierEasing(0.5f, 0f, 0.75f, 0.75f)
    }
}

/** Remembers a [RevealController] keyed to nothing — one per host site. */
@Composable
fun <T> rememberRevealController(initial: T): RevealController<T> =
    remember { RevealController(initial) }
