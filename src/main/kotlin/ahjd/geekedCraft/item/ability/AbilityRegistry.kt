package ahjd.geekedCraft.item.ability

import ahjd.geekedCraft.item.ability.abilities.SunsWrathAbility

// Ability registry
object AbilityRegistry {
    private val abilities = mutableMapOf<String, Ability>()

    private fun register(ability: Ability) {
        abilities[ability.id] = ability
    }

    fun get(id: String): Ability? = abilities[id]

    fun getAll(): List<Ability> = abilities.values.toList()

    // Register default abilities
    fun registerDefaults() {
        register(SunsWrathAbility())
    }
}