package ahjd.geekedCraft.item.ability

enum class AbilityTrigger {
    ON_HIT,          // When player hits an entity
    ON_DAMAGED,      // When player takes damage
    ON_KILL,         // When player kills an entity
    ON_DEATH,        // When player dies
    WHILE_HELD,      // Passive while holding item
    WHILE_WORN,      // Passive while wearing armor
    PERIODIC         // Triggers every X seconds
}
