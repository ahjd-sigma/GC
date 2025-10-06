package ahjd.geekedCraft.database.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import java.util.*

data class HologramOBJ(
    var name: String = "Unknown",
    var uuid: UUID? = null,
    var text: TextComponent = Component.text(""),
    var location: Location? = null,
    var alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
    var shadowed: Boolean = false,
    var seeThrough: Boolean = false,
    var backgroundColor: Int? = null,
    var glow: Boolean = false,
    var billboard: Display.Billboard = Display.Billboard.FIXED,
    var textOpacity: Byte = -1,
    var lineWidth: Int = 200,
    var brightness: Pair<Int, Int>? = null,
    var viewRange: Float = 1.0f,
    var shadowRadius: Float = 0.0f,
    var shadowStrength: Float = 1.0f,
    var scale: Triple<Float, Float, Float>? = null  // (x, y, z) scale
)