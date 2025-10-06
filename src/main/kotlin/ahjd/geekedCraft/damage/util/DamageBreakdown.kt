package ahjd.geekedCraft.damage.util

data class DamageBreakdown(
    val elementalDamages: Map<ElementType, Int>,
    val totalDamage: Int,
    val wasCrit: Boolean
)