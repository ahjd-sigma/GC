package ahjd.geekedCraft.listeners.item

import ahjd.geekedCraft.item.ItemFactory
import ahjd.geekedCraft.item.ItemManager
import ahjd.geekedCraft.item.ability.AbilityContext
import ahjd.geekedCraft.item.ability.AbilityRegistry
import ahjd.geekedCraft.item.ability.AbilityTrigger
import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.util.MSG
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class AbilityLSN(private val plugin: GeekedCraft) : Listener {

    // Track ability cooldowns per player per ability
    private val cooldowns = mutableMapOf<UUID, MutableMap<String, Long>>()

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        val weapon = player.inventory.itemInMainHand

        if (weapon.type != Material.AIR) {
            triggerAbility(player, weapon, AbilityTrigger.ON_HIT, AbilityContext(damageEvent = event))
        }
    }

    @EventHandler
    fun onPlayerDamaged(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return

        // Check all equipped armor pieces
        listOfNotNull(
            player.inventory.helmet,
            player.inventory.chestplate,
            player.inventory.leggings,
            player.inventory.boots
        ).forEach { armor ->
            if (armor.type != Material.AIR) {
                triggerAbility(player, armor, AbilityTrigger.ON_DAMAGED, AbilityContext(damageEvent = event))
                triggerAbility(player, armor, AbilityTrigger.WHILE_WORN, AbilityContext(damageEvent = event))
            }
        }
    }

    @EventHandler
    fun onEntityKill(event: EntityDeathEvent) {
        val player = event.entity.killer ?: return
        val weapon = player.inventory.itemInMainHand

        if (weapon.type != Material.AIR) {
            triggerAbility(player, weapon, AbilityTrigger.ON_KILL, AbilityContext())
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity

        // Check all equipped items
        player.inventory.armorContents.filterNotNull().forEach { armor ->
            if (armor.type != Material.AIR) {
                triggerAbility(player, armor, AbilityTrigger.ON_DEATH, AbilityContext())
            }
        }

        val weapon = player.inventory.itemInMainHand
        if (weapon.type != Material.AIR) {
            triggerAbility(player, weapon, AbilityTrigger.ON_DEATH, AbilityContext())
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        if (item.type != Material.AIR) {
            triggerAbility(player, item, AbilityTrigger.ON_HIT, AbilityContext(interactEvent = event))
        }
    }

    private fun triggerAbility(player: Player, item: ItemStack, trigger: AbilityTrigger, context: AbilityContext) {
        if (item.type == Material.AIR || !ItemFactory.isCustomItem(item)) return

        val templateId = ItemFactory.getTemplateId(item) ?: return
        val template = ItemManager.getTemplate(templateId) ?: return
        val abilityId = template.abilityId ?: return
        val ability = AbilityRegistry.get(abilityId) ?: return

        if (ability.trigger != trigger || !canUseAbility(player.uniqueId, abilityId, ability.cooldown)) return

        runCatching {
            ability.execute(player, context)
            setCooldown(player.uniqueId, abilityId, ability.cooldown)
        }.onFailure {
            MSG.warn("Error executing ability $abilityId: ${it.message}")
        }
    }

    private fun canUseAbility(playerId: UUID, abilityId: String, cooldownMs: Long): Boolean {
        if (cooldownMs == 0L) return true

        val playerCooldowns = cooldowns.getOrPut(playerId) { mutableMapOf() }
        val lastUsed = playerCooldowns[abilityId] ?: 0L
        val now = System.currentTimeMillis()

        return (now - lastUsed) >= cooldownMs
    }

    private fun setCooldown(playerId: UUID, abilityId: String, cooldownMs: Long) {
        if (cooldownMs == 0L) return

        val playerCooldowns = cooldowns.getOrPut(playerId) { mutableMapOf() }
        playerCooldowns[abilityId] = System.currentTimeMillis()
    }

    fun startPeriodicAbilities(): Int {
        return plugin.scheduleRepeatingTask({
            plugin.server.onlinePlayers.forEach { player ->
                try {
                    checkPeriodicAbilities(player)
                } catch (e: Exception) {
                    MSG.warn("Error checking periodic abilities for ${player.name}: ${e.message}")
                }
            }
        }, 0L, 20L)
    }

    private fun checkPeriodicAbilities(player: Player) {
        // Check armor
        player.inventory.armorContents.filterNotNull().forEach { armor ->
            if (armor.type != Material.AIR) {
                checkAndTriggerPeriodic(player, armor)
            }
        }

        // Check main hand
        val mainHand = player.inventory.itemInMainHand
        if (mainHand.type != Material.AIR) {
            checkAndTriggerPeriodic(player, mainHand)
        }

        // Check off hand
        val offHand = player.inventory.itemInOffHand
        if (offHand.type != Material.AIR) {
            checkAndTriggerPeriodic(player, offHand)
        }
    }

    private fun checkAndTriggerPeriodic(player: Player, item: ItemStack) {
        if (item.type == Material.AIR || !ItemFactory.isCustomItem(item)) return

        val templateId = ItemFactory.getTemplateId(item) ?: return
        val abilityId = ItemManager.getTemplate(templateId)?.abilityId ?: return
        val ability = AbilityRegistry.get(abilityId) ?: return

        if (ability.trigger == AbilityTrigger.PERIODIC && canUseAbility(player.uniqueId, abilityId, ability.cooldown)) {
            runCatching {
                ability.execute(player, AbilityContext())
                setCooldown(player.uniqueId, abilityId, ability.cooldown)
            }.onFailure { MSG.warn("Error executing periodic ability $abilityId: ${it.message}") }
        }
    }
}