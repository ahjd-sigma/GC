package ahjd.geekedCraft.main

import ahjd.geekedCraft.commands.database.DBCMD
import ahjd.geekedCraft.commands.human.HumanCMD
import ahjd.geekedCraft.commands.item.ItemCMD
import ahjd.geekedCraft.commands.mob.MobCMD
import ahjd.geekedCraft.database.DatabaseManager
import ahjd.geekedCraft.database.managers.HologramManager
import ahjd.geekedCraft.database.managers.PlayerManager
import ahjd.geekedCraft.hologram.HologramController
import ahjd.geekedCraft.hologram.TempHologramController
import ahjd.geekedCraft.item.ItemManager
import ahjd.geekedCraft.item.ability.AbilityRegistry
import ahjd.geekedCraft.listeners.damage.DamageCatcherLSN
import ahjd.geekedCraft.listeners.human.HumanDeathLSN
import ahjd.geekedCraft.listeners.human.PlayerLSN
import ahjd.geekedCraft.listeners.item.*
import ahjd.geekedCraft.listeners.mob.MobDeathLSN
import ahjd.geekedCraft.listeners.mob.MobOBJChangeLSN
import ahjd.geekedCraft.listeners.mob.MobSpeedLSN
import ahjd.geekedCraft.mob.MobManager
import ahjd.geekedCraft.mob.ai.MobAI
import ahjd.geekedCraft.util.MSG
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

//TODO: last thing frfr -> effects on mobs and players, set kinds of effects like abilities, ways to inflict them like consumables (effect like ability so the normal stats displayed also apply as an equipment)
//TODO: also show in compass breakdown // DOUBLE CHECK DAMAGE CALC's ELEMENTS IT SHOULD BE FINE THO
//TODO: classes -> spells -> subclasses {itemized} trove tye shi

class GeekedCraft : JavaPlugin() {
    private lateinit var playerManager: PlayerManager
    private lateinit var abilityLSN: AbilityLSN
    private lateinit var playerLSN: PlayerLSN

    companion object {
        private lateinit var instance: GeekedCraft

        fun getInstance(): GeekedCraft = instance
    }

    override fun onEnable() {
        instance = this
        DatabaseManager.start()
        playerManager = PlayerManager

        // Register all abilities
        AbilityRegistry.registerDefaults()

        // Register listeners (this initializes abilityLSN and playerLSN)
        regListeners()
        regCommands()

        MobManager.clear()

        // Load and spawn holograms
        HologramManager.loadAll()
        HologramController.cleanupOrphanedEntities()
        HologramController.spawnAll()

        // Initialize mob AI
        MobAI.initialize(instance)

        // Start all tasks - ONE LINE!
        TaskManager.startAll(this, abilityLSN, playerLSN)

        // Config setup
        saveDefaultConfig()

        // Items.yml setup
        if (!dataFolder.exists()) dataFolder.mkdirs()
        val itemsFile = File(dataFolder, "items.yml")
        if (!itemsFile.exists()) {
            saveResource("items.yml", false)
        }

        ItemManager.loadTemplates(dataFolder)

        MSG.info("GeekedCraft RPG Plugin has been enabled!")
    }


    override fun onDisable() {
        PlayerManager.saveAll()
        MobManager.clear()
        HologramController.despawnAll()

        // Stop all tasks - ONE LINE!
        TaskManager.stopAll()

        TempHologramController.cleanupAll()
        HologramManager.saveAll()
        DatabaseManager.shutdown()

        MSG.info("GeekedCraft RPG Plugin has been disabled!")
    }

    private fun regListeners() {
        val dummyGUIListener = DummyGUILSN()
        abilityLSN = AbilityLSN(this)
        playerLSN = PlayerLSN()
        listOf(
            playerLSN,
            HumanDeathLSN(),
            DamageCatcherLSN(),
            MobDeathLSN(),
            MobSpeedLSN(),
            MobOBJChangeLSN(),
            ItemEquipLSN(this),
            abilityLSN,
            CompassLSN(this),
            dummyGUIListener,
            DummyInteractLSN(dummyGUIListener),
            DummyEggLSN()
        ).forEach { server.pluginManager.registerEvents(it, this) }
    }

    private fun regCommands() {
        val databaseCmd = DBCMD()
        val humanCmd = HumanCMD()
        val mobCmd = MobCMD()
        val itemCmd = ItemCMD()

        getCommand("db")?.setExecutor(databaseCmd)
        getCommand("hm")?.setExecutor(humanCmd)
        getCommand("mob")?.setExecutor(mobCmd)
        getCommand("it")?.setExecutor(itemCmd)
    }

    // ==================== TASK HELPERS ====================

    /**
     * Schedule a repeating task
     * @return task ID
     */
    fun scheduleRepeatingTask(runnable: Runnable, delay: Long, period: Long): Int {
        return server.scheduler.runTaskTimer(this, runnable, delay, period).taskId
    }

    /**
     * Schedule a one-time delayed task
     * @return task ID
     */
    fun scheduleDelayedTask(runnable: Runnable, delay: Long): Int {
        return server.scheduler.runTaskLater(this, runnable, delay).taskId
    }

    /**
     * Cancel a task by ID
     */
    fun cancelTask(taskId: Int) {
        server.scheduler.cancelTask(taskId)
    }
}