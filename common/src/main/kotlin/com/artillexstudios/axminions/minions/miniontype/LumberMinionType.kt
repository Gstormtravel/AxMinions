package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.logging.MinionLogger
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.nms.NMSHandler
import com.artillexstudios.axminions.skills.AbilityEngine
import com.artillexstudios.axminions.utils.CustomDropRoller
import com.artillexstudios.axminions.utils.Enchantments
import kotlin.math.roundToInt
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.ItemStack

class LumberMinionType : MinionType("lumber", AxMinionsPlugin.INSTANCE.getResource("minions/lumber.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(minion.getEffectiveRange())
        val tool = Enchantments.EFFICIENCY?.let { minion.getTool()?.getEnchantmentLevel(it)?.div(10.0) } ?: 0.1
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

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        if (minion.getLinkedInventory()?.firstEmpty() == -1) {
            Warnings.CONTAINER_FULL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        val loot = ArrayList<ItemStack>()
        val emptyTool = ItemStack(Material.AIR)
        val clearLeaves = AbilityEngine.shouldClearTreeLeaves(minion)
        var lastTreeBlock: org.bukkit.block.Block? = null
        LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
            val treeBlocks = MinionUtils.getTree(location.block)
            if (treeBlocks.isEmpty()) return@fastFor

            treeBlocks.forEach {
                lastTreeBlock = it
                val down = it.getRelative(BlockFace.DOWN).type
                loot.addAll(NMSHandler.get().getDrops(it, minion.getTool() ?: emptyTool))

                if (down == Material.DIRT || down == Material.GRASS_BLOCK || down == Material.COARSE_DIRT || down == Material.ROOTED_DIRT || down == Material.DIRT_PATH || down == Material.MUD || down == Material.MUDDY_MANGROVE_ROOTS || down == Material.PODZOL) {
                    it.type = getSaplingType(it.type)
                    AbilityEngine.processSaplingFertilizer(minion, it)
                } else {
                    it.type = Material.AIR
                }
            }

            if (clearLeaves) {
                MinionUtils.getTreeLeaves(treeBlocks).forEach { leaf ->
                    loot.addAll(NMSHandler.get().getDrops(leaf, emptyTool))
                    leaf.type = Material.AIR
                }
            }
        }

        val logsHarvested = loot.size
        if (logsHarvested > 0) {
            val customResult = CustomDropRoller.roll(
                minion = minion,
                chanceMultiplier = AbilityEngine.getLumberRareDropMultiplier(minion),
                sourceTag = "Lumber"
            )
            if (customResult.bonusItems.isNotEmpty()) {
                loot.addAll(customResult.bonusItems)
            }
            if (customResult.rareTriggeredKeys.isNotEmpty()) {
                MinionLogger.log(minion, "LUMBER_RARE_DROP", "Drops raros: ${customResult.rareTriggeredKeys.joinToString(", ")}")
            }
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
        }

        val outcome = AbilityEngine.processActionDrops(minion, loot, block = lastTreeBlock)
        val lootSize = loot.size
        if (!outcome.cancelToolDamage) {
            minion.damageTool(logsHarvested.coerceAtLeast(lootSize))
        }
        minion.setActions(minion.getActionAmount() + lootSize)
        minion.addToContainerOrDrop(loot)
        AbilityEngine.compactChestForCandidates(minion, outcome.compactCandidates)
        AbilityEngine.processPeriodicCycle(minion)
    }

    private fun getSaplingType(material: Material): Material {
        val normalized = material.name.removePrefix("STRIPPED_")
        val candidate = when {
            normalized.startsWith("DARK_OAK_") -> "DARK_OAK_SAPLING"
            normalized.startsWith("PALE_OAK_") -> "PALE_OAK_SAPLING"
            normalized.startsWith("OAK_") -> "OAK_SAPLING"
            normalized.startsWith("SPRUCE_") -> "SPRUCE_SAPLING"
            normalized.startsWith("BIRCH_") -> "BIRCH_SAPLING"
            normalized.startsWith("JUNGLE_") -> "JUNGLE_SAPLING"
            normalized.startsWith("ACACIA_") -> "ACACIA_SAPLING"
            normalized.startsWith("MANGROVE_") -> "MANGROVE_PROPAGULE"
            normalized.startsWith("CHERRY_") -> "CHERRY_SAPLING"
            normalized.startsWith("POPLAR_") -> "POPLAR_SAPLING"
            normalized.startsWith("BAMBOO_") -> "BAMBOO"
            normalized.startsWith("WARPED_") -> "WARPED_FUNGUS"
            normalized.startsWith("CRIMSON_") -> "CRIMSON_FUNGUS"
            else -> "OAK_SAPLING"
        }
        return Material.matchMaterial(candidate) ?: Material.OAK_SAPLING
    }
}