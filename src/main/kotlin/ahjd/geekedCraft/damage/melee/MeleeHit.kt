package ahjd.geekedCraft.damage.melee

import ahjd.geekedCraft.damage.DealDamage
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.MobManager
import net.minecraft.network.protocol.game.ClientboundCooldownPacket
import net.minecraft.resources.ResourceLocation
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object MeleeHit {
    private val plugin = GeekedCraft.getInstance()
    private val keyLastUse = NamespacedKey(plugin, "last_use_time")
    private val playerLastAttackTime = mutableMapOf<String, Pair<Long, Long>>()

    fun onMelee(attacker: Player) {
        val mainHand = attacker.inventory.itemInMainHand
        val uuid = attacker.uniqueId.toString()
        val stats = HumanStatManager.get(uuid)

        val attackSpeed = stats.getValue("attackspeed") ?: 0

        val meta = mainHand.itemMeta
        val baseCooldownSeconds = meta?.getUseCooldown()?.getCooldownSeconds() ?: 0f
        val playerCooldownMs = if (baseCooldownSeconds > 0f) {
            val finalCooldown = (baseCooldownSeconds * (1.0f - attackSpeed / 100.0f)).coerceAtLeast(0.1f)
            (finalCooldown * 1000).toLong()
        } else {
            500L
        }

        val now = System.currentTimeMillis()
        val (lastAttack, lastCooldown) = playerLastAttackTime[uuid] ?: Pair(0L, 0L)
        if (now - lastAttack < lastCooldown) return

        playerLastAttackTime[uuid] = Pair(now, playerCooldownMs)

        val sweep = stats.getValue("sweep")?.toDouble() ?: 60.0
        val range = stats.getValue("range")?.toDouble() ?: 3.0

        // Show visual feedback
        MeleeVisuals.showConeVisual(attacker, sweep, range)

        // Get targets in cone
        val targets = ConeDetection.getTargetsInCone(attacker, sweep, range)

        // Apply damage to valid targets
        for (target in targets) {
            val isValidTarget = when (target) {
                is Player -> true
                else -> MobManager.has(target.uniqueId.toString())
            }
            if (!isValidTarget) continue

            DealDamage.applyDamage(
                attacker = attacker,
                target = target,
                type = DamageType.MELEE
            )

            MeleeVisuals.showHitParticles(target)
        }

        applyCooldown(mainHand, attackSpeed, attacker)
    }

    private fun applyCooldown(item: ItemStack, attackSpeed: Int, player: Player) {
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val cdComp = meta.getUseCooldown()
        val baseCooldown = cdComp.getCooldownSeconds()
        val finalCooldown = (baseCooldown * (1.0f - attackSpeed / 100.0f)).coerceAtLeast(0.1f)
        val now = System.currentTimeMillis()

        pdc.set(keyLastUse, PersistentDataType.LONG, now)
        item.itemMeta = meta

        val cooldownTicks = (finalCooldown * 20).toInt()
        sendItemCooldownPacket(player, item, cooldownTicks)
    }

    private fun sendItemCooldownPacket(player: Player, item: ItemStack, ticks: Int) {
        val nmsPlayer = (player as CraftPlayer).handle
        val resourceLocation = getItemResourceLocation(item)
        val packet = ClientboundCooldownPacket(resourceLocation, ticks)
        nmsPlayer.connection.send(packet)
    }

    private fun getItemResourceLocation(item: ItemStack): ResourceLocation {
        val path = item.type.key.key
        return ResourceLocation.withDefaultNamespace(path)
    }
}