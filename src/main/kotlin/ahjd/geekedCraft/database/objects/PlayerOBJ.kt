package ahjd.geekedCraft.database.objects

data class PlayerOBJ(
    var name: String = "Unknown",
    var playtime: Long = 0L,
    var jumps: Int = 0,
    var deaths: Int = 0,
    var logins: Int = 0
)
