package ahjd.geekedCraft.item

import ahjd.geekedCraft.item.util.ItemSlotType
import ahjd.geekedCraft.item.util.ItemTemplate
import ahjd.geekedCraft.item.util.RarityUtil
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*


object ItemFactory {
    private val plugin = Bukkit.getPluginManager().getPlugin("GeekedCraft")!!
    private val keyTemplate = NamespacedKey(plugin, "template_id")
    private val keyUUID = NamespacedKey(plugin, "uuid")
    private val keyStats = NamespacedKey(plugin, "stats")
    private val keySlotType = NamespacedKey(plugin, "slot_type")

    fun create(template: ItemTemplate, amount: Int = 1): ItemStack {
        val stack = ItemStack(template.material, amount)
        val meta = stack.itemMeta ?: return stack

        meta.isUnbreakable = true
        meta.setDisplayName(RarityUtil.getGradientText(template.name, template.rarity))
        meta.lore = template.getFormattedLore()
        template.customModelData?.let { meta.setCustomModelData(it) }

        // Clean all attributes at once
        listOf(
            Attribute.ATTACK_SPEED, Attribute.ATTACK_DAMAGE, Attribute.ARMOR,
            Attribute.ARMOR_TOUGHNESS, Attribute.KNOCKBACK_RESISTANCE, Attribute.ATTACK_KNOCKBACK
        ).forEach { meta.removeAttributeModifier(it) }

        // Infinity for projectile weapons
        if (template.material in listOf(org.bukkit.Material.BOW, org.bukkit.Material.CROSSBOW)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.INFINITY, 1, true)
        }

        meta.addItemFlags(
            ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE,
            ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP
        )

        val pdc = meta.persistentDataContainer
        pdc.set(keyTemplate, PersistentDataType.STRING, template.id)
        pdc.set(keySlotType, PersistentDataType.STRING, template.slotType.name)

        if (template.stats.isNotEmpty()) {
            pdc.set(keyStats, PersistentDataType.STRING,
                template.stats.entries.joinToString(";") { "${it.key}:${it.value}" })
        }

        if (!template.stackable) {
            pdc.set(keyUUID, PersistentDataType.STRING, UUID.randomUUID().toString())
        }

        stack.itemMeta = meta
        return stack
    }

    fun getTemplateId(item: ItemStack?): String? {
        if (item == null) return null
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(keyTemplate, PersistentDataType.STRING)
    }

    fun getUUID(item: ItemStack?): UUID? {
        if (item == null) return null
        val meta = item.itemMeta ?: return null
        val str = meta.persistentDataContainer.get(keyUUID, PersistentDataType.STRING) ?: return null
        return try {
            UUID.fromString(str)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun getStats(item: ItemStack?): Map<String, Int> {
        if (item == null) return emptyMap()
        val meta = item.itemMeta ?: return emptyMap()
        val statsString = meta.persistentDataContainer.get(keyStats, PersistentDataType.STRING) ?: return emptyMap()

        return try {
            statsString.split(";")
                .filter { it.isNotBlank() }
                .associate {
                    val (key, value) = it.split(":")
                    key to value.toInt()
                }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getSlotType(item: ItemStack?): ItemSlotType? {
        if (item == null) return null
        val meta = item.itemMeta ?: return null
        val slotTypeString = meta.persistentDataContainer.get(keySlotType, PersistentDataType.STRING) ?: return null

        return try {
            ItemSlotType.valueOf(slotTypeString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun isCustomItem(item: ItemStack?): Boolean {
        return getTemplateId(item) != null
    }
}