package ahjd.geekedCraft.human

import ahjd.geekedCraft.effect.Effect
import ahjd.geekedCraft.events.human.HumanDeathEvent
import ahjd.geekedCraft.human.util.HumanEffectTask
import ahjd.geekedCraft.listeners.human.HumanSpeedLSN
import ahjd.geekedCraft.listeners.item.ItemEquipLSN
import ahjd.geekedCraft.main.GeekedCraft
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

data class HumanStats(
    val uuid: String,

    // ==================== CORE STATS ====================
    var health: Int = 100,
    var maxhealth: Int = 100,
    var healthregen: Int = 5,
    var defense: Int = 0,
    var baseResistance: Int = 0,
    var mana: Int = 100,
    var speed: Int = 100,
    var attackspeed: Int = 0,
    var knockback: Int = 100,
    var knockbackResistance: Int = 0,

    // ==================== COMBAT STATS ====================
    var critChance: Int = 10,
    var critDamage: Int = 50,
    var sweep: Int = 60,
    var range: Int = 3,

    // ==================== ATTACK TYPE STATS ====================
    var meleeRaw: Int = 0,
    var meleePercent: Int = 0,
    var projectileRaw: Int = 0,
    var projectilePercent: Int = 0,
    var magicRaw: Int = 0,
    var magicPercent: Int = 0,

    // ==================== GLOBAL DAMAGE ====================
    var damageRaw: Int = 0,
    var damagePercent: Int = 0,

    // ==================== ELEMENTAL DAMAGE ====================
    var elementalRaw: Int = 0,
    var elementalPercent: Int = 0,

    // Individual Elements
    var thunderRaw: Int = 0,
    var thunderPercent: Int = 0,
    var earthRaw: Int = 0,
    var earthPercent: Int = 0,
    var waterRaw: Int = 0,
    var waterPercent: Int = 0,
    var fireRaw: Int = 0,
    var firePercent: Int = 0,
    var airRaw: Int = 0,
    var airPercent: Int = 0,
    var darkRaw: Int = 0,
    var darkPercent: Int = 0,
    var lightRaw: Int = 0,
    var lightPercent: Int = 0,
    var neutralRaw: Int = 10,
    var neutralPercent: Int = 0,

    // ==================== ELEMENTAL RESISTANCES ====================
    var thunderResistance: Int = 0,
    var earthResistance: Int = 0,
    var waterResistance: Int = 0,
    var fireResistance: Int = 0,
    var airResistance: Int = 0,
    var darkResistance: Int = 0,
    var lightResistance: Int = 0,
    var neutralResistance: Int = 0,

    // ==================== DOT STATS ====================
    var bleeding: Int = 0,
    var poison: Int = 0
) {
    // Equipment slots enum
    enum class EquipmentSlot {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS,
        MAIN_HAND, OFF_HAND,
        ARROW
    }

    // ==================== INTERNAL STATE ====================

    // Store true base values separately from equipment
    private val baseValues = mutableMapOf<String, Int>()

    // Store ONLY current equipment bonuses: Slot -> (StatName -> Value)
    private val equipmentBonuses = mutableMapOf<EquipmentSlot, Map<String, Int>>()

    // Effects - MADE INTERNAL (not private) so HumanEffectTask can access
    internal val activeEffects = mutableListOf<Effect>()

    // Lock for thread safety
    private val lock = Any()

    init {
        // Initialize base values from constructor defaults
        getStatNames().forEach { statName ->
            getValue(statName)?.let { defaultValue ->
                baseValues[statName] = defaultValue
            }
        }
    }

    companion object {
        // Valid ranges for each stat
        private val statRanges = mapOf(
            "health" to (0..100000),
            "maxhealth" to (5..100000),
            "healthregen" to (-10000..10000),
            "defense" to (0..10000),
            "baseResistance" to (0..100),
            "mana" to (0..10000),
            "speed" to (-100..500),
            "attackspeed" to (-200..200),
            "knockback" to (-100..500),
            "knockbackResistance" to (0..100),
            "critChance" to (0..200),
            "critDamage" to (0..10000),
            "sweep" to (0..360),
            "range" to (1..12),
            "meleeRaw" to (0..100000),
            "meleePercent" to (0..10000),
            "projectileRaw" to (0..100000),
            "projectilePercent" to (0..10000),
            "magicRaw" to (0..100000),
            "magicPercent" to (0..10000),
            "damageRaw" to (0..100000),
            "damagePercent" to (0..10000),
            "elementalRaw" to (0..100000),
            "elementalPercent" to (0..10000),
            "thunderRaw" to (0..100000),
            "thunderPercent" to (0..10000),
            "earthRaw" to (0..100000),
            "earthPercent" to (0..10000),
            "waterRaw" to (0..100000),
            "waterPercent" to (0..10000),
            "fireRaw" to (0..100000),
            "firePercent" to (0..10000),
            "airRaw" to (0..100000),
            "airPercent" to (0..10000),
            "darkRaw" to (0..100000),
            "darkPercent" to (0..10000),
            "lightRaw" to (0..100000),
            "lightPercent" to (0..10000),
            "neutralRaw" to (0..100000),
            "neutralPercent" to (0..10000),
            "thunderResistance" to (0..100),
            "earthResistance" to (0..100),
            "waterResistance" to (0..100),
            "fireResistance" to (0..100),
            "airResistance" to (0..100),
            "darkResistance" to (0..100),
            "lightResistance" to (0..100),
            "neutralResistance" to (0..100),
            "bleeding" to (0..10000),
            "poison" to (0..10000)
        )

        // Get all mutable stat names (excluding uuid)
        private fun getStatNames(): List<String> =
            HumanStats::class.memberProperties
                .filter { it.name != "uuid" && it is KMutableProperty1<*, *> }
                .map { it.name }
    }

    // ==================== PUBLIC API: EQUIPMENT MANAGEMENT ====================

    /**
     * ATOMIC UPDATE: Completely replace a slot's bonuses
     * This is the ONLY way to update equipment stats
     */
    fun setSlotBonuses(slot: EquipmentSlot, bonuses: Map<String, Int>) {
        synchronized(lock) {
            equipmentBonuses[slot] = bonuses.toMap()
            recalculateAllStats()
        }
    }

    /**
     * ATOMIC CLEAR: Remove all bonuses from a slot
     */
    fun clearSlot(slot: EquipmentSlot) {
        synchronized(lock) {
            equipmentBonuses.remove(slot)
            recalculateAllStats()
        }
    }

    /**
     * ATOMIC CLEAR ALL: Remove all equipment bonuses
     */
    fun clearAllEquipmentBonuses() {
        synchronized(lock) {
            equipmentBonuses.clear()
            recalculateAllStats()
        }
    }

    // ==================== PUBLIC API: STAT QUERIES ====================

    /**
     * Get current total value of a stat (base + equipment)
     */
    @Suppress("UNCHECKED_CAST")
    fun getValue(statName: String): Int? {
        val property = HumanStats::class.memberProperties
            .firstOrNull { it.name.equals(statName, ignoreCase = true) }
                as? KMutableProperty1<HumanStats, Int> ?: return null

        return property.get(this)
    }

    /**
     * Get base value (stat without any equipment bonuses)
     */
    fun getBaseValue(statName: String): Int? {
        return baseValues[statName]
    }

    /**
     * Get total equipment bonus across all slots for a stat
     */
    fun getTotalEquipmentBonus(statName: String): Int {
        return equipmentBonuses.values.sumOf { it[statName] ?: 0 }
    }

    /**
     * Get equipment bonus from a specific slot
     */
    fun getSlotBonus(slot: EquipmentSlot, statName: String): Int {
        return equipmentBonuses[slot]?.get(statName) ?: 0
    }

    /**
     * Get all current stats as a map
     */
    fun getAllStats(): Map<String, Int> =
        getStatNames().mapNotNull { statName ->
            getValue(statName)?.let { statName to it }
        }.toMap()

    /**
     * Debug: Get equipment breakdown for a stat
     */
    fun getEquipmentBreakdown(statName: String): Map<EquipmentSlot, Int> {
        return equipmentBonuses
            .mapNotNull { (slot, stats) ->
                stats[statName]?.let { slot to it }
            }
            .toMap()
    }

    // ==================== PUBLIC API: STAT MODIFICATION ====================

    /**
     * Set a stat's base value (preserves equipment bonuses)
     * Use this for skills, buffs, level-ups, etc.
     */
    @Suppress("UNCHECKED_CAST")
    fun setBaseValue(statName: String, baseValue: Int): Boolean {
        synchronized(lock) {
            val property = HumanStats::class.memberProperties
                .firstOrNull { it.name.equals(statName, ignoreCase = true) }
                    as? KMutableProperty1<HumanStats, Int> ?: return false

            val range = statRanges[property.name] ?: return false

            // Store the true base value
            baseValues[property.name] = baseValue

            // Recalculate total: base + equipment
            recalculateAllStats()
            return true
        }
    }

    /**
     * Modify a stat's base value by a delta (preserves equipment bonuses)
     */
    fun modifyBaseValue(statName: String, delta: Int): Boolean {
        val currentBase = getBaseValue(statName) ?: return false
        return setBaseValue(statName, currentBase + delta)
    }

    // ==================== INTERNAL: RECALCULATION ====================

    /**
     * CRITICAL: Recalculate ALL stats from base values + equipment
     * This ensures no accumulation bugs
     */
    @Suppress("UNCHECKED_CAST")
    private fun recalculateAllStats() {
        getStatNames().forEach { statName ->
            val property = HumanStats::class.memberProperties
                .firstOrNull { it.name.equals(statName, ignoreCase = true) }
                    as? KMutableProperty1<HumanStats, Int> ?: return@forEach

            val range = statRanges[property.name] ?: return@forEach

            // Use stored base value (guaranteed correct)
            val baseValue = baseValues[property.name] ?: 0

            // Calculate total equipment bonus
            val equipmentTotal = equipmentBonuses.values.sumOf { it[property.name] ?: 0 }

            // Set new total: base + equipment (effects are NOT stat modifiers)
            val newTotal = (baseValue + equipmentTotal).coerceIn(range)
            property.set(this, newTotal)
        }

        triggerStatChangeEffects()
    }

    /**
     * Handle side effects of stat changes
     */
    private fun triggerStatChangeEffects() {
        val player = Bukkit.getPlayer(UUID.fromString(uuid))

        // Health bounds enforcement
        health = health.coerceIn(0, maxhealth)

        // Death check
        if (health <= 0 && player != null) {
            Bukkit.getPluginManager().callEvent(HumanDeathEvent(player))
        }

        // Speed update
        player?.let { HumanSpeedLSN.onHumanSpeedChange(it, speed) }

        // Crossbow update (attack speed affects quick charge)
        if (player is Player) {
            ItemEquipLSN.updateCrossbow(player)
        }
    }

    // =================== EFFECTS ====================

    fun applyEffect(effect: Effect) {
        synchronized(lock) {
            activeEffects.add(effect)
            effect.onApply(this)

            // schedule ticks if periodic
            if (effect.periodic) {
                HumanEffectTask.start(GeekedCraft.getInstance(), this, effect)
            }

            // auto-remove after duration if timed
            effect.durationTicks?.let { ticks ->
                GeekedCraft.getInstance().scheduleDelayedTask({
                    removeEffect(effect)
                }, ticks)
            }
        }
    }

    fun removeEffect(effect: Effect) {
        synchronized(lock) {
            if (activeEffects.remove(effect)) {
                effect.onRemove(this)
                HumanEffectTask.stop(this)
            }
        }
    }

    fun hasEffect(effectId: String): Boolean {
        return activeEffects.any { it.id == effectId }
    }

    fun getActiveEffects(): List<Effect> {
        return activeEffects.toList()
    }
}