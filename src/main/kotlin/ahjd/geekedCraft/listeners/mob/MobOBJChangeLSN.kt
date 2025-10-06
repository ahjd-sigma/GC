package ahjd.geekedCraft.listeners.mob

import ahjd.geekedCraft.events.mob.MobOBJChangeEvent
import ahjd.geekedCraft.mob.MobManager
import ahjd.geekedCraft.mob.ai.MobAI
import ahjd.geekedCraft.mob.stats.ChangeType
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

class MobOBJChangeLSN : Listener {

    @EventHandler
    fun onMobOBJChange(event: MobOBJChangeEvent) {
        val mobStats = event.mobOBJ

        // Check what type of change this is
        when (event.changeType) {
            ChangeType.AI_PERSONALITY -> {
                // Change AI Personality
                val entity = Bukkit.getEntity(UUID.fromString(mobStats.uuid)) as? Mob ?: return
                MobAI.updateMobAI(entity, mobStats.aiType)
            }

            ChangeType.KIND -> {
                // Get location before removing
                val oldEntity = Bukkit.getEntity(UUID.fromString(mobStats.uuid)) as? LivingEntity ?: return
                val spawnLocation = oldEntity.location

                // Remove old entity & object
                MobManager.remove(mobStats.uuid)
                oldEntity.remove()

                // Spawn new mob — spawnMob already sets UUID and adds to MobManager
                MobManager.spawnMob(mobStats, spawnLocation)
            }

            // Add more change types as needed
        }
    }
}