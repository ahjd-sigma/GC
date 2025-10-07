package ahjd.geekedCraft.human.util

import ahjd.geekedCraft.effect.Effect
import ahjd.geekedCraft.human.HumanStats
import ahjd.geekedCraft.main.GeekedCraft
import java.util.*

object HumanEffectTask {
    // Track ONE task per player that handles ALL their effects
    private val runningTasks = mutableMapOf<UUID, Int>()

    // Track tick counters for each player to handle different intervals
    private val tickCounters = mutableMapOf<UUID, Long>()

    fun start(plugin: GeekedCraft, stats: HumanStats, effect: Effect) {
        val playerId = UUID.fromString(stats.uuid)

        // If no task exists for this player, create one
        if (!runningTasks.containsKey(playerId)) {
            // Initialize tick counter
            tickCounters[playerId] = 0L

            val taskId = plugin.scheduleRepeatingTask({
                val currentTick = tickCounters[playerId] ?: 0L
                tickCounters[playerId] = currentTick + 1

                // Tick ALL active periodic effects that should fire this tick
                stats.activeEffects
                    .filter { it.periodic }
                    .forEach { activeEffect ->
                        // Check if this effect should tick on this counter
                        if (currentTick % activeEffect.tickInterval == 0L) {
                            activeEffect.onTick(stats)
                        }
                    }

                // If no more periodic effects, stop the task
                if (stats.activeEffects.none { it.periodic }) {
                    stop(stats)
                }
            }, 0L, 1L) // Start immediately, run every tick

            runningTasks[playerId] = taskId
        }
        // If task already exists, it will automatically handle the new effect
    }

    fun stop(stats: HumanStats) {
        val playerId = UUID.fromString(stats.uuid)
        runningTasks.remove(playerId)?.let {
            GeekedCraft.getInstance().cancelTask(it)
        }
        tickCounters.remove(playerId)
    }

    /**
     * Force stop a specific player's effect task (useful for cleanup)
     */
    fun stopPlayer(playerId: UUID) {
        runningTasks.remove(playerId)?.let {
            GeekedCraft.getInstance().cancelTask(it)
        }
        tickCounters.remove(playerId)
    }

    /**
     * Check if a player has an active effect task running
     */
    fun isRunning(playerId: UUID): Boolean {
        return runningTasks.containsKey(playerId)
    }

    /**
     * Get current tick count for a player (useful for debugging)
     */
    fun getTickCount(playerId: UUID): Long {
        return tickCounters[playerId] ?: 0L
    }

    /**
     * Stop all effect tasks (called on plugin disable)
     */
    fun stopAll() {
        runningTasks.values.forEach { taskId ->
            GeekedCraft.getInstance().cancelTask(taskId)
        }
        runningTasks.clear()
        tickCounters.clear()
    }
}