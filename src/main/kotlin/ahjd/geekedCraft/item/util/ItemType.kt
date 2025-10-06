package ahjd.geekedCraft.item.util

enum class ItemType(val displayName: String, val subtypes: List<ItemSubtype>) {
    ARROW("Arrow", listOf(
        ItemSubtype.NORMALARROW
    )),
    ARMOR("Armor", listOf(
        ItemSubtype.HELMET,
        ItemSubtype.CHESTPLATE,
        ItemSubtype.LEGGINGS,
        ItemSubtype.BOOTS
    )),
    WEAPON("Weapon", listOf(
        ItemSubtype.SPEAR,
        ItemSubtype.TRIDENT,
        ItemSubtype.DAGGER,
        ItemSubtype.SHORT_BOW,
        ItemSubtype.CROSSBOW,
        ItemSubtype.SWORD,
        ItemSubtype.LONGSWORD,
        ItemSubtype.WAND,
        ItemSubtype.GRIMOIRE
    )),
    MISC("Misc", emptyList()),
    CONSUMABLE("Consumable", emptyList()),
    MATERIAL("Material", emptyList()),
    ACCESSORY("Accessory", emptyList());

    companion object {
        fun fromString(name: String): ItemType {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: MISC
        }
    }
}
