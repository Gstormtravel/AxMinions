package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.skills.AbilityEngine
import com.artillexstudios.axminions.utils.Enchantments
import com.artillexstudios.axminions.utils.ItemUtils
import kotlin.math.roundToInt
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.DoubleChestInventory

class CollectorMinionType : MinionType("collector", AxMinionsPlugin.INSTANCE.getResource("minions/collector.yml")!!) {

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
        if (minion.getLinkedChest() == null) {
            Warnings.NO_CONTAINER.display(minion)
            return
        }

        val chestLoc = minion.getLinkedChest()!!
        
        // Se o chunk do baú ainda não estiver carregado, espera o próximo tick silenciosamente
        if (chestLoc.world?.isChunkLoaded(chestLoc.blockX shr 4, chestLoc.blockZ shr 4) == false) {
            return
        }

        val type = chestLoc.block.type

        // Só desvincula se o bloco realmente não for um baú válido
        if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.BARREL) {
            Warnings.NO_CONTAINER.display(minion)
            minion.setLinkedChest(null)
            return
        }

        // Lógica para Double Chest
        if (type == Material.CHEST && minion.getLinkedInventory() !is DoubleChestInventory && hasChestOnSide(chestLoc.block)) {
            minion.setLinkedChest(chestLoc)
        }

        if (type == Material.CHEST && minion.getLinkedInventory() is DoubleChestInventory && !hasChestOnSide(chestLoc.block)) {
            minion.setLinkedChest(chestLoc)
        }

        // Se o inventário for nulo, NÃO apaga o link. Apenas avisa e espera o próximo tick (Bug fix do Boot)
        if (minion.getLinkedInventory() == null) {
            Warnings.NO_CONTAINER.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_CONTAINER)

        if (minion.getLinkedInventory()?.firstEmpty() == -1) {
            Warnings.CONTAINER_FULL.display(minion)
        }

        if (!minion.canUseTool()) {
            Warnings.NO_TOOL.display(minion)
            return
        }

        Warnings.remove(minion, Warnings.NO_TOOL)

        val entities = minion.getLocation().world?.getNearbyEntities(
            minion.getLocation(),
            minion.getRange(),
            minion.getRange(),
            minion.getRange()
        )

        entities?.filterIsInstance<Item>()?.fastFor { item ->
            if (minion.getLinkedInventory()?.firstEmpty() == -1) {
                Warnings.CONTAINER_FULL.display(minion)
                return@fastFor
            }

            val integration = AxMinionsPlugin.integrations.getStackerIntegration()
            val total = integration.getStackSize(item)
            if (total <= 0L) return@fastFor

            // Template = a "identidade" do item limpa de NBT interno (para vendas no HeroVender).
            // amount é setado por chunk a cada iteração.
            val template = ItemUtils.stripAxMinionsNbt(item.itemStack)
            if (AbilityEngine.isVoidTrashFiltered(minion, template)) {
                minion.setActions(minion.getActionAmount() + total)
                item.remove()
                return@fastFor
            }
            val maxStack = template.maxStackSize.coerceAtLeast(1)
            template.amount = 1

            var remaining = total
            var collected = 0L

            // Quebra o stack total em pedaços de no máximo maxStackSize,
            // pois o ItemStack vanilla não segura mais do que isso.
            while (remaining > 0L) {
                val chunk = remaining.coerceAtMost(maxStack.toLong()).toInt()
                val piece = template.clone()
                piece.amount = chunk

                // addWithRemaining devolve o que NÃO coube no baú (HashMap<slot, sobra>).
                // O `piece` em si não é modificado pelo Bukkit.
                val leftover = minion.addWithRemaining(piece) ?: break

                val failed = leftover.values.sumOf { it.amount }
                val inserted = chunk - failed

                if (inserted <= 0) {
                    if (AbilityEngine.tryOverflowToOwnerEnderChest(minion, piece)) {
                        collected += chunk.toLong()
                        remaining -= chunk.toLong()
                        continue
                    }
                    // Baú encheu no meio do caminho — para tudo e mantém o resto no chão.
                    Warnings.CONTAINER_FULL.display(minion)
                    break
                }

                collected += inserted.toLong()
                remaining -= inserted.toLong()
            }

            if (collected > 0L) {
                minion.setActions(minion.getActionAmount() + collected)
                if (!AbilityEngine.shouldSaveDurability(minion)) {
                    minion.damageTool()
                }
                AbilityEngine.compactChestIfAbilityEnabled(minion, template.type)
            }

            if (remaining <= 0L) {
                item.remove()
            } else {
                // Devolve ao item no chão APENAS o que realmente sobrou.
                integration.setStackSize(item, remaining.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
            }
        }

        AbilityEngine.collectNearbyXpOrbsIfEnabled(minion)
        AbilityEngine.processPeriodicCycle(minion)
    }
}