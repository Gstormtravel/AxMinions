package com.artillexstudios.axminions.utils

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

/**
 * Shared roller for YAML-configured `custom-drops` sections (used by Fisher and Lumber minions).
 */
object CustomDropRoller {
    private val warnedKeys = mutableSetOf<String>()

    data class CustomDropResult(
        val replaceItem: ItemStack? = null,
        val bonusItems: List<ItemStack> = emptyList(),
        val commands: List<String> = emptyList(),
        val rareTriggeredKeys: List<String> = emptyList()
    )

    fun roll(
        minion: Minion,
        luckLevel: Int = 0,
        passiveLuckBonus: Double = 0.0,
        chanceMultiplier: Double = 1.0,
        extraMaxDrops: Int = 0,
        sourceTag: String = minion.getType().getName()
    ): CustomDropResult {
        val doc = minion.getType().getConfig().backingDocument
        if (!doc.isSection("custom-drops")) {
            return CustomDropResult()
        }
        val customSection = doc.getSection("custom-drops") ?: return CustomDropResult()
        val maxPerCatch = (doc.getInt("custom-drops-max-per-catch", 1) + extraMaxDrops).coerceAtLeast(1)

        val minionLevel = minion.getLevel()
        val random = ThreadLocalRandom.current()

        val entries = customSection.getRoutesAsStrings(false)
            .mapNotNull { key -> customSection.getSection(key)?.let { key to it } }
            .sortedBy { (_, sec) -> sec.getDouble("chance", 0.0) }

        var replaceItem: ItemStack? = null
        val bonusItems = mutableListOf<ItemStack>()
        val commands = mutableListOf<String>()
        val rareKeys = mutableListOf<String>()
        var dropsAwarded = 0

        for ((key, section) in entries) {
            if (dropsAwarded >= maxPerCatch) break
            if (!section.getBoolean("enabled", true)) continue
            if (minionLevel < section.getInt("min-level", 1)) continue
            if (luckLevel < section.getInt("min-luck", 0)) continue

            val baseChance = section.getDouble("chance", 0.0)
            val luckBonus = section.getDouble("luck-bonus", 0.0)
            val effectiveChance = ((baseChance + luckLevel * luckBonus + passiveLuckBonus) * chanceMultiplier).coerceIn(0.0, 100.0)
            if (effectiveChance <= 0.0) continue

            if (random.nextDouble(100.0) >= effectiveChance) continue

            val minAmount = section.getInt("min-amount", 1).coerceAtLeast(1)
            val maxAmount = section.getInt("max-amount", minAmount).coerceAtLeast(minAmount)
            val amount = if (maxAmount > minAmount) random.nextInt(minAmount, maxAmount + 1) else minAmount

            val onlineOwner = Bukkit.getPlayer(minion.getOwnerUUID())
            val item = createCustomDropItem(sourceTag, key, section, amount, onlineOwner) ?: continue

            val replaceVanilla = section.getBoolean("replace-vanilla", false)
            if (replaceVanilla && replaceItem == null) {
                replaceItem = item
            } else {
                bonusItems.add(item)
            }

            if (section.isList("commands")) {
                commands.addAll(section.getStringList("commands"))
            }

            if (baseChance <= 2.0) {
                rareKeys.add(key)
            }
            dropsAwarded++
        }

        return CustomDropResult(replaceItem, bonusItems, commands, rareKeys)
    }

    private fun createCustomDropItem(
        sourceTag: String,
        key: String,
        section: Section,
        amount: Int,
        owner: Player?
    ): ItemStack? {
        val eiId = section.getString("executable-item", "")
        if (!eiId.isNullOrBlank()) {
            return ItemUtils.buildExecutableItem(eiId, amount, owner)
        }

        val matName = section.getString("material", "")
        if (matName.isNullOrBlank()) return null
        val material = Material.matchMaterial(matName.uppercase())
        if (material == null) {
            if (warnedKeys.add("$sourceTag:$key:$matName")) {
                AxMinionsPlugin.INSTANCE.logger.warning("[$sourceTag] Invalid material '$matName' in custom-drops.$key")
            }
            return null
        }

        val item = ItemStack(material, amount)
        val meta = item.itemMeta ?: return item

        val displayName = section.getString("name", "")
        if (!displayName.isNullOrBlank()) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName))
        }

        if (section.isList("lore")) {
            meta.lore = section.getStringList("lore").map { ChatColor.translateAlternateColorCodes('&', it) }
        }

        var hasCustomEnchants = false
        if (section.isList("enchantments")) {
            for (raw in section.getStringList("enchantments")) {
                val parts = raw.split(":")
                if (parts.size == 2) {
                    val enchKey = NamespacedKey.minecraft(parts[0].trim().lowercase())
                    val ench = Registry.ENCHANTMENT.get(enchKey)
                    val level = parts[1].trim().toIntOrNull() ?: 1
                    if (ench != null) {
                        meta.addEnchant(ench, level, true)
                        hasCustomEnchants = true
                    }
                }
            }
        } else if (section.isSection("enchantments")) {
            val enchSec = section.getSection("enchantments")
            enchSec?.getRoutesAsStrings(false)?.forEach { rawName ->
                val enchKey = NamespacedKey.minecraft(rawName.trim().lowercase())
                val ench = Registry.ENCHANTMENT.get(enchKey)
                val level = enchSec.getInt(rawName, 1)
                if (ench != null) {
                    meta.addEnchant(ench, level, true)
                    hasCustomEnchants = true
                }
            }
        }

        if (section.getBoolean("glow", false) && !hasCustomEnchants) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        item.itemMeta = meta
        return item
    }
}
