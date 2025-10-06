package ahjd.geekedCraft.damage

import ahjd.geekedCraft.damage.util.DamageBreakdown
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.damage.util.ElementType
import ahjd.geekedCraft.human.HumanStats
import ahjd.geekedCraft.mob.stats.MobStats
import kotlin.random.Random

object DamageCalculator {

    private const val DEFENSE_CONSTANT = 1000.0

    // Primary elements that benefit from elemental stats
    private val PRIMARY_ELEMENTS = setOf(
        ElementType.THUNDER,
        ElementType.EARTH,
        ElementType.WATER,
        ElementType.FIRE,
        ElementType.AIR
    )

    // ======================== MAIN ENTRY POINT ========================

    fun calculateDamage(attacker: Any, target: Any, damageType: DamageType): DamageBreakdown {
        return when {
            attacker is HumanStats && target is MobStats -> calculateHumanToMob(attacker, target, damageType)
            attacker is MobStats && target is HumanStats -> calculateMobToHuman(attacker, target, damageType)
            attacker is HumanStats && target is HumanStats -> calculateHumanToHuman(attacker, target, damageType)
            attacker is MobStats && target is MobStats -> calculateMobToMob(attacker, target, damageType)
            else -> DamageBreakdown(emptyMap(), 0, false)
        }
    }

    // ======================== CALCULATION FLOWS ========================

    private fun calculateHumanToMob(attacker: HumanStats, target: MobStats, damageType: DamageType): DamageBreakdown {
        val elementalDamages = calculateElementalDamages(attacker, damageType)

        // Apply target resistances per element
        val resistedDamages = applyElementalResistances(elementalDamages, target)
        val resistedTotal = resistedDamages.values.sum()

        // Apply universal damage multiplier
        val universalMultiplier = 1.0 + (attacker.damagePercent / 100.0)
        var totalDamage = resistedTotal * universalMultiplier

        // Apply critical hit
        val critMultiplier = if (Random.nextInt(0, 100) < attacker.critChance) {
            1.0 + (attacker.critDamage / 100.0)
        } else {
            1.0
        }
        val wasCrit = critMultiplier > 1.0
        totalDamage *= critMultiplier

        // Apply defense reduction
        val defenseMultiplier = calculateDefenseMultiplier(target.defense)
        val finalDamage = totalDamage * defenseMultiplier

        val finalElementalDamages = resistedDamages.mapValues { (_, dmg) ->
            (dmg * universalMultiplier * critMultiplier * defenseMultiplier).toInt().coerceAtLeast(0)
        }

        return DamageBreakdown(finalElementalDamages, finalDamage.toInt().coerceAtLeast(0), wasCrit)
    }

    private fun calculateMobToHuman(attacker: MobStats, target: HumanStats, damageType: DamageType): DamageBreakdown {
        val elementalDamages = calculateElementalDamages(attacker, damageType)

        // Apply target resistances
        val resistedDamages = applyElementalResistances(elementalDamages, target)
        val resistedTotal = resistedDamages.values.sum()

        // Apply defense and base resistance
        val defenseMultiplier = calculateDefenseMultiplier(target.defense)
        val baseResistanceMultiplier = 1.0 - (target.baseResistance / 100.0)
        val finalDamage = resistedTotal * defenseMultiplier * baseResistanceMultiplier

        val finalElementalDamages = resistedDamages.mapValues { (_, dmg) ->
            (dmg * defenseMultiplier * baseResistanceMultiplier).toInt().coerceAtLeast(0)
        }

        return DamageBreakdown(finalElementalDamages, finalDamage.toInt().coerceAtLeast(0), false)
    }

    private fun calculateHumanToHuman(attacker: HumanStats, target: HumanStats, damageType: DamageType): DamageBreakdown {
        val elementalDamages = calculateElementalDamages(attacker, damageType)

        // Apply target resistances
        val resistedDamages = applyElementalResistances(elementalDamages, target)
        val resistedTotal = resistedDamages.values.sum()

        // Apply universal damage multiplier
        val universalMultiplier = 1.0 + (attacker.damagePercent / 100.0)
        var totalDamage = resistedTotal * universalMultiplier

        // Apply critical hit
        val critMultiplier = if (Random.nextInt(0, 100) < attacker.critChance) {
            1.0 + (attacker.critDamage / 100.0)
        } else {
            1.0
        }
        val wasCrit = critMultiplier > 1.0
        totalDamage *= critMultiplier

        // Apply defense and base resistance
        val defenseMultiplier = calculateDefenseMultiplier(target.defense)
        val baseResistanceMultiplier = 1.0 - (target.baseResistance / 100.0)
        val finalDamage = totalDamage * defenseMultiplier * baseResistanceMultiplier

        val finalElementalDamages = resistedDamages.mapValues { (_, dmg) ->
            (dmg * universalMultiplier * critMultiplier *
                    defenseMultiplier * baseResistanceMultiplier).toInt().coerceAtLeast(0)
        }

        return DamageBreakdown(finalElementalDamages, finalDamage.toInt().coerceAtLeast(0), wasCrit)
    }

    private fun calculateMobToMob(attacker: MobStats, target: MobStats, damageType: DamageType): DamageBreakdown {
        val elementalDamages = calculateElementalDamages(attacker, damageType)

        // Apply target resistances
        val resistedDamages = applyElementalResistances(elementalDamages, target)
        val resistedTotal = resistedDamages.values.sum()

        // Apply defense
        val defenseMultiplier = calculateDefenseMultiplier(target.defense)
        val finalDamage = resistedTotal * defenseMultiplier

        val finalElementalDamages = resistedDamages.mapValues { (_, dmg) ->
            (dmg * defenseMultiplier).toInt().coerceAtLeast(0)
        }

        return DamageBreakdown(finalElementalDamages, finalDamage.toInt().coerceAtLeast(0), false)
    }

    // ======================== CORE CALCULATION HELPERS ========================

    /**
     * Calculate raw elemental damages for a human attacker.
     * Returns a map of element -> raw damage (before resistances/defense).
     */
    private fun calculateElementalDamages(attacker: HumanStats, damageType: DamageType): Map<ElementType, Double> {
        val damages = mutableMapOf<ElementType, Double>()

        for (element in ElementType.entries) {
            val rawDamage = calculateRawDamage(attacker, element, damageType)
            if (rawDamage > 0.0) {
                val multipliedDamage = applyDamageMultipliers(attacker, element, damageType, rawDamage)
                damages[element] = multipliedDamage
            }
        }

        return damages
    }

    /**
     * Calculate raw elemental damages for a mob attacker.
     */
    private fun calculateElementalDamages(attacker: MobStats, damageType: DamageType): Map<ElementType, Double> {
        val damages = mutableMapOf<ElementType, Double>()

        for (element in ElementType.entries) {
            val rawDamage = calculateRawDamage(attacker, element, damageType)
            if (rawDamage > 0.0) {
                damages[element] = rawDamage
            }
        }

        return damages
    }

    /**
     * Calculate base raw damage for a specific element (Human).
     *
     * Raw damage formula:
     * - Specific element raw (e.g., fireRaw)
     * - + Damage type raw (melee/projectile/magic) - only if element raw > 0
     * - + Elemental raw (adds to ALL primary elements)
     *
     * Note: Damage type raw only applies to elements the player has invested in.
     * This prevents dealing damage in elements you haven't specc'ed into.
     */
    private fun calculateRawDamage(attacker: HumanStats, element: ElementType, damageType: DamageType): Double {
        // Get specific element raw
        val elementRaw = getElementRaw(attacker, element).toDouble()

        // Elemental raw applies to all primary elements
        val elementalRaw = if (element in PRIMARY_ELEMENTS) {
            attacker.elementalRaw.toDouble()
        } else {
            0.0
        }

        // Early exit if no investment in this element
        if (elementRaw + elementalRaw <= 0.0) {
            return 0.0
        }

        // Get damage type raw (only applies if you have this element)
        val damageTypeRaw = when (damageType) {
            DamageType.MELEE -> attacker.meleeRaw
            DamageType.PROJECTILE -> attacker.projectileRaw
            DamageType.MAGIC -> attacker.magicRaw
            DamageType.ENVIRONMENTAL -> 0
        }.toDouble()

        return elementRaw + damageTypeRaw + elementalRaw
    }

    /**
     * Calculate base raw damage for a specific element (Mob).
     */
    private fun calculateRawDamage(attacker: MobStats, element: ElementType, damageType: DamageType): Double {
        val elementRaw = getElementRaw(attacker, element).toDouble()

        val elementalRaw = if (element in PRIMARY_ELEMENTS) {
            attacker.elementalRaw.toDouble()
        } else {
            0.0
        }

        // Early exit if no investment in this element
        if (elementRaw + elementalRaw <= 0.0) {
            return 0.0
        }

        val damageTypeRaw = when (damageType) {
            DamageType.MELEE -> attacker.meleeRaw
            DamageType.PROJECTILE -> attacker.projectileRaw
            DamageType.MAGIC -> attacker.magicRaw
            DamageType.ENVIRONMENTAL -> 0
        }.toDouble()

        return elementRaw + damageTypeRaw + elementalRaw
    }

    /**
     * Apply percentage multipliers to raw damage (Human only).
     *
     * Multiplier formula (all multiplicative):
     * - Damage type % (melee/projectile/magic)
     * - × Elemental % (applies to ALL primary elements)
     * - × Specific element % (e.g., firePercent)
     */
    private fun applyDamageMultipliers(
        attacker: HumanStats,
        element: ElementType,
        damageType: DamageType,
        rawDamage: Double
    ): Double {
        // Damage type multiplier
        val damageTypeMultiplier = when (damageType) {
            DamageType.MELEE -> 1.0 + (attacker.meleePercent / 100.0)
            DamageType.PROJECTILE -> 1.0 + (attacker.projectilePercent / 100.0)
            DamageType.MAGIC -> 1.0 + (attacker.magicPercent / 100.0)
            DamageType.ENVIRONMENTAL -> 1.0
        }

        // Elemental multiplier (applies to primary elements only)
        val elementalMultiplier = if (element in PRIMARY_ELEMENTS) {
            1.0 + (attacker.elementalPercent / 100.0)
        } else {
            1.0
        }

        // Specific element multiplier
        val specificElementMultiplier = 1.0 + (getElementPercent(attacker, element) / 100.0)

        return rawDamage * damageTypeMultiplier * elementalMultiplier * specificElementMultiplier
    }

    /**
     * Apply elemental resistances to calculated damages.
     */
    private fun applyElementalResistances(
        elementalDamages: Map<ElementType, Double>,
        target: HumanStats
    ): Map<ElementType, Double> {
        return elementalDamages.mapValues { (element, damage) ->
            val resistance = getElementResistance(target, element)
            damage * (1.0 - resistance / 100.0)
        }
    }

    private fun applyElementalResistances(
        elementalDamages: Map<ElementType, Double>,
        target: MobStats
    ): Map<ElementType, Double> {
        return elementalDamages.mapValues { (element, damage) ->
            val resistance = getElementResistance(target, element)
            damage * (1.0 - resistance / 100.0)
        }
    }

    /**
     * Calculate defense reduction multiplier.
     */
    private fun calculateDefenseMultiplier(defense: Int): Double {
        return 1.0 - (defense / (defense + DEFENSE_CONSTANT))
    }

    // ======================== STAT ACCESSORS ========================

    private fun getElementRaw(stats: HumanStats, element: ElementType): Int {
        return when (element) {
            ElementType.THUNDER -> stats.thunderRaw
            ElementType.EARTH -> stats.earthRaw
            ElementType.WATER -> stats.waterRaw
            ElementType.FIRE -> stats.fireRaw
            ElementType.AIR -> stats.airRaw
            ElementType.DARK -> stats.darkRaw
            ElementType.LIGHT -> stats.lightRaw
            ElementType.NEUTRAL -> stats.neutralRaw
        }
    }

    private fun getElementRaw(stats: MobStats, element: ElementType): Int {
        return when (element) {
            ElementType.THUNDER -> stats.thunderRaw
            ElementType.EARTH -> stats.earthRaw
            ElementType.WATER -> stats.waterRaw
            ElementType.FIRE -> stats.fireRaw
            ElementType.AIR -> stats.airRaw
            ElementType.DARK -> stats.darkRaw
            ElementType.LIGHT -> stats.lightRaw
            ElementType.NEUTRAL -> stats.neutralRaw
        }
    }

    private fun getElementPercent(stats: HumanStats, element: ElementType): Int {
        return when (element) {
            ElementType.THUNDER -> stats.thunderPercent
            ElementType.EARTH -> stats.earthPercent
            ElementType.WATER -> stats.waterPercent
            ElementType.FIRE -> stats.firePercent
            ElementType.AIR -> stats.airPercent
            ElementType.DARK -> stats.darkPercent
            ElementType.LIGHT -> stats.lightPercent
            ElementType.NEUTRAL -> stats.neutralPercent
        }
    }

    private fun getElementResistance(stats: HumanStats, element: ElementType): Int {
        return when (element) {
            ElementType.THUNDER -> stats.thunderResistance
            ElementType.EARTH -> stats.earthResistance
            ElementType.WATER -> stats.waterResistance
            ElementType.FIRE -> stats.fireResistance
            ElementType.AIR -> stats.airResistance
            ElementType.DARK -> stats.darkResistance
            ElementType.LIGHT -> stats.lightResistance
            ElementType.NEUTRAL -> stats.neutralResistance
        }
    }

    private fun getElementResistance(stats: MobStats, element: ElementType): Int {
        return when (element) {
            ElementType.THUNDER -> stats.thunderResistance
            ElementType.EARTH -> stats.earthResistance
            ElementType.WATER -> stats.waterResistance
            ElementType.FIRE -> stats.fireResistance
            ElementType.AIR -> stats.airResistance
            ElementType.DARK -> stats.darkResistance
            ElementType.LIGHT -> stats.lightResistance
            ElementType.NEUTRAL -> stats.neutralResistance
        }
    }
}