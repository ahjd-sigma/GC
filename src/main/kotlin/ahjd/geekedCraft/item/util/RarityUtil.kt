package ahjd.geekedCraft.item.util

import net.md_5.bungee.api.ChatColor
import java.awt.Color

object RarityUtil {
    fun getGradientText(text: String, rarity: Rarity): String {
        if (rarity.colorEnd == null) {
            return ChatColor.of(rarity.colorStart).toString() + text
        }

        val result = StringBuilder()
        val length = text.length

        for (i in text.indices) {
            val ratio = if (length > 1) i.toFloat() / (length - 1) else 0f
            val color = interpolateHex(rarity.colorStart, rarity.colorEnd, ratio)
            result.append(ChatColor.of(color)).append(text[i])
        }

        return result.toString()
    }

    private fun interpolateHex(startHex: String, endHex: String, ratio: Float): String {
        val start = Color.decode(startHex)
        val end = Color.decode(endHex)

        val r = (start.red + ratio * (end.red - start.red)).toInt().coerceIn(0, 255)
        val g = (start.green + ratio * (end.green - start.green)).toInt().coerceIn(0, 255)
        val b = (start.blue + ratio * (end.blue - start.blue)).toInt().coerceIn(0, 255)

        return "#%02x%02x%02x".format(r, g, b)
    }
}
