package ahjd.geekedCraft.item.itemstacks

import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.human.HumanStats
import ahjd.geekedCraft.item.util.StatColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object StatCompass {

    fun createCompass(): ItemStack {
        val compass = ItemStack(Material.COMPASS)
        val meta = compass.itemMeta!!

        meta.displayName(
            Component.text("Stats Menu")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
        )

        meta.lore(
            listOf(
                Component.text("Right-click to view your stats")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            )
        )

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        compass.itemMeta = meta
        return compass
    }

    /**
     * Returns a list of categorized stat items with base/equipment breakdown.
     */
    fun getStatItems(player: Player): List<ItemStack> {
        val stats = HumanStatManager.get(player.uniqueId.toString())

        val categories = listOf(
            Triple("Main Stats", Material.HEART_OF_THE_SEA, listOf(
                "health", "maxhealth", "healthregen", "defense", "baseResistance",
                "mana", "speed", "attackspeed", "knockback", "knockbackResistance"
            )),
            Triple("Combat Stats", Material.IRON_SWORD, listOf(
                "critChance", "critDamage", "sweep", "range",
                "meleeRaw", "meleePercent", "projectileRaw", "projectilePercent",
                "magicRaw", "magicPercent", "damageRaw", "damagePercent"
            )),
            Triple("Elemental Damage", Material.BLAZE_POWDER, listOf(
                "elementalRaw", "elementalPercent",
                "thunderRaw", "thunderPercent", "earthRaw", "earthPercent",
                "waterRaw", "waterPercent", "fireRaw", "firePercent",
                "airRaw", "airPercent", "darkRaw", "darkPercent",
                "lightRaw", "lightPercent", "neutralRaw", "neutralPercent"
            )),
            Triple("Elemental Defenses", Material.SHIELD, listOf(
                "thunderResistance", "earthResistance", "waterResistance", "fireResistance",
                "airResistance", "darkResistance", "lightResistance", "neutralResistance"
            )),
            Triple("DoT Effects", Material.SPIDER_EYE, listOf("bleeding", "poison"))
        )

        return categories.map { (title, mat, keys) ->
            createStatItem(title, mat, stats, keys)
        } + createEquipmentBreakdownItem(stats)
    }

    /**
     * Create a stat display item showing total, base, and equipment bonus
     */
    private fun createStatItem(
        title: String,
        material: Material,
        stats: HumanStats,
        statKeys: List<String>
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta!!

        meta.displayName(
            Component.text(title)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
        )

        val loreLines = mutableListOf<Component>()

        statKeys.forEach { key ->
            val total = stats.getValue(key) ?: 0
            val base = stats.getBaseValue(key) ?: 0
            val equipBonus = stats.getTotalEquipmentBonus(key)  // Use proper method!

            val displayName = StatColor.getDisplayName(key)
            val statColor = hexToTextColor(StatColor.getHex(key))
            val suffix = if (key.endsWith("Percent", ignoreCase = true)) "%" else ""

            // Main line: Colored stat name with total value
            val mainLine = Component.text("$displayName: ")
                .color(statColor)
                .append(Component.text("$total$suffix").color(getColorForValue(total)))
                .decoration(TextDecoration.ITALIC, false)

            loreLines.add(mainLine)

            // If there's an equipment bonus, show breakdown
            if (equipBonus != 0) {
                val breakdownLine = Component.text("  └ ")
                    .color(NamedTextColor.DARK_GRAY)
                    .append(Component.text("Base: $base$suffix").color(NamedTextColor.WHITE))
                    .append(Component.text(" | ").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text("Equipment: ${formatBonus(equipBonus)}$suffix")
                        .color(if (equipBonus > 0) NamedTextColor.GREEN else NamedTextColor.RED))
                    .decoration(TextDecoration.ITALIC, false)

                loreLines.add(breakdownLine)
            }
        }

        meta.lore(loreLines)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
        item.itemMeta = meta
        return item
    }

    /**
     * Create an item showing which equipment slots provide what bonuses
     */
    private fun createEquipmentBreakdownItem(stats: HumanStats): ItemStack {
        val item = ItemStack(Material.ARMOR_STAND)
        val meta = item.itemMeta!!

        meta.displayName(
            Component.text("Equipment Breakdown")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
        )

        val loreLines = mutableListOf<Component>()

        // Check each equipment slot (including ARROW now!)
        HumanStats.EquipmentSlot.entries.forEach { slot ->
            val slotBonuses = getSlotBonuses(stats, slot)

            if (slotBonuses.isNotEmpty()) {
                // Slot header
                loreLines.add(
                    Component.text("${formatSlotName(slot)}:")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false)
                )

                // Slot bonuses with colored stat names
                slotBonuses.forEach { (statName, value) ->
                    val displayName = StatColor.getDisplayName(statName)
                    val statColor = hexToTextColor(StatColor.getHex(statName))
                    val suffix = if (statName.endsWith("Percent", ignoreCase = true)) "%" else ""

                    loreLines.add(
                        Component.text("  • ")
                            .color(NamedTextColor.DARK_GRAY)
                            .append(Component.text("$displayName: ").color(statColor))
                            .append(Component.text("${formatBonus(value)}$suffix")
                                .color(if (value > 0) NamedTextColor.GREEN else NamedTextColor.RED))
                            .decoration(TextDecoration.ITALIC, false)
                    )
                }

                loreLines.add(Component.empty()) // Spacing between slots
            }
        }

        if (loreLines.isEmpty()) {
            loreLines.add(
                Component.text("No equipment bonuses active")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, true)
            )
        } else {
            // Remove last empty line
            if (loreLines.lastOrNull() == Component.empty()) {
                loreLines.removeAt(loreLines.lastIndex)
            }

            // Add helpful info about arrow slot
            loreLines.add(Component.empty())
            loreLines.add(
                Component.text("Tip: Place arrow in top-left")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
            )
            loreLines.add(
                Component.text("inventory slot to apply bonuses")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
            )
        }

        meta.lore(loreLines)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
        item.itemMeta = meta
        return item
    }

    /**
     * Get all stat bonuses from a specific equipment slot
     */
    private fun getSlotBonuses(stats: HumanStats, slot: HumanStats.EquipmentSlot): Map<String, Int> {
        val bonuses = mutableMapOf<String, Int>()

        // Get all possible stat names
        val allStats = stats.getAllStats()

        allStats.keys.forEach { statName ->
            val bonus = stats.getSlotBonus(slot, statName)
            if (bonus != 0) {
                bonuses[statName] = bonus
            }
        }

        return bonuses.toSortedMap() // Sort alphabetically for consistency
    }

    private fun formatSlotName(slot: HumanStats.EquipmentSlot): String {
        return slot.name.split("_")
            .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    private fun formatBonus(value: Int): String {
        return if (value > 0) "+$value" else "$value"
    }

    private fun getColorForValue(value: Int): NamedTextColor {
        return when {
            value > 0 -> NamedTextColor.GREEN
            value < 0 -> NamedTextColor.RED
            else -> NamedTextColor.GRAY
        }
    }

    /**
     * Convert hex color string to Kyori TextColor
     */
    private fun hexToTextColor(hex: String): TextColor {
        val cleanHex = hex.removePrefix("#")
        return TextColor.fromHexString("#$cleanHex") ?: NamedTextColor.WHITE
    }
}