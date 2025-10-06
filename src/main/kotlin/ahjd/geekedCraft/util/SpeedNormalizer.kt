package ahjd.geekedCraft.util

object SpeedNormalizer {
    fun normalize(speed: Int): Float = when {
        speed <= 100 -> (speed + 100) * 0.001f  // 0.0 to 0.2
        else -> (0.2 + (speed - 100) * 0.002).coerceIn(0.0, 1.5).toFloat()  // 0.2 to 1.0
    }
}