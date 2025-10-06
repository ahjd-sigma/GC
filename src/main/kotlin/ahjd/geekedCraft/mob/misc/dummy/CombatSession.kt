package ahjd.geekedCraft.mob.misc.dummy

data class CombatSession(
    val damageEntries: MutableList<DamageEntry> = mutableListOf(),
    var lastDamageTime: Long = 0L,
    var combatStartTime: Long = 0L,
    var isInCombat: Boolean = false,
    var spawnTime: Long = System.currentTimeMillis()
)