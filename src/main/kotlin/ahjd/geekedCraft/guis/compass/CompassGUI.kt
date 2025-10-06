package ahjd.geekedCraft.guis.compass

import ahjd.geekedCraft.item.itemstacks.StatCompass
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object CompassGUI {

    fun hasOpenGUI(player: Player): Boolean {
        val openInventory = player.openInventory
        return openInventory.type != org.bukkit.event.inventory.InventoryType.PLAYER &&
                openInventory.type != org.bukkit.event.inventory.InventoryType.CRAFTING &&
                openInventory.type != org.bukkit.event.inventory.InventoryType.CREATIVE
    }

    fun openStatsGUI(player: Player) {
        val gui = Bukkit.createInventory(
            null,
            36, // Increased from 27 to 36 to accommodate 6 stat items
            Component.text("Your Stats").color(NamedTextColor.GOLD)
        )

        // Get the category items from StatCompass (now returns 6 items including Equipment Breakdown)
        val categoryItems = StatCompass.getStatItems(player)

        // Layout positions for 6 items (2 rows of 3)
        // Row 1: slots 10, 12, 14
        // Row 2: slots 19, 21, 23
        val itemSlots = listOf(
            10, 12, 14,  // Top row
            19, 21, 23   // Bottom row
        )

        categoryItems.forEachIndexed { index, item ->
            if (index < itemSlots.size) {
                gui.setItem(itemSlots[index], item)
            }
        }

        // Decorative filler (gray glass pane)
        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val fillerMeta = filler.itemMeta!!
        fillerMeta.displayName(Component.empty())
        filler.itemMeta = fillerMeta

        // Fill all other empty slots with glass panes
        for (i in 0 until gui.size) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler)
            }
        }

        player.openInventory(gui)
    }

    fun isStatCompass(item: ItemStack?): Boolean {
        return item?.type == Material.COMPASS &&
                item.hasItemMeta() &&
                item.itemMeta?.displayName()?.let { displayName ->
                    net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(displayName).contains("Stats Menu")
                } == true
    }
}