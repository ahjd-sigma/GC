package ahjd.geekedCraft.damage.types

import ahjd.geekedCraft.damage.DamageCalculator
import ahjd.geekedCraft.damage.util.DamageBreakdown
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.events.human.HumanDeathEvent
import ahjd.geekedCraft.events.mob.MobDeathEvent
import ahjd.geekedCraft.hologram.util.DamageIndicatorHolo
import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.mob.MobManager
import ahjd.geekedCraft.mob.util.dummy.DummyCombatTracker
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object MagicDamage {

    fun onPlayerToPlayer(attacker: LivingEntity, target: LivingEntity){
        if(attacker is Player && target is Player){
            val attackerStats = HumanStatManager.get(attacker.uniqueId.toString())
            val targetStats = HumanStatManager.get(target.uniqueId.toString())

            val breakdown = DamageCalculator.calculateDamage(attackerStats, targetStats, DamageType.MAGIC)
            applyDamageToPlayer(target, breakdown)
        }
    }

    fun onPlayerToMob(attacker: LivingEntity, target: LivingEntity){
        if(attacker is Player && target !is Player){
            if(!MobManager.has(target.uniqueId.toString())) return

            val attackerStats = HumanStatManager.get(attacker.uniqueId.toString())
            val targetStats = MobManager.get(target.uniqueId.toString())

            val breakdown = DamageCalculator.calculateDamage(attackerStats, targetStats, DamageType.MAGIC)
            applyDamageToMob(target, breakdown)
            if (targetStats.isDummy) {
                DummyCombatTracker.onDummyDamaged(target.uniqueId.toString(), breakdown.totalDamage)
            }
        }
    }

    fun onMobToPlayer(attacker: LivingEntity, target: LivingEntity){
        if(attacker !is Player && target is Player){
            if(!MobManager.has(attacker.uniqueId.toString())) return
            val attackerStats = MobManager.get(attacker.uniqueId.toString())
            val targetStats = HumanStatManager.get(target.uniqueId.toString())

            val breakdown = DamageCalculator.calculateDamage(attackerStats, targetStats, DamageType.MAGIC)
            applyDamageToPlayer(target, breakdown)
        }
    }

    fun onMobToMob(attacker: LivingEntity, target: LivingEntity){
        if(attacker !is Player && target !is Player){
            if(!MobManager.has(attacker.uniqueId.toString())) return
            if(!MobManager.has(target.uniqueId.toString())) return
            val attackerStats = MobManager.get(attacker.uniqueId.toString())
            val targetStats = MobManager.get(target.uniqueId.toString())

            val breakdown = DamageCalculator.calculateDamage(attackerStats, targetStats, DamageType.MAGIC)
            applyDamageToMob(target, breakdown)
        }
    }

    private fun applyDamageToPlayer(target: Player, breakdown: DamageBreakdown) {
        val targetStats = HumanStatManager.get(target.uniqueId.toString())
        targetStats.health -= breakdown.totalDamage
        DamageIndicatorHolo().showDamageIndicatorsBounce(target.location, breakdown)
        if (targetStats.health <= 0) {
            Bukkit.getPluginManager().callEvent(HumanDeathEvent(target))
        }
    }

    private fun applyDamageToMob(target: LivingEntity, breakdown: DamageBreakdown) {
        if (!MobManager.has(target.uniqueId.toString())) return
        val targetStats = MobManager.get(target.uniqueId.toString())
        targetStats.health -= breakdown.totalDamage
        DamageIndicatorHolo().showDamageIndicatorsBounce(target.location, breakdown)
        if (targetStats.health <= 0) {
            Bukkit.getPluginManager().callEvent(MobDeathEvent(target))
        }
    }
}
