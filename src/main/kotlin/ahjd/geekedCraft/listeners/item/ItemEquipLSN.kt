package ahjd.geekedCraft.listeners.item

import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.human.HumanStats
import ahjd.geekedCraft.item.ItemFactory
import ahjd.geekedCraft.item.util.ItemSlotType
import ahjd.geekedCraft.main.GeekedCraft
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerChangedMainHandEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

/**
 * Handles equipment changes and applies stats atomically
 * Thread-safe with proper synchronization via Bukkit scheduler
 */
class ItemEquipLSN(private val plugin: GeekedCraft) : Listener {

    companion object {
        // Shortbow cooldown tracking
        private val shortbowLastShot = ConcurrentHashMap<UUID, Long>()
        private const val SHORTBOW_BASE_COOLDOWN = 400L

        /**
         * Update crossbow quick charge based on player's attack speed
         * Called automatically when attack speed changes
         */
        fun updateCrossbow(player: Player) {
            val item = player.inventory.itemInMainHand
            if (item.type != Material.CROSSBOW) return

            val atkspd = HumanStatManager.get(player.uniqueId.toString()).attackspeed
            val level = calculateQuickChargeLevel(atkspd)

            cleanCrossbowEnchants(item)

            if (level > 0) {
                applyCrossbowEnchant(item, level)
            }
        }

        private fun calculateQuickChargeLevel(attackSpeed: Int): Int {
            return when {
                attackSpeed < 50 -> 0
                attackSpeed >= 200 -> 10
                else -> ((attackSpeed - 50) / 50) + 1
            }
        }

        private fun applyCrossbowEnchant(item: ItemStack, level: Int) {
            item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, level)
            val meta = item.itemMeta ?: return
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_STORED_ENCHANTS)
            item.itemMeta = meta
        }

        private fun cleanCrossbowEnchants(item: ItemStack?) {
            if (item?.type != Material.CROSSBOW) return
            item.removeEnchantment(Enchantment.QUICK_CHARGE)
        }
    }

    // ==================== EVENT HANDLERS: EQUIPMENT CHANGES ====================

    @EventHandler
    fun onPlayerArmorChange(event: PlayerArmorChangeEvent) {
        scheduleEquipmentRecalculation(event.player)
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        scheduleEquipmentRecalculation(event.player)
    }

    @EventHandler
    fun onPlayerItemChange(event: PlayerChangedMainHandEvent) {
        scheduleEquipmentRecalculation(event.player)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        (event.whoClicked as? Player)?.let { scheduleEquipmentRecalculation(it) }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        (event.whoClicked as? Player)?.let { scheduleEquipmentRecalculation(it) }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        scheduleEquipmentRecalculation(event.player)
    }

    // ==================== EVENT HANDLERS: CROSSBOW ====================

    @EventHandler
    fun onCrossbowLoad(event: EntityLoadCrossbowEvent) {
        (event.entity as? Player)?.let { player ->
            plugin.server.scheduler.runTask(plugin) { _ ->
                updateCrossbow(player)
            }
        }
    }

    @EventHandler
    fun onCrossbowShoot(event: EntityShootBowEvent) {
        if (event.entity !is Player) return
        if (event.bow?.type != Material.CROSSBOW) return

        @Suppress("DEPRECATION")
        event.setConsumeItem(false)
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        event.entity.remove()
    }

    // ==================== EVENT HANDLERS: SHORTBOW ====================

    @EventHandler
    fun onShortbowShoot(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (!isShortbowRightClick(item, event.action)) return
        if (!canShootShortbow(player)) return

        fireShortbow(player)
    }

    // ==================== EQUIPMENT RECALCULATION (ATOMIC) ====================

    /**
     * Schedule equipment recalculation on main thread (thread-safe)
     */
    private fun scheduleEquipmentRecalculation(player: Player) {
        plugin.server.scheduler.runTask(plugin) { _ ->
            recalculateAllEquipment(player)
        }
    }

    /**
     * ATOMIC RECALCULATION: Update ALL equipment slots
     * Each slot is completely replaced, never accumulated
     */
    private fun recalculateAllEquipment(player: Player) {
        val humanStats = HumanStatManager.get(player.uniqueId.toString())

        // Armor
        updateSlot(humanStats, HumanStats.EquipmentSlot.HELMET, player.inventory.helmet, ItemSlotType.ARMOR)
        updateSlot(humanStats, HumanStats.EquipmentSlot.CHESTPLATE, player.inventory.chestplate, ItemSlotType.ARMOR)
        updateSlot(humanStats, HumanStats.EquipmentSlot.LEGGINGS, player.inventory.leggings, ItemSlotType.ARMOR)
        updateSlot(humanStats, HumanStats.EquipmentSlot.BOOTS, player.inventory.boots, ItemSlotType.ARMOR)

        // Hands
        updateSlot(humanStats, HumanStats.EquipmentSlot.MAIN_HAND, player.inventory.itemInMainHand, ItemSlotType.MAIN_HAND)
        updateSlot(humanStats, HumanStats.EquipmentSlot.OFF_HAND, player.inventory.itemInOffHand, ItemSlotType.MAIN_HAND)

        // Arrow
        updateSlot(humanStats, HumanStats.EquipmentSlot.ARROW, player.inventory.getItem(9), ItemSlotType.ARROW)

        updateCrossbow(player)
    }

    // ==================== SLOT UPDATE HELPERS ====================

    private fun updateSlot(humanStats: HumanStats, slot: HumanStats.EquipmentSlot, item: ItemStack?, requiredSlotType: ItemSlotType) {
        if (item != null && item.type != Material.AIR &&
            ItemFactory.isCustomItem(item) &&
            ItemFactory.getSlotType(item) == requiredSlotType) {
            humanStats.setSlotBonuses(slot, ItemFactory.getStats(item))
        } else {
            humanStats.clearSlot(slot)
        }
    }

    // ==================== SHORTBOW MECHANICS ====================

    private fun isShortbowRightClick(item: ItemStack, action: Action): Boolean {
        return item.type == Material.CARROT_ON_A_STICK &&
                ItemFactory.isCustomItem(item) &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
    }

    private fun canShootShortbow(player: Player): Boolean {
        val cooldown = calculateShortbowCooldown(player)
        val now = System.currentTimeMillis()
        val lastShot = shortbowLastShot[player.uniqueId] ?: 0L
        return now - lastShot >= cooldown
    }

    private fun calculateShortbowCooldown(player: Player): Long {
        val atkspd = HumanStatManager.get(player.uniqueId.toString()).attackspeed
        val multiplier = 2.0.pow(-atkspd / 200.0)
        return (SHORTBOW_BASE_COOLDOWN * multiplier).toLong()
    }

    private fun fireShortbow(player: Player) {
        shortbowLastShot[player.uniqueId] = System.currentTimeMillis()

        val spawnLocation = player.eyeLocation.add(player.location.direction.multiply(1.5))
        val arrow = player.world.spawnArrow(
            spawnLocation,
            player.location.direction,
            2f,
            0f
        )
        arrow.shooter = player
    }
}

/**
 * INVENTORY SLOT REFERENCE:
 *
 * Slot 9 = Top-left of main inventory (above hotbar)
 * Slots 0-8 = Hotbar (bottom row)
 * Slots 9-35 = Main inventory (3 rows above hotbar)
 * Slots 36-39 = Armor (boots, leggings, chestplate, helmet)
 * Slot 40 = Offhand
 */