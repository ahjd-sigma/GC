package ahjd.geekedCraft.hologram.util

import ahjd.geekedCraft.damage.util.DamageBreakdown
import ahjd.geekedCraft.damage.util.ElementType
import ahjd.geekedCraft.damage.util.EnvCause
import ahjd.geekedCraft.hologram.TempHologramController
import ahjd.geekedCraft.main.GeekedCraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class DamageIndicatorHolo {
    private val plugin: GeekedCraft = GeekedCraft.getInstance()

    companion object {
        private const val SHOULDER_HEIGHT = 2.2
        private const val SHOULDER_RADIUS = 1.3
        private const val ANIMATION_DURATION = 25L
        private const val MAX_RISE_HEIGHT = 0.07
        private const val FALL_DISTANCE = 0.6
        private const val RISE_PERCENT = 0.1 // 10% rising, 90% falling
        private const val BASE_SCALE = 0.07
        private const val CRIT_SCALE = 0.1
    }

    private fun ElementType.toSymbol(): ElementalSymbols {
        return when (this) {
            ElementType.THUNDER -> ElementalSymbols.THUNDER
            ElementType.EARTH -> ElementalSymbols.EARTH
            ElementType.WATER -> ElementalSymbols.WATER
            ElementType.FIRE -> ElementalSymbols.FIRE
            ElementType.AIR -> ElementalSymbols.AIR
            ElementType.DARK -> ElementalSymbols.DARK
            ElementType.LIGHT -> ElementalSymbols.LIGHT
            ElementType.NEUTRAL -> ElementalSymbols.NEUTRAL
        }
    }

    /**
     * Formats a damage text component for normal or crit damage.
     * Crits have an element-specific gradient and a star at the end.
     */
    private fun formatDamageText(symbol: ElementalSymbols, element: ElementType, damage: Int, isCrit: Boolean): Component {
        return if (isCrit) {
            val (startColor, endColor) = when (element) {
                ElementType.LIGHT -> 0xFFFFE0 to 0xFFFFFF
                ElementType.THUNDER -> 0xFFFF00 to 0xFFD700
                ElementType.FIRE -> 0xFF4500 to 0xFF8C00
                ElementType.WATER -> 0x00BFFF to 0x1E90FF
                ElementType.EARTH -> 0x228B22 to 0x32CD32
                ElementType.AIR -> 0xADD8E6 to 0xE0FFFF
                ElementType.DARK -> 0x4B0082 to 0x800080
                ElementType.NEUTRAL -> 0xC0C0C0 to 0x808080
            }

            Component.text()
                .append(Component.text(symbol.symbol, TextColor.color(startColor)))
                .append(Component.text(" -$damage ", TextColor.color(endColor)))
                .append(Component.text("✦", TextColor.color(endColor)))
                .build()
        } else {
            Component.text("${symbol.symbol} -$damage", TextColor.color(symbol.colorHex))
        }
    }
    fun showEnvironmentalIndicator(location: Location, healthLoss: Int, envCause: EnvCause) {
        if (healthLoss <= 0) return

        val baseLoc = location.clone().add(0.0, SHOULDER_HEIGHT, 0.0)

        val element = when (envCause) {
            EnvCause.LAVA, EnvCause.FIRE, EnvCause.FIRE_TICK, EnvCause.HOT_FLOOR, EnvCause.CAMPFIRE, EnvCause.BLOCK_EXPLOSION -> ElementType.FIRE
            EnvCause.DROWNING -> ElementType.WATER
            EnvCause.FREEZE -> ElementType.AIR
            EnvCause.VOID -> ElementType.DARK
            EnvCause.LIGHTNING -> ElementType.THUNDER
            EnvCause.CONTACT, EnvCause.THORNS -> ElementType.EARTH
            else -> ElementType.NEUTRAL
        }

        val symbol = element.toSymbol()
        val text = formatDamageText(symbol, element, healthLoss, isCrit = false)

        val angleOffset = if (Math.random() < 0.5) Math.PI / 4 else -Math.PI / 4
        val spawnLoc = baseLoc.clone().add(cos(angleOffset) * SHOULDER_RADIUS, 0.0, sin(angleOffset) * SHOULDER_RADIUS)

        val display = TempHologramController.spawnTemp(spawnLoc, text, ANIMATION_DURATION, plugin) {
            this.billboard = Display.Billboard.CENTER
            this.isShadowed = true
            this.alignment = TextDisplay.TextAlignment.CENTER
        }

        display?.let {
            val startLoc = spawnLoc.clone()
            HologramAnimation.animate(it, plugin, ANIMATION_DURATION.toInt()) {
                onTick = { progress, _ ->
                    val yOffset = if (progress < RISE_PERCENT) {
                        // Rise phase
                        (progress / RISE_PERCENT) * MAX_RISE_HEIGHT
                    } else {
                        // Fall phase
                        MAX_RISE_HEIGHT - ((progress - RISE_PERCENT) / (1.0 - RISE_PERCENT)) * (MAX_RISE_HEIGHT + FALL_DISTANCE)
                    }
                    it.teleport(startLoc.clone().add(0.0, yOffset, 0.0))
                }
                fadeIn = true
                fadeEndPercent = 0.2
                fadeOut = true
                fadeStartPercent = 0.7
                scaleChange = Vector(BASE_SCALE, BASE_SCALE, BASE_SCALE)
            }
        }
    }

    fun showDamageIndicatorsBounce(location: Location, breakdown: DamageBreakdown) {
        val baseLoc = location.clone().add(0.0, SHOULDER_HEIGHT, 0.0)

        breakdown.elementalDamages.forEach { (element, damage) ->
            if (damage <= 0) return@forEach

            val symbol = element.toSymbol()
            val text = formatDamageText(symbol, element, damage, breakdown.wasCrit)

            val angle = Math.random() * 2 * Math.PI
            val radius = 0.5 + Math.random() * 0.3
            val spawnLoc = baseLoc.clone().add(
                cos(angle) * radius,
                Math.random() * 0.15,
                sin(angle) * radius
            )

            val display = TempHologramController.spawnTemp(spawnLoc, text, ANIMATION_DURATION, plugin) {
                this.billboard = Display.Billboard.CENTER
                this.isShadowed = true
                this.alignment = TextDisplay.TextAlignment.CENTER
            }

            display?.let {
                val startLoc = spawnLoc.clone()
                HologramAnimation.animate(it, plugin, ANIMATION_DURATION.toInt()) {
                    onTick = { progress, _ ->
                        val yOffset = if (progress < RISE_PERCENT) {
                            // Rise phase
                            (progress / RISE_PERCENT) * MAX_RISE_HEIGHT
                        } else {
                            // Fall phase
                            MAX_RISE_HEIGHT - ((progress - RISE_PERCENT) / (1.0 - RISE_PERCENT)) * (MAX_RISE_HEIGHT + FALL_DISTANCE)
                        }
                        it.teleport(startLoc.clone().add(0.0, yOffset, 0.0))
                    }
                    fadeOut = true
                    fadeStartPercent = 0.7
                    scaleChange = if (breakdown.wasCrit) Vector(CRIT_SCALE, CRIT_SCALE, CRIT_SCALE) else Vector(BASE_SCALE, BASE_SCALE, BASE_SCALE)
                }
            }
        }
    }
}