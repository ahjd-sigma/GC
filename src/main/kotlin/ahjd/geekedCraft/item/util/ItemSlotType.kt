package ahjd.geekedCraft.item.util

enum class ItemSlotType(val description: String) {
    ARROW("Directly buff projectiles, can only have one active at a time"),
    MAIN_HAND("Only applies when held in main hand"),
    OFF_HAND("Only applies when in off-hand slot"),
    ARMOR("Only applies when equipped in armor slot"),
    NONE("Does nothing");

    companion object {
        fun fromString(name: String): ItemSlotType {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}