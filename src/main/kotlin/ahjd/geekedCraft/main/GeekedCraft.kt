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
import ahjd.geekedCraft.human.util.HumanHealthDisplay
import ahjd.geekedCraft.human.util.HumanRegenTicker
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
import ahjd.geekedCraft.mob.misc.MobDisplayUpdateTask
import ahjd.geekedCraft.mob.misc.MobHealthRegenTask
import ahjd.geekedCraft.mob.misc.dummy.DummyCombatTracker
import ahjd.geekedCraft.mob.misc.dummy.DummyDPSTest
import ahjd.geekedCraft.util.MSG
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

//TODO: classes -> spells -> subclasses {itemized} trove tye shi

class GeekedCraft : JavaPlugin() {
    private lateinit var playerManager: PlayerManager
    private lateinit var abilityLSN: AbilityLSN

    companion object {
        private lateinit var instance: GeekedCraft

        fun getInstance(): GeekedCraft = instance
    }

    override fun onEnable() {
        // Plugin startup logic
        instance = this

        DatabaseManager.start()

        playerManager = PlayerManager

        // Register all abilities FIRST
        AbilityRegistry.registerDefaults()
        MSG.info("Registered ${AbilityRegistry.getAll().size} abilities")

        // Register listeners (this initializes abilityListener)
        regListeners()
        regCommands()

        MobManager.clear()

        // Load all holograms from database
        HologramManager.loadAll()

        // Clean up orphaned TextDisplay entities
        HologramController.cleanupOrphanedEntities()

        // Spawn all holograms that have valid locations
        HologramController.spawnAll()

        MobAI.initialize(instance)

        HumanRegenTicker.start()
        HumanHealthDisplay.startTicker()
        PlayerLSN().startPlaytimeTracking()
        MobHealthRegenTask.start(this)
        MobDisplayUpdateTask.start(this)
        DummyCombatTracker.start()
        DummyDPSTest.start()
        abilityLSN.startPeriodicAbilities()

        // Config.yml for plugin-wide settings
        saveDefaultConfig()

        // Items.yml for custom items
        if (!dataFolder.exists()) dataFolder.mkdirs()
        val itemsFile = File(dataFolder, "items.yml")
        if (!itemsFile.exists()) {
            saveResource("items.yml", false)
        }

        ItemManager.loadTemplates(dataFolder)

        MSG.info("GeekedCraft RPG Plugin has been enabled!")
    }

    override fun onDisable() {
       // Plugin shutdown logic
        PlayerManager.saveAll()

        // Clear tracked mobs from memory
        MobManager.clear()

        // Despawn all holograms
        HologramController.despawnAll()

        // Clean-up any temp hologram
        TempHologramController.cleanupAll()

        // Save all holograms to database
        HologramManager.saveAll()

        DatabaseManager.shutdown()

        MSG.info("GeekedCraft RPG Plugin has been disabled!")
    }


    private fun regListeners() {
        val dummyGUIListener = DummyGUILSN()
        abilityLSN = AbilityLSN(this)

        listOf(
            PlayerLSN(),
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
       // Create base command instances
       val databaseCmd = DBCMD()
       val humanCmd = HumanCMD()
       val mobCmd = MobCMD()
       val itemCmd = ItemCMD()

       // Register base commands
       getCommand("db")?.setExecutor(databaseCmd)
       getCommand("hm")?.setExecutor(humanCmd)
       getCommand("mob")?.setExecutor(mobCmd)
       getCommand("it")?.setExecutor(itemCmd)
    }

    //Helper method for outer classes
    fun scheduleRepeatingTask(runnable: Runnable, delay: Long, period: Long): Int {
        return server.scheduler.runTaskTimer(this, runnable, delay, period).taskId
    }
}
