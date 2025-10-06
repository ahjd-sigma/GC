package ahjd.geekedCraft.item.util

enum class ItemSubtype(val displayName: String, val parent: ItemType) {

    //Arrow subtypes
    NORMALARROW("NormalArrow", ItemType.ARROW),

    // Armor subtypes
    HELMET("Helmet", ItemType.ARMOR),
    CHESTPLATE("Chestplate", ItemType.ARMOR),
    LEGGINGS("Leggings", ItemType.ARMOR),
    BOOTS("Boots", ItemType.ARMOR),

    // Weapon subtypes
    SPEAR("Spear", ItemType.WEAPON),
    TRIDENT("Trident", ItemType.WEAPON),
    DAGGER("Dagger", ItemType.WEAPON),
    SHORT_BOW("Short Bow", ItemType.WEAPON),
    CROSSBOW("Crossbow", ItemType.WEAPON),
    SWORD("Sword", ItemType.WEAPON),
    LONGSWORD("Longsword", ItemType.WEAPON),
    WAND("Wand", ItemType.WEAPON),
    GRIMOIRE("Grimoire", ItemType.WEAPON);

    companion object {
        fun fromString(name: String): ItemSubtype? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}