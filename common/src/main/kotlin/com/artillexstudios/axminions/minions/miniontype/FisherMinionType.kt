package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.events.PreFisherMinionFishEvent
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.nms.NMSHandler
import com.artillexstudios.axminions.skills.AbilityEngine
import com.artillexstudios.axminions.utils.CustomDropRoller
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt

class FisherMinionType : MinionType("fisher", AxMinionsPlugin.INSTANCE.getResource("minions/fisher.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(minion.getEffectiveRange())
        val tool = minion.getTool()?.getEnchantmentLevel(Enchantment.LURE)?.div(10.0) ?: 0.1
        val efficiency = 1.0 - if (tool > 0.9) 0.9 else tool
        minionImpl.setNextAction((minion.getEffectiveSpeed() * efficiency).roundToInt().coerceAtLeast(10))
    }

    override fun run(minion: Minion) {
        if (minion.getLinkedInventory() != null && minion.getLinkedInventory()?.firstEmpty() != -1) {
            Warnings.remove(minion, Warnings.CONTAINER_FULL)
        }

        if (minion.getLinkedChest() != null) {
            val type = minion.getLinkedChest()!!.block.type
            if (type == Material.CHEST && minion.getLinkedInventory() !is DoubleChestInventory && hasChestOnSide(minion.getLinkedChest()!!.block)) {
                minion.setLinkedChest(minion.getLinkedChest())
            }

            if (type == Material.CHEST && minion.getLinkedInventory() is DoubleChestInventory && !hasChestOnSide(minion.getLinkedChest()!!.block)) {
                minion.setLinkedChest(minion.getLinkedChest())
            }

            if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.BARREL) {
                minion.setLinkedChest(null)
            }
        }

        if (minion.getLinkedInventory()?.firstEmpty() == -1) {
            Warnings.CONTAINER_FULL.display(minion)
            return
        }

        var waterLocation: Location? = null
        val searchRadius = Math.max(2.0, minion.getRange())
        run breaking@{
            LocationUtils.getAllBlocksInRadius(minion.getLocation(), searchRadius, false).fastFor {
                val blockType = it.block.type
                if (blockType != Material.WATER && blockType != Material.WATER_CAULDRON) return@fastFor

                waterLocation = it
                return@breaking
            }
        }

        if (waterLocation == null) {
            Warnings.NO_WATER_NEARBY.display(minion)
            return
        }
        Warnings.remove(minion, Warnings.NO_WATER_NEARBY)

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        val fisherBonus = AbilityEngine.getFisherAbilityBonus(minion)
        val combinedLoot = mutableListOf<ItemStack>()
        combinedLoot.addAll(NMSHandler.get().generateRandomFishingLoot(minion, waterLocation!!))
        if (fisherBonus.doubleCatchChance > 0.0 && ThreadLocalRandom.current().nextDouble(100.0) < fisherBonus.doubleCatchChance) {
            combinedLoot.addAll(NMSHandler.get().generateRandomFishingLoot(minion, waterLocation!!))
        }
        if (fisherBonus.weatherFrenzyRolls > 0) {
            for (i in 0 until fisherBonus.weatherFrenzyRolls) {
                combinedLoot.addAll(NMSHandler.get().generateRandomFishingLoot(minion, waterLocation!!))
            }
        }

        val luckLevel = minion.getTool()?.getEnchantmentLevel(Enchantment.LUCK_OF_THE_SEA) ?: 0
        val passiveLuckBonus = AbilityEngine.getEffectiveLuckBonus(minion) + fisherBonus.extraLuckFromAbilities
        val customResult = CustomDropRoller.roll(
            minion = minion,
            luckLevel = luckLevel,
            passiveLuckBonus = passiveLuckBonus,
            extraMaxDrops = fisherBonus.maxCustomDropsBonus,
            sourceTag = "Fisher"
        )
        var loot: List<ItemStack> = if (customResult.replaceItem != null) {
            listOf(customResult.replaceItem)
        } else {
            combinedLoot
        }

        val preFishEvent = PreFisherMinionFishEvent(minion, loot)
        Bukkit.getPluginManager().callEvent(preFishEvent)
        if (preFishEvent.isCancelled) {
            return
        }
        if (preFishEvent.item != loot) {
            loot = preFishEvent.item
        }
        val xp = ((ThreadLocalRandom.current().nextInt(6) + 1) * AbilityEngine.getXpMultiplier(minion)).toInt().coerceAtLeast(1)

        val finalDrops = loot.toMutableList()
        if (customResult.bonusItems.isNotEmpty()) {
            finalDrops.addAll(customResult.bonusItems)
        }
        val abilityOutcome = AbilityEngine.processActionDrops(minion, finalDrops, block = waterLocation!!.block)
        minion.addToContainerOrDrop(finalDrops)
        AbilityEngine.compactChestForCandidates(minion, abilityOutcome.compactCandidates)

        if (customResult.commands.isNotEmpty()) {
            val onlineOwner = Bukkit.getPlayer(minion.getOwnerUUID())
            if (onlineOwner != null) {
                for (cmd in customResult.commands) {
                    val formatted = cmd
                        .replace("%player%", onlineOwner.name)
                        .replace("%minion_level%", minion.getLevel().toString())
                        .replace("%x%", minion.getLocation().blockX.toString())
                        .replace("%y%", minion.getLocation().blockY.toString())
                        .replace("%z%", minion.getLocation().blockZ.toString())
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted)
                }
            }
        }

        val maxStorage = AbilityEngine.getEffectiveStorage(minion, minion.getType().getLong("storage", minion.getLevel()).toDouble())
        val coerced = (minion.getStorage() + xp).coerceIn(0.0, maxStorage)
        minion.setStorage(coerced)

        minion.setActions(minion.getActionAmount() + 1)
        if (!abilityOutcome.cancelToolDamage) {
            minion.damageTool()
        }
        AbilityEngine.processPeriodicCycle(minion)
    }
}