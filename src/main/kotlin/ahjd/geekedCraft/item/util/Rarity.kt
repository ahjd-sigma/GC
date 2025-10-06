package ahjd.geekedCraft.item.util

enum class Rarity(val displayName: String, val colorStart: String, val colorEnd: String? = null) {
    COMMON("Common", "#FFFFFF"),
    UNCOMMON("Uncommon", "#55FF55"),
    RARE("Rare", "#5555FF"),
    EPIC("Epic", "#AA00AA"),
    LEGENDARY("Legendary", "#FFAA00"),
    MYTHIC("Mythic", "#FF1493", "#9932CC"),
    DIVINE("Divine", "#00FFFF", "#FFD700"),
    ANCIENT("Ancient", "#8B0000", "#FF0000"),
    CELESTIAL("Celestial", "#E0FFFF", "#4169E1"),
    PRIMORDIAL("Primordial", "#0A0033", "#00FFFF");

    companion object {
        fun fromString(name: String): Rarity {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: COMMON
        }
    }
}
