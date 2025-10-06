@file:Suppress("unused")

package ahjd.geekedCraft.hologram.util

import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.pow
import kotlin.math.sin

/**
 * Animation system for temporary holograms
 */
object HologramAnimation {

    /**
     * Animate a hologram with customizable movement, scaling, and fading
     */
    fun animate(
        display: TextDisplay,
        plugin: Plugin,
        durationTicks: Int,
        config: AnimationConfig.() -> Unit
    ) {
        val animConfig = AnimationConfig().apply(config)
        val startLoc = display.location.clone()
        val startTransform = display.transformation
        val startScale = Vector(
            startTransform.scale.x,
            startTransform.scale.y,
            startTransform.scale.z
        )

        object : BukkitRunnable() {
            var tick = 0

            override fun run() {
                if (!display.isValid || tick >= durationTicks) {
                    animConfig.onComplete?.invoke()
                    cancel()
                    return
                }

                val progress = tick.toDouble() / durationTicks

                // Apply easing
                val easedProgress = animConfig.easing(progress)

                // Movement
                if (animConfig.movement != Vector(0.0, 0.0, 0.0)) {
                    val offset = animConfig.movement.clone().multiply(easedProgress)
                    val newLoc = startLoc.clone().add(offset)
                    display.teleport(newLoc)
                }

                // Scale animation
                if (animConfig.scaleChange != Vector(0.0, 0.0, 0.0)) {
                    val newScale = startScale.clone().add(
                        animConfig.scaleChange.clone().multiply(easedProgress)
                    )
                    val transform = display.transformation
                    transform.scale.set(
                        newScale.x.coerceAtLeast(0.0),
                        newScale.y.coerceAtLeast(0.0),
                        newScale.z.coerceAtLeast(0.0)
                    )
                    display.transformation = transform
                }

                // Opacity fade
                if (animConfig.fadeOut) {
                    val fadeStart = (durationTicks * animConfig.fadeStartPercent).toInt()
                    if (tick >= fadeStart) {
                        val fadeProgress = (tick - fadeStart).toDouble() / (durationTicks - fadeStart)
                        val opacity = ((1.0 - fadeProgress) * 255).toInt().coerceIn(0, 255)
                        display.textOpacity = opacity.toByte()
                    }
                }

                if (animConfig.fadeIn) {
                    val fadeEnd = (durationTicks * animConfig.fadeEndPercent).toInt()
                    if (tick <= fadeEnd) {
                        val fadeProgress = tick.toDouble() / fadeEnd
                        val opacity = (fadeProgress * 255).toInt().coerceIn(0, 255)
                        display.textOpacity = opacity.toByte()
                    }
                }

                // Custom per-tick callback
                animConfig.onTick?.invoke(progress, tick)

                tick++
            }
        }.runTaskTimer(plugin, 1L, 1L)
    }

    /**
     * Configuration for animations
     */
    class AnimationConfig {
        var movement: Vector = Vector(0.0, 0.0, 0.0)
        var scaleChange: Vector = Vector(0.0, 0.0, 0.0)
        var fadeOut: Boolean = false
        var fadeIn: Boolean = false
        var fadeStartPercent: Double = 0.5
        var fadeEndPercent: Double = 0.3
        var easing: (Double) -> Double = Easing::linear
        var onTick: ((progress: Double, tick: Int) -> Unit)? = null
        var onComplete: (() -> Unit)? = null
    }

    /**
     * Easing functions for smooth animations
     */
    object Easing {
        fun linear(t: Double) = t

        fun easeInQuad(t: Double) = t * t

        fun easeOutQuad(t: Double) = t * (2 - t)

        fun easeInOutQuad(t: Double) = if (t < 0.5) 2 * t * t else -1 + (4 - 2 * t) * t

        fun easeInCubic(t: Double) = t * t * t

        fun easeOutCubic(t: Double): Double {
            val p = t - 1
            return p * p * p + 1
        }

        fun easeInOutCubic(t: Double) =
            if (t < 0.5) 4 * t * t * t else (t - 1) * (2 * t - 2) * (2 * t - 2) + 1

        fun easeOutBack(t: Double): Double {
            val c1 = 1.70158
            val c3 = c1 + 1
            return 1 + c3 * (t - 1).pow(3.0) + c1 * (t - 1).pow(2.0)
        }

        fun easeInElastic(t: Double): Double {
            val c4 = (2 * Math.PI) / 3
            return when (t) {
                0.0 -> 0.0
                1.0 -> 1.0
                else -> (-2.0).pow(10 * t - 10) * sin((t * 10 - 10.75) * c4)
            }
        }

        fun easeOutElastic(t: Double): Double {
            val c4 = (2 * Math.PI) / 3
            return when (t) {
                0.0 -> 0.0
                1.0 -> 1.0
                else -> 2.0.pow(-10 * t) * sin((t * 10 - 0.75) * c4) + 1
            }
        }
    }
}