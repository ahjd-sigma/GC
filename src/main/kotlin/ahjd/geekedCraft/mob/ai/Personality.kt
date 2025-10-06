package ahjd.geekedCraft.mob.ai

import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.stats.MobEnums
import com.destroystokyo.paper.entity.ai.Goal
import org.bukkit.Bukkit
import org.bukkit.entity.Mob

data class Personality(val name: String, val goals: List<Pair<Goal<Mob>, Int>>) {
    fun applyToMob(mob: Mob) {
        Bukkit.getMobGoals().apply {
            removeAllGoals(mob)
            goals.forEach { (goal, priority) -> addGoal(mob, priority, goal) }
        }
    }

    companion object {
        fun create(mob: Mob, ai: MobEnums.AI, p: GeekedCraft) = when (ai) {
            MobEnums.AI.AGGRESSIVE -> Personality("Aggressive", listOf(
                AttackNearbyPlayerGoal(mob, p, 16.0) to 1,
                WanderGoal(mob, p, 5.0) to 2,
                IdleGoal(mob, p) to 3
            ))
            MobEnums.AI.NEUTRAL -> Personality("Neutral", listOf(
                SelfDefenseGoal(mob, p) to 1,
                WanderGoal(mob, p, 5.0) to 2,
                IdleGoal(mob, p) to 3
            ))
            MobEnums.AI.STATIONARY -> Personality("Stationary", listOf(
                StationaryGoal(mob, p, 4.0) to 1
            ))
            MobEnums.AI.PASSIVE -> Personality("Passive", listOf(
                WanderGoal(mob, p, 8.0) to 1,
                IdleGoal(mob, p) to 2
            ))
            MobEnums.AI.GUARDIAN -> Personality("Guardian", listOf(
                GuardGoal(mob, p) to 1,
                PatrolGoal(mob, p) to 2,
                IdleGoal(mob, p) to 3
            ))
        }
    }
}