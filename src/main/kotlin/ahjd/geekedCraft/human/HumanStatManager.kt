package ahjd.geekedCraft.human

object HumanStatManager {
    private val statsMap = mutableMapOf<String, HumanStats>()

    fun get(uuid: String): HumanStats =
        statsMap.getOrPut(uuid) { HumanStats(uuid) }

    fun remove(uuid: String) {
        statsMap.remove(uuid)
    }
}
