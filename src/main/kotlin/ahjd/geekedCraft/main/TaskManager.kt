package ahjd.geekedCraft.main

import ahjd.geekedCraft.human.util.HumanHealthDisplay
import ahjd.geekedCraft.human.util.HumanRegenTicker
import ahjd.geekedCraft.mob.util.MobDisplayUpdateTask
import ahjd.geekedCraft.mob.util.MobHealthRegenTask
import org.bukkit.Bukkit

/**
 * Centralized task management system
 * Handles starting, stopping, and tracking all plugin tasks
 */
object TaskManager {

    private val tasks = mutableMapOf<String, Int>()

    /**
     * Start all plugin tasks
     */
    fun startAll(plugin: GeekedCraft, abilityLSN: Any? = null, playerLSN: Any? = null) {
        val startTime = System.currentTimeMillis()
        var successCount = 0

        // Player-related tasks
        runCatching {
            registerTask("human_regen", HumanRegenTicker.start())
            registerTask("human_health_display", HumanHealthDisplay.startTicker())
            successCount += 2
        }.onFailure {
            plugin.logger.severe("Failed to start player tasks: ${it.message}")
        }

        // Mob-related tasks
        runCatching {
            registerTask("mob_health_regen", MobHealthRegenTask.start(plugin))
            registerTask("mob_display_update", MobDisplayUpdateTask.start(plugin))
            successCount += 2
        }.onFailure {
            plugin.logger.severe("Failed to start mob tasks: ${it.message}")
        }

        // Ability system tasks (requires AbilityLSN instance)
        if (abilityLSN != null) {
            runCatching {
                val method = abilityLSN::class.java.getDeclaredMethod("startPeriodicAbilities")
                val taskId = method.invoke(abilityLSN) as? Int
                if (taskId != null) registerTask("periodic_abilities", taskId)
                successCount++
            }.onFailure {
                plugin.logger.severe("Failed to start ability tasks: ${it.message}")
            }
        }

        // Playtime tracking (requires PlayerLSN instance)
        if (playerLSN != null) {
            runCatching {
                val method = playerLSN::class.java.getDeclaredMethod("startPlaytimeTracking")
                val taskId = method.invoke(playerLSN) as? Int
                if (taskId != null) registerTask("playtime_tracking", taskId)
                successCount++
            }.onFailure {
                plugin.logger.severe("Failed to start playtime tracking: ${it.message}")
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        plugin.logger.info("Started $successCount tasks in ${elapsed}ms")
    }

    /**
     * Stop all running tasks
     */
    fun stopAll() {
        val count = tasks.size

        // Stop object-based tasks properly
        MobHealthRegenTask.stop()
        MobDisplayUpdateTask.stop()

        // Cancel remaining tasks
        tasks.values.forEach { taskId ->
            runCatching {
                Bukkit.getScheduler().cancelTask(taskId)
            }
        }

        tasks.clear()
        GeekedCraft.getInstance().logger.info("Stopped $count tasks")
    }

    /**
     * Stop a specific task by name
     */
    fun stopTask(name: String) {
        tasks[name]?.let { taskId ->
            Bukkit.getScheduler().cancelTask(taskId)
            tasks.remove(name)
            GeekedCraft.getInstance().logger.info("Stopped task: $name")
        }
    }

    /**
     * Restart all tasks
     */
    fun restart(plugin: GeekedCraft, abilityLSN: Any? = null, playerLSN: Any? = null) {
        stopAll()
        startAll(plugin, abilityLSN, playerLSN)
    }

    /**
     * Check if a task is running
     */
    fun isRunning(name: String): Boolean = tasks.containsKey(name)

    /**
     * Get list of all running tasks
     */
    fun getRunningTasks(): List<String> = tasks.keys.toList()

    /**
     * Register a task with tracking
     */
    private fun registerTask(name: String, taskId: Int) {
        if (tasks.containsKey(name)) {
            Bukkit.getScheduler().cancelTask(tasks[name]!!)
            GeekedCraft.getInstance().logger.warning("Task '$name' was already running, restarted")
        }
        tasks[name] = taskId
    }
}