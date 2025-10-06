package ahjd.geekedCraft.item.util

/**
 * Centralized color definitions for all stats in the game.
 * Each stat has an associated hex color for consistent display across the UI.
 */
enum class StatColor(val hex: String, val displayName: String) {
    // Core Stats - Vital colors
    HEALTH("#FF5555", "❤ Health"),
    MAX_HEALTH("#FF5555", "❤ Max Health"),
    HEALTH_REGEN("#FF9999", "❤ Health Regen"),
    DEFENSE("#5555FF", "🛡 Defense"),
    BASE_RESISTANCE("#5599FF", "🛡 Resistance"),
    MANA("#55FFFF", "✦ Mana"),
    SPEED("#55FF55", "⚡ Speed"),
    ATTACK_SPEED("#FFFF55", "⚔ Attack Speed"),
    KNOCKBACK("#FF8800", "💥 Knockback"),
    KNOCKBACK_RESISTANCE("#FF8800", "💥 Knockback Resistance"),

    // Combat Stats - Yellow/Gold tones
    CRIT_CHANCE("#FFAA00", "☄ Crit Chance"),
    CRIT_DAMAGE("#FF5500", "☄ Crit Damage"),
    SWEEP("#FFDD55", "🌀 Sweep"),
    RANGE("#FFCC88", "📏 Range"),

    // Attack Types - Distinct combat colors
    MELEE("#FF6B6B", "⚔ Melee Damage"),
    PROJECTILE("#88DD88", "🏹 Projectile Damage"),
    MAGIC("#BB88FF", "✨ Magic Damage"),

    // Global Damage - Neutral aggressive
    DAMAGE("#FFAA55", "⚔ Damage"),

    // Elemental Damage - Matching element themes
    ELEMENTAL("#DD88FF", "🔮 Elemental Damage"),
    THUNDER("#FFFF00", "⚡ Thunder Damage"),
    EARTH("#8B4513", "🌍 Earth Damage"),
    WATER("#0099FF", "💧 Water Damage"),
    FIRE("#FF4400", "🔥 Fire Damage"),
    AIR("#E0E0E0", "💨 Air Damage"),
    DARK("#4B0082", "🌑 Dark Damage"),
    LIGHT("#FFFFE0", "☀ Light Damage"),
    NEUTRAL("#CCCCCC", "⚪ Neutral Damage"),

    // Resistances - Defensive versions of element colors
    THUNDER_RESISTANCE("#DDDD00", "⚡ Thunder Resistance"),
    EARTH_RESISTANCE("#A0522D", "🌍 Earth Resistance"),
    WATER_RESISTANCE("#0088DD", "💧 Water Resistance"),
    FIRE_RESISTANCE("#DD3300", "🔥 Fire Resistance"),
    AIR_RESISTANCE("#C0C0C0", "💨 Air Resistance"),
    DARK_RESISTANCE("#663399", "🌑 Dark Resistance"),
    LIGHT_RESISTANCE("#FFFFCC", "☀ Light Resistance"),
    NEUTRAL_RESISTANCE("#AAAAAA", "⚪ Neutral Resistance"),

    // DoT Stats - Danger colors
    BLEEDING("#8B0000", "🩸 Bleeding"),
    POISON("#00FF00", "☠ Poison");

    companion object {
        // Map stat keys to their colors
        private val statKeyMap = mapOf(
            "health" to HEALTH,
            "maxhealth" to MAX_HEALTH,
            "healthregen" to HEALTH_REGEN,
            "defense" to DEFENSE,
            "baseResistance" to BASE_RESISTANCE,
            "mana" to MANA,
            "speed" to SPEED,
            "attackspeed" to ATTACK_SPEED,
            "knockback" to KNOCKBACK,
            "knockbackResistance" to KNOCKBACK_RESISTANCE,

            "critChance" to CRIT_CHANCE,
            "critDamage" to CRIT_DAMAGE,
            "sweep" to SWEEP,
            "range" to RANGE,

            "meleeRaw" to MELEE,
            "meleePercent" to MELEE,
            "projectileRaw" to PROJECTILE,
            "projectilePercent" to PROJECTILE,
            "magicRaw" to MAGIC,
            "magicPercent" to MAGIC,

            "damageRaw" to DAMAGE,
            "damagePercent" to DAMAGE,

            "elementalRaw" to ELEMENTAL,
            "elementalPercent" to ELEMENTAL,
            "thunderRaw" to THUNDER,
            "thunderPercent" to THUNDER,
            "earthRaw" to EARTH,
            "earthPercent" to EARTH,
            "waterRaw" to WATER,
            "waterPercent" to WATER,
            "fireRaw" to FIRE,
            "firePercent" to FIRE,
            "airRaw" to AIR,
            "airPercent" to AIR,
            "darkRaw" to DARK,
            "darkPercent" to DARK,
            "lightRaw" to LIGHT,
            "lightPercent" to LIGHT,
            "neutralRaw" to NEUTRAL,
            "neutralPercent" to NEUTRAL,

            "thunderResistance" to THUNDER_RESISTANCE,
            "earthResistance" to EARTH_RESISTANCE,
            "waterResistance" to WATER_RESISTANCE,
            "fireResistance" to FIRE_RESISTANCE,
            "airResistance" to AIR_RESISTANCE,
            "darkResistance" to DARK_RESISTANCE,
            "lightResistance" to LIGHT_RESISTANCE,
            "neutralResistance" to NEUTRAL_RESISTANCE,

            "bleeding" to BLEEDING,
            "poison" to POISON
        )

        /**
         * Get the color for a specific stat key.
         * Returns DAMAGE color as default if stat not found.
         */
        private fun fromStatKey(statKey: String): StatColor {
            return statKeyMap[statKey] ?: DAMAGE
        }

        /**
         * Get the display name for a stat key with its icon.
         */
        fun getDisplayName(statKey: String): String {
            return statKeyMap[statKey]?.displayName
                ?: statKey.replaceFirstChar { it.uppercase() }
        }

        /**
         * Get the hex color code for a stat key.
         */
        fun getHex(statKey: String): String {
            return fromStatKey(statKey).hex
        }

        /**
         * Convert hex color to Minecraft color code (§x format).
         * Example: #FF5555 -> §x§f§f§5§5§5§5
         */
        private fun hexToMinecraft(hex: String): String {
            val cleanHex = hex.removePrefix("#")
            return "§x" + cleanHex.map { "§$it" }.joinToString("")
        }

        /**
         * Get the full Minecraft color code for a stat key.
         */
        fun getMinecraftColor(statKey: String): String {
            return hexToMinecraft(getHex(statKey))
        }
    }
}