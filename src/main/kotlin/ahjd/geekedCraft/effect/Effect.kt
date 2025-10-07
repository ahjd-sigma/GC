package ahjd.geekedCraft.effect

import ahjd.geekedCraft.human.HumanStats

interface Effect {
    val id: String              // Unique identifier (e.g., "poison", "regen")
    val displayName: String     // Display name (e.g., "Poison", "Regeneration")
    val description: String     // Description for UI
    val durationTicks: Long?    // Duration in ticks (null = permanent, 20 ticks = 1 second)
    val periodic: Boolean       // Should onTick() be called periodically?
    val tickInterval: Long      // How often to call onTick (in ticks, default 1 = every tick)

    /**
     * Called once when effect is first applied
     */
    fun onApply(stats: HumanStats)

    /**
     * Called every [tickInterval] ticks if periodic = true
     */
    fun onTick(stats: HumanStats) {}

    /**
     * Called once when effect is removed (duration ends or manually removed)
     */
    fun onRemove(stats: HumanStats) {}
}