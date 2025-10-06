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

    /** Standard vertical damage indicators */
    fun showDamageIndicators(location: Location, breakdown: DamageBreakdown) {
        val baseLoc = location.clone()
        var verticalOffset = 0.0

        breakdown.elementalDamages.forEach { (element, damage) ->
            if (damage <= 0) return@forEach

            val symbol = element.toSymbol()
            val text = formatDamageText(symbol, element, damage, breakdown.wasCrit)

            val spawnLoc = baseLoc.clone().add(
                (Math.random() - 0.5) * 0.3,
                verticalOffset,
                (Math.random() - 0.5) * 0.3
            )

            val display = TempHologramController.spawnTemp(spawnLoc, text, 40L, plugin) {
                this.billboard = Display.Billboard.CENTER
                this.isShadowed = true
                this.alignment = TextDisplay.TextAlignment.CENTER
            }

            display?.let {
                HologramAnimation.animate(it, plugin, 40) {
                    movement = Vector(0.0, 1.0, 0.0)
                    easing = HologramAnimation.Easing::easeOutQuad
                    fadeOut = true
                    fadeStartPercent = 0.5
                    scaleChange = if (breakdown.wasCrit) Vector(0.3, 0.3, 0.3) else Vector(0.2, 0.2, 0.2)
                }
            }

            verticalOffset += 0.3
        }
    }

    fun showEnvironmentalIndicator(location: Location, healthLoss: Int, envCause: EnvCause) {
        if (healthLoss <= 0) return

        val baseLoc = location.clone()

        // Map environmental causes to ElementType
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

        val radius = 0.5
        val angleOffset = 0.0
        val spawnLoc = baseLoc.clone().add(cos(angleOffset) * radius, 0.5, sin(angleOffset) * radius)

        val display = TempHologramController.spawnTemp(spawnLoc, text, 30L, plugin) {
            this.billboard = Display.Billboard.CENTER
            this.isShadowed = true
            this.alignment = TextDisplay.TextAlignment.CENTER
        }

        display?.let {
            HologramAnimation.animate(it, plugin, 30) {
                scaleChange = Vector(0.7, 0.7, 0.7)
                easing = HologramAnimation.Easing::easeOutBack
                fadeIn = true
                fadeEndPercent = 0.2
                fadeOut = true
                fadeStartPercent = 0.7
                movement = Vector(0.0, 0.8, 0.0)
            }
        }
    }


    /** Circular pop-out damage indicators */
    fun showDamageIndicatorsPopup(location: Location, breakdown: DamageBreakdown) {
        val baseLoc = location.clone()
        var angleOffset = 0.0

        breakdown.elementalDamages.forEach { (element, damage) ->
            if (damage <= 0) return@forEach

            val symbol = element.toSymbol()
            val text = formatDamageText(symbol, element, damage, breakdown.wasCrit)

            val radius = 0.5
            val x = cos(angleOffset) * radius
            val z = sin(angleOffset) * radius
            val spawnLoc = baseLoc.clone().add(x, 0.5, z)

            val display = TempHologramController.spawnTemp(spawnLoc, text, 30L, plugin) {
                this.billboard = Display.Billboard.CENTER
                this.isShadowed = true
                this.alignment = TextDisplay.TextAlignment.CENTER
            }

            display?.let {
                HologramAnimation.animate(it, plugin, 30) {
                    scaleChange = if (breakdown.wasCrit) Vector(0.7, 0.7, 0.7) else Vector(0.5, 0.5, 0.5)
                    easing = HologramAnimation.Easing::easeOutBack
                    fadeIn = true
                    fadeEndPercent = 0.2
                    fadeOut = true
                    fadeStartPercent = 0.7
                    movement = Vector(x * 0.5, 0.8, z * 0.5)
                }
            }

            angleOffset += Math.PI * 2 / breakdown.elementalDamages.size
        }
    }

    /** Bouncing damage indicators */
    fun showDamageIndicatorsBounce(location: Location, breakdown: DamageBreakdown) {
        val baseLoc = location.clone()

        breakdown.elementalDamages.forEach { (element, damage) ->
            if (damage <= 0) return@forEach

            val symbol = element.toSymbol()
            val text = formatDamageText(symbol, element, damage, breakdown.wasCrit)

            val spawnLoc = baseLoc.clone().add(
                (Math.random() - 0.5) * 0.4,
                1.0,
                (Math.random() - 0.5) * 0.4
            )

            val display = TempHologramController.spawnTemp(spawnLoc, text, 40L, plugin) {
                this.billboard = Display.Billboard.CENTER
                this.isShadowed = true
                this.alignment = TextDisplay.TextAlignment.CENTER
            }

            display?.let {
                val startLoc = spawnLoc.clone()
                HologramAnimation.animate(it, plugin, 40) {
                    onTick = { progress, _ ->
                        val height = sin(progress * Math.PI) * 1.2
                        it.teleport(startLoc.clone().add(0.0, height, 0.0))
                    }
                    fadeOut = true
                    fadeStartPercent = 0.6
                    easing = HologramAnimation.Easing::linear
                    scaleChange = if (breakdown.wasCrit) Vector(0.5, 0.5, 0.5) else Vector(0.3, 0.3, 0.3)
                }
            }
        }
    }

    /** Drift-style damage indicators with crit customization */
    fun showDamageIndicatorsDrift(location: Location, breakdown: DamageBreakdown) {
        val baseLoc = location.clone()

        breakdown.elementalDamages.forEach { (element, damage) ->
            if (damage <= 0) return@forEach

            val symbol = element.toSymbol()
            val isCrit = breakdown.wasCrit
            val text = formatDamageText(symbol, element, damage, isCrit)

            val horizontalOffsetX = (Math.random() - 0.5) * 0.4
            val horizontalOffsetZ = (Math.random() - 0.5) * 0.4
            val spawnLoc = baseLoc.clone().add(horizontalOffsetX, 0.8, horizontalOffsetZ)

            val display = TempHologramController.spawnTemp(spawnLoc, text, 40L, plugin) {
                this.billboard = Display.Billboard.CENTER
                this.isShadowed = true
                this.alignment = TextDisplay.TextAlignment.CENTER
            }

            display?.let {
                val startLoc = spawnLoc.clone()
                HologramAnimation.animate(it, plugin, 40) {
                    onTick = { progress, _ ->
                        val height = sin(progress * Math.PI) * 1.0 + 0.2
                        val x = startLoc.x + horizontalOffsetX
                        val y = startLoc.y + height
                        val z = startLoc.z + horizontalOffsetZ
                        it.teleport(Location(startLoc.world, x, y, z))
                    }
                    scaleChange = if (isCrit) Vector(0.5, 0.5, 0.5) else Vector(0.3, 0.3, 0.3)
                    fadeOut = true
                    fadeStartPercent = 0.6
                    easing = HologramAnimation.Easing::easeOutQuad
                }
            }
        }
    }
}