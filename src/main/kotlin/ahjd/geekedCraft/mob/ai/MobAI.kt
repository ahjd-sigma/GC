package ahjd.geekedCraft.mob.ai

import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.stats.MobEnums
import org.bukkit.Bukkit
import org.bukkit.entity.Mob

object MobAI {
    private lateinit var plugin: GeekedCraft

    fun initialize(p: GeekedCraft) { plugin = p }

    fun mobAIInjection(entity: Mob, ai: MobEnums.AI) {
        Bukkit.getMobGoals().removeAllGoals(entity)
        Personality.create(entity, ai, plugin).applyToMob(entity)
    }

    fun updateMobAI(entity: Mob, ai: MobEnums.AI) = mobAIInjection(entity, ai)
}
