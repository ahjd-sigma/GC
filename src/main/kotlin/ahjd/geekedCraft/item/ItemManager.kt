package ahjd.geekedCraft.item

import ahjd.geekedCraft.item.util.*
import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.util.MSG
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

object ItemManager {
    private val plugin = GeekedCraft.getInstance()
    private val templates = mutableMapOf<String, ItemTemplate>()

    fun loadTemplates(dataFolder: File) {
        val file = File(dataFolder, "items.yml")
        if (!file.exists()) {
            MSG.warn("items.yml not found! Creating default...")
            createDefaultConfig(file)
            return
        }

        templates.clear()
        val config = YamlConfiguration.loadConfiguration(file)
        val section = config.getConfigurationSection("items") ?: run {
            MSG.warn("No 'items' section found in items.yml")
            return
        }

        var loaded = 0
        for (id in section.getKeys(false)) {
            try {
                val mat = section.getString("$id.material")?.let {
                    try {
                        org.bukkit.Material.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        MSG.warn("Invalid material '$it' for item '$id'")
                        null
                    }
                } ?: continue

                val name = section.getString("$id.name") ?: id
                val rarity = Rarity.fromString(section.getString("$id.rarity") ?: "COMMON")
                val type = ItemType.fromString(section.getString("$id.type") ?: "MISC")
                val subtype = section.getString("$id.subtype")?.let { ItemSubtype.fromString(it) }
                val slotType = ItemSlotType.fromString(section.getString("$id.slotType") ?: inferSlotType(type))
                val stackable = section.getBoolean("$id.stackable", true)
                val description = section.getString("$id.description")
                val baseLore = section.getStringList("$id.lore")

                val stats = mutableMapOf<String, Int>()
                section.getConfigurationSection("$id.stats")?.let { statSec ->
                    statSec.getKeys(false).forEach { key ->
                        stats[key] = statSec.getInt(key)
                    }
                }

                val abilityId = section.getString("$id.ability")
                val cmd = if (section.isInt("$id.customModelData"))
                    section.getInt("$id.customModelData") else null

                val template = ItemTemplate(id, name, mat, rarity, type, subtype, slotType, stackable, cmd, description, baseLore, stats, abilityId)
                templates[id] = template
                loaded++
            } catch (e: Exception) {
                MSG.warn("Failed to load item '$id': ${e.message}")
            }
        }

        MSG.info("Loaded $loaded item templates")
    }

    private fun inferSlotType(type: ItemType): String {
        return when (type) {
            ItemType.ARROW -> "ARROW"
            ItemType.WEAPON -> "MAIN_HAND"
            ItemType.ARMOR -> "ARMOR"
            ItemType.ACCESSORY -> "NONE"
            ItemType.CONSUMABLE -> "NONE"
            ItemType.MATERIAL -> "NONE"
            ItemType.MISC -> "NONE"
        }
    }

    private fun createDefaultConfig(file: File) {
        file.parentFile.mkdirs()
        file.writeText(
            """
items:
  # ===== WEAPONS (MAIN_HAND) =====

  # SWORD
  iron_blade:
    name: "Iron Blade"
    material: IRON_SWORD
    type: WEAPON
    subtype: SWORD
    slotType: MAIN_HAND
    rarity: COMMON
    stackable: false
    stats:
      meleeRaw: 8
      critChance: 15

  # LONGSWORD
  greatsword:
    name: "Claymore"
    material: DIAMOND_SWORD
    type: WEAPON
    subtype: LONGSWORD
    slotType: MAIN_HAND
    rarity: RARE
    stackable: false
    customModelData: 1001
    stats:
      meleeRaw: 15
      sweep: 90
      attackspeed: -20

  # DAGGER
  shadow_blade:
    name: "Assassin's Edge"
    material: IRON_SWORD
    type: WEAPON
    subtype: DAGGER
    slotType: MAIN_HAND
    rarity: EPIC
    stackable: false
    customModelData: 1002
    stats:
      meleeRaw: 6
      critChance: 50
      critDamage: 120
      attackspeed: 40

  # SPEAR
  pike:
    name: "Iron Pike"
    material: TRIDENT
    type: WEAPON
    subtype: SPEAR
    slotType: MAIN_HAND
    rarity: UNCOMMON
    stackable: false
    ability: suns_wrath
    stats:
      meleeRaw: 10
      range: 5

  # TRIDENT
  ocean_fork:
    name: "Trident of the Deep"
    material: TRIDENT
    type: WEAPON
    subtype: TRIDENT
    slotType: MAIN_HAND
    rarity: RARE
    stackable: false
    customModelData: 1003
    stats:
      meleeRaw: 12
      waterRaw: 8
      range: 4

  # SHORT_BOW
  hunting_bow:
    name: "Hunter's Shortbow"
    material: CARROT_ON_A_STICK
    type: WEAPON
    subtype: SHORT_BOW
    slotType: MAIN_HAND
    rarity: COMMON
    stackable: false
    customModelData: 1004
    stats:
      projectileRaw: 12
      attackspeed: 30

  # CROSSBOW
  heavy_crossbow:
    name: "Siege Crossbow"
    material: CROSSBOW
    type: WEAPON
    subtype: CROSSBOW
    slotType: MAIN_HAND
    rarity: RARE
    stackable: false
    customModelData: 1005
    stats:
      projectileRaw: 20
      attackspeed: -10

  # WAND
  fire_wand:
    name: "Wand of Flames"
    material: STICK
    type: WEAPON
    subtype: WAND
    slotType: MAIN_HAND
    rarity: EPIC
    stackable: false
    customModelData: 1006
    stats:
      magicRaw: 15
      fireRaw: 10
      mana: 30

  # GRIMOIRE
  spell_tome:
    name: "Arcane Codex"
    material: BOOK
    type: WEAPON
    subtype: GRIMOIRE
    slotType: MAIN_HAND
    rarity: LEGENDARY
    stackable: false
    customModelData: 1007
    stats:
      magicRaw: 25
      elementalRaw: 15
      mana: 60

  # ===== ARMOR (ARMOR) =====

  # HELMET
  iron_helm:
    name: "Iron Helmet"
    material: IRON_HELMET
    type: ARMOR
    subtype: HELMET
    slotType: ARMOR
    rarity: COMMON
    stackable: false
    stats:
      defense: 10
      maxhealth: 5

  # CHESTPLATE
  iron_chest:
    name: "Iron Chestplate"
    material: IRON_CHESTPLATE
    type: ARMOR
    subtype: CHESTPLATE
    slotType: ARMOR
    rarity: COMMON
    stackable: false
    ability: damage_reduction
    stats:
      defense: 20
      maxhealth: 10

  # LEGGINGS
  iron_legs:
    name: "Iron Leggings"
    material: IRON_LEGGINGS
    type: ARMOR
    subtype: LEGGINGS
    slotType: ARMOR
    rarity: COMMON
    stackable: false
    stats:
      defense: 15
      maxhealth: 8

  # BOOTS
  iron_boots:
    name: "Iron Boots"
    material: IRON_BOOTS
    type: ARMOR
    subtype: BOOTS
    slotType: ARMOR
    rarity: COMMON
    stackable: false
    stats:
      defense: 8
      maxhealth: 5

  # ===== ACCESSORIES =====

  speed_ring:
    name: "Ring of Swiftness"
    material: GOLD_NUGGET
    type: ACCESSORY
    slotType: NONE
    rarity: UNCOMMON
    stackable: false
    customModelData: 2001
    ability: regeneration_aura
    description: "Increases movement and attack speed"
    stats:
      speed: 20
      attackspeed: 15

  strength_amulet:
    name: "Amulet of Strength"
    material: IRON_INGOT
    type: ACCESSORY
    slotType: NONE
    rarity: RARE
    stackable: false
    customModelData: 2002
    description: "Grants raw power"
    stats:
      damageRaw: 10
      meleeRaw: 5

  mana_charm:
    name: "Charm of the Arcane"
    material: LAPIS_LAZULI
    type: ACCESSORY
    slotType: NONE
    rarity: EPIC
    stackable: false
    customModelData: 2003
    description: "Enhances magical abilities"
    stats:
      mana: 40
      magicRaw: 8

  # ===== CONSUMABLES =====

  health_potion:
    name: "Health Potion"
    material: POTION
    type: CONSUMABLE
    slotType: NONE
    rarity: COMMON
    stackable: true
    description: "Restores health over time"
    stats:
      healthregen: 10

  strength_elixir:
    name: "Elixir of Strength"
    material: SPLASH_POTION
    type: CONSUMABLE
    slotType: NONE
    rarity: UNCOMMON
    stackable: true
    description: "Temporarily increases damage"
    stats:
      damageRaw: 15

  # ===== MATERIALS =====

  iron_ingot_mat:
    name: "Refined Iron"
    material: IRON_INGOT
    type: MATERIAL
    slotType: NONE
    rarity: COMMON
    stackable: true
    description: "Used for crafting equipment"

  dragon_scale:
    name: "Dragon Scale"
    material: PHANTOM_MEMBRANE
    type: MATERIAL
    slotType: NONE
    rarity: LEGENDARY
    stackable: true
    customModelData: 3001
    description: "Rare material from ancient dragons"

  # ===== MISC =====

  teleport_stone:
    name: "Waystone"
    material: ENDER_PEARL
    type: MISC
    slotType: NONE
    rarity: RARE
    stackable: true
    customModelData: 4001
    description: "Allows fast travel between locations"
    
    # ===== ARROWS =====
  normal_arrow:
    name: "Tuffest Arrow"
    material: Arrow
    type: ARROW
    slotType: ARROW
    rarity: PRIMORDIAL
    stackable: false
    customModelData: 5001
    description: "Infinite source of arrows"
    stats:
      projectileRaw: 35
      projectilePercent: 20
            """.trimIndent()
        )
    }

    fun getTemplate(id: String): ItemTemplate? = templates[id]

    fun createItem(id: String, amount: Int = 1): ItemStack? {
        val template = templates[id] ?: return null
        return ItemFactory.create(template, amount)
    }

    fun giveItem(player: Player, item: ItemStack) {
        val leftover = player.inventory.addItem(item)
        if (leftover.isNotEmpty()) {
            leftover.values.forEach {
                player.world.dropItemNaturally(player.location, it)
            }
        }
    }

    fun getTemplateIds(): List<String> = templates.keys.sorted()

    private fun isCustomItem(stack: ItemStack?): Boolean = ItemFactory.isCustomItem(stack)

    fun reload() {
        templates.clear()
        loadTemplates(plugin.dataFolder)
    }
    private fun getItemType(item: ItemStack?): ItemType? {
        val templateId = ItemFactory.getTemplateId(item) ?: return null
        val template = getTemplate(templateId) ?: return null
        return template.type
    }
    private fun getItemSubtype(item: ItemStack?): ItemSubtype? {
        val templateId = ItemFactory.getTemplateId(item) ?: return null
        val template = getTemplate(templateId) ?: return null
        return template.subtype
    }

    private val meleeSubtypes = setOf(
        ItemSubtype.SPEAR, ItemSubtype.DAGGER, ItemSubtype.SWORD,
        ItemSubtype.TRIDENT, ItemSubtype.LONGSWORD
    )

    fun canMelee(item: ItemStack?): Boolean {
        return item?.let {
            isCustomItem(it) &&
                    getItemType(it) == ItemType.WEAPON &&
                    getItemSubtype(it) in meleeSubtypes
        } ?: false
    }
}