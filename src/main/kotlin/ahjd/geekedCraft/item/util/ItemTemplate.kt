package ahjd.geekedCraft.item.util

import org.bukkit.Material

data class ItemTemplate(
    val id: String,
    val name: String,
    val material: Material,
    val rarity: Rarity = Rarity.COMMON,
    val type: ItemType = ItemType.MISC,
    val subtype: ItemSubtype? = null,
    val slotType: ItemSlotType = ItemSlotType.NONE,
    val stackable: Boolean = true,
    val customModelData: Int? = null,
    val description: String? = null,
    val baseLore: List<String> = emptyList(),
    val stats: Map<String, Int> = emptyMap(),
    val abilityId: String? = null
) {
    companion object {
        fun getDisplayName(statKey: String): String {
            return StatColor.getDisplayName(statKey)
        }

        fun isPercentStat(statKey: String): Boolean {
            return statKey.endsWith("Percent", ignoreCase = true)
        }
    }

    fun getFormattedLore(): List<String> {
        val lore = mutableListOf<String>()

        // Add base lore (if any)
        if (baseLore.isNotEmpty()) {
            lore.addAll(baseLore)
        }

        // Add stats section organized by category
        if (stats.isNotEmpty()) {
            addOrganizedStats(lore)
        }

        // Add ability section
        if (abilityId != null) {
            val ability = ahjd.geekedCraft.item.ability.AbilityRegistry.get(abilityId)
            if (ability != null) {
                if (lore.isNotEmpty()) lore.add("")
                lore.add("§6§lPassive Ability: §e${ability.displayName}")
                lore.add("§8${ability.description}")
            }
        }

        // Add description
        if (description != null) {
            if (lore.isNotEmpty()) lore.add("")
            lore.add("§8$description")
        }

        // Add rarity line
        if (lore.isNotEmpty()) lore.add("")
        val itemCategory = subtype?.displayName?.uppercase() ?: type.displayName.uppercase()
        val rarityLine = RarityUtil.getGradientText("${rarity.displayName.uppercase()} $itemCategory", rarity)
        lore.add(rarityLine)

        return lore
    }

    private fun addOrganizedStats(lore: MutableList<String>) {
        val categorized = categorizeStats()

        // Determine order based on item type
        val sections = when (type) {
            ItemType.ARMOR -> listOf("defensive", "damage", "misc")
            ItemType.WEAPON -> listOf("damage", "defensive", "misc")
            else -> listOf("damage", "defensive", "misc")
        }

        var firstSection = true
        sections.forEach { sectionKey ->
            val sectionStats = categorized[sectionKey] ?: return@forEach
            if (sectionStats.isEmpty()) return@forEach

            // Add spacing before section (except first)
            if (firstSection) {
                if (lore.isNotEmpty()) lore.add("")
                firstSection = false
            } else {
                lore.add("")
            }

            // Add section stats with colored display names
            sectionStats.forEach { (stat, value) ->
                val colorCode = StatColor.getMinecraftColor(stat)
                val displayName = StatColor.getDisplayName(stat)
                val prefix = if (value > 0) "§a+" else "§c"
                val suffix = if (isPercentStat(stat)) "%" else ""
                lore.add("$colorCode$displayName§7: $prefix$value$suffix")
            }
        }
    }

    private fun categorizeStats(): Map<String, Map<String, Int>> {
        val defensive = listOf(
            "defense", "baseResistance", "maxhealth", "healthregen",
            "thunderResistance", "earthResistance", "waterResistance",
            "fireResistance", "airResistance", "darkResistance",
            "lightResistance", "neutralResistance", "knockbackResistance"
        )

        val damage = listOf(
            "meleeRaw", "meleePercent", "projectileRaw", "projectilePercent",
            "magicRaw", "magicPercent", "damageRaw", "damagePercent",
            "elementalRaw", "elementalPercent", "thunderRaw", "thunderPercent",
            "earthRaw", "earthPercent", "waterRaw", "waterPercent",
            "fireRaw", "firePercent", "airRaw", "airPercent",
            "darkRaw", "darkPercent", "lightRaw", "lightPercent",
            "neutralRaw", "neutralPercent", "critChance", "critDamage",
            "bleeding", "poison"
        )

        return mapOf(
            "defensive" to stats.filterKeys { it in defensive },
            "damage" to stats.filterKeys { it in damage },
            "misc" to stats.filterKeys { it !in defensive && it !in damage }
        )
    }
}