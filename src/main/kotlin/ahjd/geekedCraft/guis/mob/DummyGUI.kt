package ahjd.geekedCraft.guis.mob

import ahjd.geekedCraft.mob.stats.MobStats
import ahjd.geekedCraft.mob.util.dummy.DummyCombatTracker
import ahjd.geekedCraft.mob.util.dummy.DummyDPSTest
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object DummyGUI {

    fun openMainMenu(player: Player, mobStats: MobStats) {
        val inv = Bukkit.createInventory(null, 54, "§8Training Dummy §7- §6Main Menu")

        fillPremiumBorders(inv)

        // Combat Stats Section (Top Left)
        inv.setItem(11, createGlowingItem(Material.NETHERITE_SWORD, "§c§l⚔ Combat Stats",
            "§7Melee, Projectile & Magic damage",
            "§7Configure your attack power",
            "",
            "§e§l» §6Click to open §e«")
        )

        // Elemental Damage Section (Top Right)
        inv.setItem(15, createGlowingItem(Material.BLAZE_POWDER, "§d§l✦ Elemental Damage",
            "§cFire §8• §bWater §8• §eThunder §8• §6Earth",
            "§fAir §8• §0Dark §8• §eLight §8• §7Neutral",
            "",
            "§e§l» §6Click to open §e«")
        )

        // Resistances Section (Bottom Left)
        inv.setItem(29, createGlowingItem(Material.SHIELD, "§9§l⛨ Resistances",
            "§7Elemental & DoT resistance",
            "§7Defense configuration",
            "",
            "§e§l» §6Click to open §e«")
        )

        // Current DPS Stats (Center)
        val dps = DummyCombatTracker.getDPS(mobStats.uuid)
        val totalDmg = DummyCombatTracker.getTotalDamage(mobStats.uuid)
        val inCombat = DummyCombatTracker.isInCombat(mobStats.uuid)

        inv.setItem(22, createGlowingItem(Material.DIAMOND_SWORD, "§e§l⚡ Current Performance",
            "§7DPS: §f%.1f".format(dps),
            "§7Total Damage: §f$totalDmg",
            "§7Status: ${if (inCombat) "§a§lIN COMBAT" else "§7Idle"}",
            "",
            "§c§l» §4Click to reset stats §c«")
        )

        // Misc Settings (Bottom Right)
        inv.setItem(33, createGlowingItem(Material.COMPARATOR, "§7§l⚙ Misc Settings",
            "§7Knockback, I-Frames, AI Type",
            "§7Advanced configuration",
            "",
            "§e§l» §6Click to open §e«")
        )

        // Info Item (Bottom Center)
        inv.setItem(49, createItem(Material.BOOK, "§b§l⚐ Dummy Info",
            "§7Name: §f${mobStats.name}",
            "§7Type: §f${mobStats.mobKind}",
            "§7AI: §f${mobStats.aiType}",
            "",
            "§c§l» §4Click to despawn §c«")
        )

        player.openInventory(inv)
    }

    fun openCombatStats(player: Player, mobStats: MobStats) {
        val inv = Bukkit.createInventory(null, 54, "§8Dummy §7- §c⚔ Combat Stats")
        fillPremiumBorders(inv)

        // Attack Types with stylish spacing
        inv.setItem(11, createStatItem(Material.NETHERITE_SWORD, "§c§l⚔ Melee Raw Damage",
            mobStats.meleeRaw, "meleeRaw")
        )
        inv.setItem(13, createStatItem(Material.BOW, "§a§l➹ Projectile Raw Damage",
            mobStats.projectileRaw, "projectileRaw")
        )
        inv.setItem(15, createStatItem(Material.ENCHANTED_BOOK, "§d§l✦ Magic Raw Damage",
            mobStats.magicRaw, "magicRaw")
        )

        // Back button
        inv.setItem(49, createGlowingItem(Material.ARROW, "§c§l« Back to Main Menu"))

        player.openInventory(inv)
    }

    fun openElementalDamage(player: Player, mobStats: MobStats) {
        val inv = Bukkit.createInventory(null, 54, "§8Dummy §7- §d✦ Elemental Damage")
        fillPremiumBorders(inv)

        // Top row - Primary elements
        inv.setItem(10, createStatItem(Material.LIGHTNING_ROD, "§e§l⚡ Thunder Damage",
            mobStats.thunderRaw, "thunderRaw")
        )
        inv.setItem(11, createStatItem(Material.ROOTED_DIRT, "§6§l⛰ Earth Damage",
            mobStats.earthRaw, "earthRaw")
        )
        inv.setItem(12, createStatItem(Material.HEART_OF_THE_SEA, "§b§l≈ Water Damage",
            mobStats.waterRaw, "waterRaw")
        )
        inv.setItem(13, createStatItem(Material.FIRE_CHARGE, "§c§l✹ Fire Damage",
            mobStats.fireRaw, "fireRaw")
        )

        // Bottom row - Secondary elements
        inv.setItem(19, createStatItem(Material.FEATHER, "§f§l✶ Air Damage",
            mobStats.airRaw, "airRaw")
        )
        inv.setItem(20, createStatItem(Material.OBSIDIAN, "§0§l◆ Dark Damage",
            mobStats.darkRaw, "darkRaw")
        )
        inv.setItem(21, createStatItem(Material.GLOWSTONE, "§e§l☀ Light Damage",
            mobStats.lightRaw, "lightRaw")
        )
        inv.setItem(22, createStatItem(Material.IRON_INGOT, "§7§l◈ Neutral Damage",
            mobStats.neutralRaw, "neutralRaw")
        )

        // Special elemental
        inv.setItem(31, createStatItem(Material.NETHER_STAR, "§d§l✦ Elemental Raw Damage",
            mobStats.elementalRaw, "elementalRaw")
        )

        inv.setItem(49, createGlowingItem(Material.ARROW, "§c§l« Back to Main Menu"))
        player.openInventory(inv)
    }

    fun openResistances(player: Player, mobStats: MobStats) {
        val inv = Bukkit.createInventory(null, 54, "§8Dummy §7- §9⛨ Resistances")
        fillPremiumBorders(inv)

        // Elemental Resistances - Top section
        inv.setItem(10, createStatItem(Material.YELLOW_GLAZED_TERRACOTTA, "§e§l⚡ Thunder Resistance",
            mobStats.thunderResistance, "thunderResistance", "§7Current: §e${mobStats.thunderResistance}%")
        )
        inv.setItem(11, createStatItem(Material.BROWN_GLAZED_TERRACOTTA, "§6§l⛰ Earth Resistance",
            mobStats.earthResistance, "earthResistance", "§7Current: §6${mobStats.earthResistance}%")
        )
        inv.setItem(12, createStatItem(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, "§b§l≈ Water Resistance",
            mobStats.waterResistance, "waterResistance", "§7Current: §b${mobStats.waterResistance}%")
        )
        inv.setItem(13, createStatItem(Material.ORANGE_GLAZED_TERRACOTTA, "§c§l✹ Fire Resistance",
            mobStats.fireResistance, "fireResistance", "§7Current: §c${mobStats.fireResistance}%")
        )

        inv.setItem(19, createStatItem(Material.WHITE_GLAZED_TERRACOTTA, "§f§l✶ Air Resistance",
            mobStats.airResistance, "airResistance", "§7Current: §f${mobStats.airResistance}%")
        )
        inv.setItem(20, createStatItem(Material.BLACK_GLAZED_TERRACOTTA, "§0§l◆ Dark Resistance",
            mobStats.darkResistance, "darkResistance", "§7Current: §8${mobStats.darkResistance}%")
        )
        inv.setItem(21, createStatItem(Material.YELLOW_GLAZED_TERRACOTTA, "§e§l☀ Light Resistance",
            mobStats.lightResistance, "lightResistance", "§7Current: §e${mobStats.lightResistance}%")
        )
        inv.setItem(22, createStatItem(Material.GRAY_GLAZED_TERRACOTTA, "§7§l◈ Neutral Resistance",
            mobStats.neutralResistance, "neutralResistance", "§7Current: §7${mobStats.neutralResistance}%")
        )

        // DoT Resistances - Bottom section
        inv.setItem(28, createStatItem(Material.REDSTONE_BLOCK, "§c§l❤ Bleeding Resistance",
            mobStats.bleedingResistance, "bleedingResistance", "§7Current: §c${mobStats.bleedingResistance}%")
        )
        inv.setItem(30, createStatItem(Material.SLIME_BLOCK, "§2§l☠ Poison Resistance",
            mobStats.poisonResistance, "poisonResistance", "§7Current: §2${mobStats.poisonResistance}%")
        )
        inv.setItem(32, createStatItem(Material.DIAMOND_CHESTPLATE, "§b§l⛨ Defense",
            mobStats.defense, "defense", "§7Current: §b${mobStats.defense}")
        )

        inv.setItem(49, createGlowingItem(Material.ARROW, "§c§l« Back to Main Menu"))
        player.openInventory(inv)
    }

    fun openMiscSettings(player: Player, mobStats: MobStats) {
        val inv = Bukkit.createInventory(null, 54, "§8Dummy §7- §7⚙ Misc Settings")
        fillPremiumBorders(inv)

        inv.setItem(20, createStatItemLong(Material.CLOCK, "§6§l⌚ I-Frames",
            mobStats.iframes, "iframes", "§7Current: §6${mobStats.iframes}ms")
        )

        inv.setItem(22, createStatItem(Material.IRON_SWORD, "§e§l⚡ Attack Speed",
            mobStats.attackSpeed, "attackSpeed",
            "§7Current: §e${mobStats.attackSpeed}/s",
            "§7Attacks per second")
        )

        inv.setItem(24, createDPSTestItem(mobStats))

        inv.setItem(49, createGlowingItem(Material.ARROW, "§c§l« Back to Main Menu"))
        player.openInventory(inv)
    }

    private fun createItem(material: Material, name: String, vararg lore: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta!!
        meta.setDisplayName(name)
        if (lore.isNotEmpty()) {
            meta.lore = lore.toList()
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
        item.itemMeta = meta
        return item
    }

    private fun createGlowingItem(material: Material, name: String, vararg lore: String): ItemStack {
        val item = createItem(material, name, *lore)
        val meta = item.itemMeta!!
        meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        item.itemMeta = meta
        return item
    }

    private fun createStatItem(material: Material, name: String, value: Int, statKey: String, vararg extraLore: String): ItemStack {
        val lore = mutableListOf(
            "§8§m                    ",
            "§7Current Value: §f$value",
            "",
            "§e§l▶ §aLeft-Click §7+1",
            "§e§l▶ §cRight-Click §7-1",
            "§e§l▶ §aShift-Left §7+10",
            "§e§l▶ §cShift-Right §7-10",
            "§8§m                    "
        )

        if (extraLore.isNotEmpty()) {
            lore.add("")
            lore.addAll(extraLore)
        }

        val item = createGlowingItem(material, name, *lore.toTypedArray())
        val meta = item.itemMeta!!
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(ahjd.geekedCraft.main.GeekedCraft.getInstance(), "stat_key"),
            org.bukkit.persistence.PersistentDataType.STRING,
            statKey
        )
        item.itemMeta = meta
        return item
    }

    private fun createStatItemLong(material: Material, name: String, value: Long, statKey: String, vararg extraLore: String): ItemStack {
        val lore = mutableListOf(
            "§8§m                    ",
            "§7Current Value: §f$value",
            "",
            "§e§l▶ §aLeft-Click §7+1",
            "§e§l▶ §cRight-Click §7-1",
            "§e§l▶ §aShift-Left §7+10",
            "§e§l▶ §cShift-Right §7-10",
            "§8§m                    "
        )

        if (extraLore.isNotEmpty()) {
            lore.add("")
            lore.addAll(extraLore)
        }

        val item = createGlowingItem(material, name, *lore.toTypedArray())
        val meta = item.itemMeta!!
        meta.persistentDataContainer.set(
            org.bukkit.NamespacedKey(ahjd.geekedCraft.main.GeekedCraft.getInstance(), "stat_key"),
            org.bukkit.persistence.PersistentDataType.STRING,
            statKey
        )
        item.itemMeta = meta
        return item
    }

    private fun createDPSTestItem(mobStats: MobStats): ItemStack {
        val sessionInfo = DummyDPSTest.getSessionInfo(mobStats.uuid)

        return if (sessionInfo != null && sessionInfo.isActive) {
            // Active session
            createGlowingItem(Material.REDSTONE_BLOCK, "§c§l⚡ DPS Test Active",
                "§7Type: §f${sessionInfo.damageType.name}",
                "§7Duration: §f%.1fs".format(sessionInfo.duration),
                "§7Attacks: §f${sessionInfo.attackCount}",
                "§7Total Damage: §f${sessionInfo.totalDamage}",
                "§7DPS: §f%.2f".format(sessionInfo.dps),
                "",
                "§c§l» §4Click to stop test §c«"
            )
        } else {
            // No active session
            createGlowingItem(Material.EMERALD_BLOCK, "§a§l⚡ Start DPS Test",
                "§7Test dummy's offensive capabilities",
                "§7Dummy will attack you within §e10 blocks",
                "",
                "§e§l» §6Left-Click: §fMelee Test",
                "§e§l» §6Right-Click: §fProjectile Test",
                "§e§l» §6Shift-Click: §fMagic Test"
            )
        }
    }

    private fun fillPremiumBorders(inv: Inventory) {
        val darkGlass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
        val lightGlass = ItemStack(Material.GRAY_STAINED_GLASS_PANE)

        val darkMeta = darkGlass.itemMeta!!
        darkMeta.setDisplayName(" ")
        darkGlass.itemMeta = darkMeta

        val lightMeta = lightGlass.itemMeta!!
        lightMeta.setDisplayName(" ")
        lightGlass.itemMeta = lightMeta

        // Top and bottom rows with alternating pattern
        for (i in 0..8) {
            inv.setItem(i, if (i % 2 == 0) darkGlass else lightGlass)
            inv.setItem(i + 45, if (i % 2 == 0) darkGlass else lightGlass)
        }

        // Left and right columns
        for (i in 1..4) {
            inv.setItem(i * 9, darkGlass)
            inv.setItem(i * 9 + 8, darkGlass)
        }
    }
}