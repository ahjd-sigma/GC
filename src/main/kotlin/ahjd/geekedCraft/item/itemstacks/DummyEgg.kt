package ahjd.geekedCraft.item.itemstacks

import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.MobManager
import ahjd.geekedCraft.mob.stats.MobStats
import ahjd.geekedCraft.mob.stats.MobEnums
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object DummyEgg {

    private val plugin = GeekedCraft.getInstance()

    fun createEgg(): ItemStack {
        val egg = ItemStack(Material.ZOMBIE_SPAWN_EGG)
        val meta = egg.itemMeta!!

        meta.setDisplayName("§6Training Dummy Egg")
        meta.lore = listOf(
            "§7Right-click to spawn a training dummy",
            "§7Dummy despawns after 2 minutes of inactivity",
            "§7and drops this egg back"
        )

        val key = NamespacedKey(plugin, "dummy_egg")
        meta.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, true)

        egg.itemMeta = meta
        return egg
    }

    fun isDummyEgg(item: ItemStack?): Boolean {
        if (item == null || item.type != Material.ZOMBIE_SPAWN_EGG) return false
        val key = NamespacedKey(plugin, "dummy_egg")
        return item.itemMeta?.persistentDataContainer?.has(key, PersistentDataType.BOOLEAN) == true
    }

    fun spawnDummy(location: org.bukkit.Location): org.bukkit.entity.LivingEntity {
        val mobStats = MobStats(
            uuid = "temp",
            name = "Training Dummy",
            mobKind = MobEnums.Kind.ZOMBIE,
            mobType = MobEnums.Type.NORMAL,
            aiType = MobEnums.AI.STATIONARY,
            spawnPoint = location,
            protectionRange = 15.0
        )

        return MobManager.spawnDummy(mobStats, location)
    }
}