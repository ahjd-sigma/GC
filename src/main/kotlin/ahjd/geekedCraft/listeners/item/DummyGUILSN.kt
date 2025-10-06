package ahjd.geekedCraft.listeners.item

import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.guis.mob.DummyGUI
import ahjd.geekedCraft.mob.MobManager
import ahjd.geekedCraft.mob.misc.dummy.DummyCombatTracker
import ahjd.geekedCraft.mob.misc.dummy.DummyDPSTest
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

class DummyGUILSN : Listener {

    private val openGUIs = mutableMapOf<Player, String>() // player -> dummyUUID

    fun openGUI(player: Player, dummyUUID: String) {
        openGUIs[player] = dummyUUID
        val mobStats = MobManager.get(dummyUUID)
        DummyGUI.openMainMenu(player, mobStats)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = event.view.title

        // Check if it's one of our dummy GUIs
        if (!title.contains("Training Dummy") && !title.contains("Dummy §7-")) return

        event.isCancelled = true

        val dummyUUID = openGUIs[player] ?: return
        if (!MobManager.has(dummyUUID)) {
            player.closeInventory()
            player.sendMessage("§c§l✖ §cDummy no longer exists!")
            openGUIs.remove(player)
            return
        }

        val mobStats = MobManager.get(dummyUUID)
        val item = event.currentItem ?: return
        val itemName = item.itemMeta?.displayName ?: return

        // Navigation
        when {
            itemName.contains("DPS Test Active") -> {
                // Stop active DPS test
                if (DummyDPSTest.stopTest(player)) {
                    DummyGUI.openMiscSettings(player, mobStats)
                }
            }

            itemName.contains("Start DPS Test") -> {
                val damageType = when {
                    event.isShiftClick -> DamageType.MAGIC
                    event.isRightClick -> DamageType.PROJECTILE
                    event.isLeftClick -> DamageType.MELEE
                    else -> return
                }

                if (DummyDPSTest.startTest(player, dummyUUID, damageType)) {
                    player.closeInventory()
                }
            }

            itemName.contains("Combat Stats") -> DummyGUI.openCombatStats(player, mobStats)
            itemName.contains("Elemental Damage") -> DummyGUI.openElementalDamage(player, mobStats)
            itemName.contains("Resistances") -> DummyGUI.openResistances(player, mobStats)
            itemName.contains("Misc Settings") -> DummyGUI.openMiscSettings(player, mobStats)
            itemName.contains("Back to Main Menu") -> DummyGUI.openMainMenu(player, mobStats)

            itemName.contains("Current Performance") -> {
                DummyCombatTracker.clear(dummyUUID)
                player.sendMessage("§a§l✓ §aReset dummy combat stats!")
                DummyGUI.openMainMenu(player, mobStats)
            }

            itemName.contains("Dummy Info") -> {
                player.closeInventory()
                openGUIs.remove(player)

                if (DummyCombatTracker.despawnDummy(dummyUUID)) {
                    player.sendMessage("§a§l✓ §aDespawned training dummy!")
                } else {
                    player.sendMessage("§c§l✖ §cERROR! Couldn't despawn training dummy!")
                }
            }
        }

        // Stat modification
        val key = NamespacedKey(ahjd.geekedCraft.main.GeekedCraft.getInstance(), "stat_key")
        val statKey = item.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.STRING) ?: return

        val amount = when {
            event.isShiftClick && event.isLeftClick -> 10
            event.isShiftClick && event.isRightClick -> -10
            event.isLeftClick -> 1
            event.isRightClick -> -1
            else -> 0
        }

        if (amount != 0) {
            val success = when (statKey) {
                "iframes" -> {
                    val newValue = (mobStats.iframes + amount).coerceAtLeast(0)
                    mobStats.iframes = newValue
                    player.sendMessage("§a§l✓ §aSet §e$statKey §ato §f${newValue}§ams")
                    true
                }
                "attackSpeed" -> {
                    val newValue = (mobStats.attackSpeed + amount).coerceAtLeast(1)
                    mobStats.attackSpeed = newValue
                    player.sendMessage("§a§l✓ §aSet §e$statKey §ato §f$newValue§a/s")
                    true
                }
                else -> {
                    val currentValue = mobStats.getValue(statKey)
                    val newValue = currentValue + amount

                    try {
                        mobStats.setValue(statKey, newValue)
                        player.sendMessage("§a§l✓ §aSet §e$statKey §ato §f$newValue")
                        true
                    } catch (e: Exception) {
                        player.sendMessage("§c§l✖ §cCouldn't modify that stat!")
                        false
                    }
                }
            }

            if (success) {
                when {
                    title.contains("Combat Stats") -> DummyGUI.openCombatStats(player, mobStats)
                    title.contains("Elemental Damage") -> DummyGUI.openElementalDamage(player, mobStats)
                    title.contains("Resistances") -> DummyGUI.openResistances(player, mobStats)
                    title.contains("Misc Settings") -> DummyGUI.openMiscSettings(player, mobStats)
                }
            }
        }
    }
}