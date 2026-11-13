package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.scheduler.impl.FoliaScheduler
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.LocationUtils
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.nms.NMSHandler
import com.artillexstudios.axminions.skills.AbilityEngine
import com.artillexstudios.axminions.utils.Enchantments
import dev.lone.itemsadder.api.CustomBlock
import me.kryniowesegryderiusz.kgenerators.Main
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MinerMinionType : MinionType("miner", AxMinionsPlugin.INSTANCE.getResource("minions/miner.yml")!!, true) {
    companion object {
        private var asyncExecutor: ExecutorService? = null
        private val smeltingRecipes = ArrayList<FurnaceRecipe>()
        private val faces = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

        init {
            Bukkit.recipeIterator().forEachRemaining {
                if (it is FurnaceRecipe) {
                    smeltingRecipes.add(it)
                }
            }
        }
    }

    private var generatorMode = false
    private val whitelist = arrayListOf<Material>()

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(minion.getEffectiveRange())
        val tool = Enchantments.EFFICIENCY?.let { minion.getTool()?.getEnchantmentLevel(it)?.div(10.0) } ?: 0.1
        val efficiency = 1.0 - if (tool > 0.9) 0.9 else tool
        minionImpl.setNextAction((minion.getEffectiveSpeed() * efficiency).roundToInt().coerceAtLeast(10))

        generatorMode = getConfig().getString("break", "generator").equals("generator", true)
        whitelist.clear()
        getConfig().getStringList("whitelist").fastFor {
            whitelist.add(Material.matchMaterial(it.uppercase(Locale.ENGLISH)) ?: return@fastFor)
        }
    }

    override fun run(minion: Minion) {
        if (minion.getLinkedInventory() != null && minion.getLinkedInventory()?.firstEmpty() != -1) {
            Warnings.remove(minion, Warnings.CONTAINER_FULL)
        }

        if (minion.getLinkedChest() != null && minion.getLinkedInventory() != null) {
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

        // 🌟 TRAVA DE DURABILIDADE (MÍNIMO 15) 🌟
        val currentTool = minion.getTool()
        if (currentTool != null && !currentTool.type.isAir) {
            val meta = currentTool.itemMeta
            if (meta is org.bukkit.inventory.meta.Damageable) {
                val maxDurability = currentTool.type.maxDurability
                val remainingDurability = maxDurability - meta.damage
                
                if (maxDurability > 0 && remainingDurability <= 15) {
                    Warnings.NO_TOOL.display(minion) // Mostra o aviso de "Sem Ferramenta"
                    return // Para o minion para não quebrar a picareta
                }
            }
        }
        // 🌟 FIM DA TRAVA 🌟

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        if (minion.getLinkedInventory()?.firstEmpty() == -1) {
            Warnings.CONTAINER_FULL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        var amount = 0
        var damageCount = 0
        var xp = 0
        when (getConfig().getString("mode").lowercase(Locale.ENGLISH)) {
            "sphere" -> {
                LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false).fastFor { location ->
                    if (AxMinionsPlugin.integrations.kGeneratorsIntegration) {
                        val gen = Main.getPlacedGenerators().getLoaded(location)
                        if (gen != null) {
                            if (gen.isBlockPossibleToMine(location)) {
                                val dropItem = gen.lastGeneratedObject.customDrops?.item?.clone()
                                if (dropItem != null) {
                                    minion.addToContainerOrDrop(dropItem)
                                }
                                gen.scheduleGeneratorRegeneration()
                            }
                            return@fastFor
                        }
                    }

                    val canBreak = if (generatorMode) {
                        MinionUtils.isStoneGenerator(location)
                    } else {
                        whitelist.contains(location.block.type)
                    }

                    if (canBreak) {
                        val block = location.block
                        if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators().getLoaded(location) != null) return@fastFor

                        val tool = minion.getTool() ?: ItemStack(Material.AIR)
                        xp += NMSHandler.get().getExp(block, tool)
                        val result = deliverMinedBlockDrops(minion, block, NMSHandler.get().getDrops(block, tool), handleIsland = true)
                        amount += result.first
                        if (!result.second) {
                            damageCount += result.first
                        }
                    }
                }
            }

            "asphere" -> {
                if (Scheduler.get() !is FoliaScheduler) {
                    if (asyncExecutor == null) {
                        asyncExecutor = Executors.newFixedThreadPool(3)
                    }

                    asyncExecutor!!.execute {
                        LocationUtils.getAllBlocksInRadius(minion.getLocation(), minion.getRange(), false)
                            .fastFor { location ->
                                if (AxMinionsPlugin.integrations.kGeneratorsIntegration) {
                                    val gen = Main.getPlacedGenerators().getLoaded(location)
                                    if (gen != null) {
                                        if (gen.isBlockPossibleToMine(location)) {
                                            val dropItem = gen.lastGeneratedObject.customDrops?.item?.clone()
                                            if (dropItem != null) {
                                                minion.addToContainerOrDrop(dropItem)
                                            }
                                            gen.scheduleGeneratorRegeneration()
                                        }
                                        return@fastFor
                                    }
                                }

                                val canBreak = if (generatorMode) {
                                    MinionUtils.isStoneGenerator(location)
                                } else {
                                    whitelist.contains(location.block.type)
                                }

                                if (canBreak) {
                                    Scheduler.get().run { _ ->
                                        val block = location.block
                                        if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators().getLoaded(location) != null) return@run

                                        val tool = minion.getTool() ?: ItemStack(Material.AIR)
                                        xp += NMSHandler.get().getExp(block, tool)
                                        val result = deliverMinedBlockDrops(minion, block, NMSHandler.get().getDrops(block, tool), handleIsland = true)
                                        amount += result.first
                                        if (!result.second) {
                                            damageCount += result.first
                                        }
                                    }
                                }
                            }
                    }
                } else {
                    val locCopy = minion.getLocation().clone()
                    locCopy.setX(locCopy.getBlockX().toDouble())
                    locCopy.setY(locCopy.getBlockY().toDouble())
                    locCopy.setZ(locCopy.getBlockZ().toDouble())
                    LocationUtils.getAllBlocksInRadius(locCopy, minion.getRange(), false)
                        .fastFor { location ->
                            if (AxMinionsPlugin.integrations.kGeneratorsIntegration) {
                                val gen = Main.getPlacedGenerators().getLoaded(location)
                                if (gen != null) {
                                    if (gen.isBlockPossibleToMine(location)) {
                                        val dropItem = gen.lastGeneratedObject.customDrops?.item?.clone()
                                        if (dropItem != null) {
                                            minion.addToContainerOrDrop(dropItem)
                                        }
                                        gen.scheduleGeneratorRegeneration()
                                    }
                                    return@fastFor
                                }
                            }

                            val canBreak = if (generatorMode) {
                                MinionUtils.isStoneGenerator(location)
                            } else {
                                whitelist.contains(location.block.type)
                            }

                            if (canBreak) {
                                val block = location.block
                                if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators().getLoaded(location) != null) return@fastFor

                                val tool = minion.getTool() ?: ItemStack(Material.AIR)
                                xp += NMSHandler.get().getExp(block, tool)
                                val result = deliverMinedBlockDrops(minion, block, NMSHandler.get().getDrops(block, tool), handleIsland = true)
                                amount += result.first
                                if (!result.second) {
                                    damageCount += result.first
                                }
                            }
                        }
                }
            }

            "line" -> {
                faces.fastFor {
                    val locCopy = minion.getLocation().clone()
                    locCopy.setX(locCopy.getBlockX().toDouble())
                    locCopy.setY(locCopy.getBlockY().toDouble())
                    locCopy.setZ(locCopy.getBlockZ().toDouble())
                    LocationUtils.getAllBlocksFacing(locCopy, minion.getRange(), it).fastFor { location ->
                        if (AxMinionsPlugin.integrations.kGeneratorsIntegration) {
                            val gen = Main.getPlacedGenerators().getLoaded(location)
                            if (gen != null) {
                                if (gen.isBlockPossibleToMine(location)) {
                                    val dropItem = gen.lastGeneratedObject.customDrops?.item?.clone()
                                    if (dropItem != null) {
                                        minion.addToContainerOrDrop(dropItem)
                                    }
                                    gen.scheduleGeneratorRegeneration()
                                }
                                return@fastFor
                            }
                        }

                        val canBreak = if (generatorMode) {
                            MinionUtils.isStoneGenerator(location)
                        } else {
                            whitelist.contains(location.block.type)
                        }

                        if (canBreak) {
                            val block = location.block
                            if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators().getLoaded(location) != null) return@fastFor

                            val tool = minion.getTool() ?: ItemStack(Material.AIR)
                            xp += NMSHandler.get().getExp(block, tool)
                            val result = deliverMinedBlockDrops(minion, block, NMSHandler.get().getDrops(block, tool), handleIsland = true)
                            amount += result.first
                            if (!result.second) {
                                damageCount += result.first
                            }
                        }
                    }
                }
            }

            "face" -> {
                val locCopy = minion.getLocation().clone()
                locCopy.setX(locCopy.getBlockX().toDouble())
                locCopy.setY(locCopy.getBlockY().toDouble())
                locCopy.setZ(locCopy.getBlockZ().toDouble())
                LocationUtils.getAllBlocksFacing(locCopy, minion.getRange(), minion.getDirection().facing)
                    .fastFor { location ->
                        var isGenerator = false
                        if (AxMinionsPlugin.integrations.kGeneratorsIntegration) {
                            try {
                                val cleanLoc = org.bukkit.Location(location.world, location.blockX.toDouble(), location.blockY.toDouble(), location.blockZ.toDouble())
                                val gen = Main.getPlacedGenerators().getLoaded(cleanLoc)
                                
                                if (gen != null && gen.location.blockX == location.blockX && gen.location.blockY == location.blockY && gen.location.blockZ == location.blockZ) {
                                    isGenerator = true
                                    
                                    val owner = minion.getOwner()
                                    if (owner is Player) {
                                        val minionLoc = minion.getLocation()
                                        val ownerLoc = owner.location
                                        
                                        // Trava de olhar pro bloco (evita quebrar o que o player tá olhando)
                                        if (ownerLoc.world == minionLoc.world && ownerLoc.distanceSquared(minionLoc) <= 100.0) {
                                            val targetBlock = owner.getTargetBlockExact(10)
                                            if (targetBlock != null && 
                                                targetBlock.x == location.blockX && 
                                                targetBlock.y == location.blockY && 
                                                targetBlock.z == location.blockZ) {
                                                return@fastFor
                                            }
                                        }
                                    }

                                    val block = location.block
                                    
                                    // Correção: Verifica se o bloco é o minério gerado e não o placeholder (BEDROCK)
                                    if (block.type != Material.AIR && block.type != Material.BEDROCK) {
                                        // Usa o sistema de proteção interno do AxMinions (WorldGuard, etc)
                                        if (owner is Player && !AxMinionsPlugin.integrations.getProtectionIntegration().canBuildAt(owner, block.location)) {
                                            return@fastFor
                                        }

                                        // Quebra o bloco MANUALMENTE (sem disparar BlockBreakEvent)
                                        // Isso impede que o KGenerators dê o item para o jogador.
                                        val tool = minion.getTool() ?: ItemStack(Material.AIR)
                                        val result = deliverMinedBlockDrops(minion, block, NMSHandler.get().getDrops(block, tool), handleIsland = false)
                                        amount += result.first
                                        if (!result.second) {
                                            damageCount += result.first
                                        }
                                        gen.scheduleGeneratorRegeneration()
                                    }
                                    return@fastFor
                                }
                            } catch (e: Throwable) {
                                // Ignora erros do KGenerators
                            }
                        }

                        if (isGenerator) return@fastFor

                        val canBreak = if (generatorMode) {
                            MinionUtils.isStoneGenerator(location)
                        } else {
                            whitelist.contains(location.block.type)
                        }

                        if (canBreak) {
                            val block = location.block

                            if (AxMinionsPlugin.integrations.kGeneratorsIntegration && Main.getPlacedGenerators().getLoaded(location) != null) {
                                return@fastFor
                            }

                            val tool = minion.getTool() ?: ItemStack(Material.AIR)
                            xp += NMSHandler.get().getExp(block, tool)
                            val result = deliverMinedBlockDrops(minion, block, NMSHandler.get().getDrops(block, tool), handleIsland = true)
                            amount += result.first
                            if (!result.second) {
                                damageCount += result.first
                            }
                        }
                    }
            }
        }

        val xpMult = AbilityEngine.getXpMultiplier(minion)
        val finalXp = (xp * xpMult).toInt().coerceAtLeast(0)
        val maxStorage = AbilityEngine.getEffectiveStorage(minion, minion.getType().getLong("storage", minion.getLevel()).toDouble())
        val coerced = (minion.getStorage() + finalXp).coerceIn(0.0, maxStorage)
        minion.setStorage(coerced)
        minion.setActions(minion.getActionAmount() + amount)

        if (amount > 0 && AbilityEngine.isMinerOverdriveTriggered(minion)) {
            val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
            minionImpl.setNextAction((minionImpl.getNextAction() / 2).coerceAtLeast(5))
        }

        for (i in 0..<damageCount) {
            minion.damageTool()
        }
        AbilityEngine.processPeriodicCycle(minion)
    }

    private fun deliverMinedBlockDrops(
        minion: Minion,
        block: org.bukkit.block.Block,
        rawDrops: List<ItemStack>,
        handleIsland: Boolean
    ): Pair<Int, Boolean> {
        val drops = rawDrops.toMutableList()
        val outcome = AbilityEngine.processActionDrops(minion, drops, block = block)
        block.type = Material.AIR
        if (handleIsland) {
            AxMinionsAPI.INSTANCE.getIntegrations().getIslandIntegration()?.handleBlockBreak(block)
        }
        val count = drops.sumOf { it.amount }
        minion.addToContainerOrDrop(drops)
        AbilityEngine.compactChestForCandidates(minion, outcome.compactCandidates)
        return count to outcome.cancelToolDamage
    }
}