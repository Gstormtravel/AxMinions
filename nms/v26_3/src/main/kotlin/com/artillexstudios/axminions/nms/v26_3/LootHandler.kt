package com.artillexstudios.axminions.nms.v26_3

import com.artillexstudios.axminions.api.minions.Minion
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

object LootHandler {

    fun generateFishingLoot(minion: Minion, waterLocation: Location): List<ItemStack> {
        return try {
            val tool = minion.getTool()
            val nmsItem: net.minecraft.world.item.ItemStack = if (tool == null) {
                net.minecraft.world.item.ItemStack.EMPTY
            } else {
                CraftItemStack.asNMSCopy(tool)
            }

            val luck = tool?.getEnchantmentLevel(Enchantment.LUCK_OF_THE_SEA)?.toFloat() ?: 0f

            val level = (minion.getLocation().world as CraftWorld).handle

            val lootparams = LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3(waterLocation.x, waterLocation.y, waterLocation.z))
                .withParameter(LootContextParams.TOOL, nmsItem)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, null)
                .withLuck(luck)
                .create(LootContextParamSets.FISHING)

            // Fórmula idêntica ao Minecraft Vanilla para pescaria:
            // Peixes: base 85, quality -1 (reduz levemente com Sorte do Mar)
            // Lixo: base 10, quality -2 (reduz fortemente com Sorte do Mar)
            // Tesouro: base 5, quality +2 (aumenta fortemente com Sorte do Mar)
            val fishWeight = Math.max(1.0, 85.0 - 1.0 * luck)
            val junkWeight = Math.max(0.0, 10.0 - 2.0 * luck)
            val treasureWeight = Math.max(0.0, 5.0 + 2.0 * luck)
            val totalWeight = fishWeight + junkWeight + treasureWeight

            val roll = ThreadLocalRandom.current().nextDouble() * totalWeight
            val selectedTableKey = when {
                roll < junkWeight -> BuiltInLootTables.FISHING_JUNK
                roll < junkWeight + treasureWeight -> BuiltInLootTables.FISHING_TREASURE
                else -> BuiltInLootTables.FISHING_FISH
            }

            val registries = MinecraftServer.getServer().reloadableRegistries()
            val lootTable = registries.getLootTable(selectedTableKey)

            val items = lootTable.getRandomItems(lootparams).map { original: net.minecraft.world.item.ItemStack ->
                CraftItemStack.asBukkitCopy(original)
            }

            if (items.isNotEmpty()) {
                items
            } else {
                listOf(ItemStack(Material.COD))
            }
        } catch (e: Exception) {
            org.bukkit.Bukkit.getLogger().severe("[AxMinions] Erro ao gerar loot de pesca via NMS: ${e.message}")
            e.printStackTrace()
            listOf(ItemStack(Material.COD))
        }
    }
}
