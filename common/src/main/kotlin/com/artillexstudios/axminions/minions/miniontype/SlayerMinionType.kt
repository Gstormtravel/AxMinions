package com.artillexstudios.axminions.minions.miniontype

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.api.warnings.Warnings
import com.artillexstudios.axminions.integrations.protection.EliteMobsProtection
import com.artillexstudios.axminions.integrations.stacker.WildStackerIntegration
import com.artillexstudios.axminions.listeners.MinionDamageListener
import com.artillexstudios.axminions.minions.MinionTicker
import com.artillexstudios.axminions.nms.NMSHandler
import com.artillexstudios.axminions.skills.AbilityEngine
import com.artillexstudios.axminions.utils.ItemUtils
import com.bgsoftware.wildstacker.api.WildStackerAPI
import com.bgsoftware.wildstacker.api.objects.StackedSpawner
import kotlin.math.roundToInt
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class SlayerMinionType : MinionType("slayer", AxMinionsPlugin.INSTANCE.getResource("minions/slayer.yml")!!) {

    override fun shouldRun(minion: Minion): Boolean {
        return MinionTicker.getTick() % minion.getNextAction() == 0L
    }

    override fun onToolDirty(minion: Minion) {
        val minionImpl = minion as com.artillexstudios.axminions.minions.Minion
        minionImpl.setRange(minion.getEffectiveRange())
        val tool = com.artillexstudios.axminions.utils.Enchantments.EFFICIENCY
            ?.let { minion.getTool()?.getEnchantmentLevel(it)?.div(10.0) } ?: 0.1
        val efficiency = 1.0 - if (tool > 0.9) 0.9 else tool
        minionImpl.setNextAction((minion.getEffectiveSpeed() * efficiency).roundToInt().coerceAtLeast(10))
    }

    override fun run(minion: Minion) {
        val chestLoc = minion.getLinkedChest()
        
        if (chestLoc != null && chestLoc.world?.isChunkLoaded(chestLoc.blockX shr 4, chestLoc.blockZ shr 4) == true) {
            val type = chestLoc.block.type
            if (type != Material.CHEST && type != Material.TRAPPED_CHEST && type != Material.BARREL) {
                minion.setLinkedChest(null)
            } else {
                val inv = minion.getLinkedInventory()
                if (inv != null && type == Material.CHEST) {
                    if (inv !is DoubleChestInventory && hasChestOnSide(chestLoc.block)) {
                        minion.setLinkedChest(chestLoc)
                    } else if (inv is DoubleChestInventory && !hasChestOnSide(chestLoc.block)) {
                        minion.setLinkedChest(chestLoc)
                    }
                }
            }
        }

        // Ataca independentemente de ter baú e sem mostrar mensagem de aviso de falta de baú
        if (!minion.canUseTool()) return

        val world = minion.getLocation().world ?: return
        val center = minion.getLocation()
        val range = minion.getRange()
        val rangeSquared = range * range
        val attackedEntities = mutableSetOf<UUID>()

        // Atacar
        world.getNearbyEntities(center, range, range, range)
            .filterIsInstance<LivingEntity>()
            .fastFor { entity ->
                if (!canAttack(minion, entity)) return@fastFor
                attackEntityWithSkills(minion, entity)
                attackedEntities.add(entity.uniqueId)
            }

        // Atacar WildStacker
        if (AxMinionsPlugin.integrations.getStackerIntegration() is WildStackerIntegration) {
            try {
                val systemManager = WildStackerAPI.getWildStacker().systemManager
                val chunk = center.chunk
                for (x in -1..1) {
                    for (z in -1..1) {
                        val targetChunk = world.getChunkAt(chunk.x + x, chunk.z + z)
                        systemManager.getStackedSpawners(targetChunk).forEach { spawner: StackedSpawner ->
                            val linkedEntity = spawner.linkedEntity
                            if (linkedEntity != null && linkedEntity.uniqueId !in attackedEntities) {
                                if (linkedEntity.location.distanceSquared(center) <= rangeSquared) {
                                    if (canAttack(minion, linkedEntity)) {
                                        attackEntityWithSkills(minion, linkedEntity)
                                        attackedEntities.add(linkedEntity.uniqueId)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Bukkit.getLogger().warning("[AxMinions] Erro no WildStacker: ${e.message}")
            }
        }

        // Habilidade AOE_CLEAVE: expande raio em +2.0 para alvos extras proximos
        val cleaveMax = AbilityEngine.getSlayerAoeTargets(minion)
        if (cleaveMax > 0) {
            var cleaved = 0
            val cleaveRadius = range + 2.0
            world.getNearbyEntities(center, cleaveRadius, cleaveRadius, cleaveRadius)
                .filterIsInstance<LivingEntity>()
                .fastFor { extra ->
                    if (cleaved >= cleaveMax) return@fastFor
                    if (extra.uniqueId in attackedEntities) return@fastFor
                    if (!canAttack(minion, extra)) return@fastFor
                    attackEntityWithSkills(minion, extra)
                    attackedEntities.add(extra.uniqueId)
                    cleaved++
                }
        }

        // Coletar do chão imediatamente e agendar varredura em +2 e +8 ticks (<= 10 ticks) se matou/atacou algo
        collectNearbyDrops(minion)
        AbilityEngine.processPeriodicCycle(minion)

        if (attackedEntities.isNotEmpty()) {
            val loc = minion.getLocation()
            Scheduler.get().runLaterAt(loc, Runnable {
                if (minion.isTicking()) {
                    collectNearbyDrops(minion)
                }
            }, 2L)
            Scheduler.get().runLaterAt(loc, Runnable {
                if (minion.isTicking()) {
                    collectNearbyDrops(minion)
                }
            }, 8L)
        }
    }

    private fun collectNearbyDrops(minion: Minion) {
        val world = minion.getLocation().world ?: return
        val center = minion.getLocation()
        val range = minion.getRange()
        val linkedInv = minion.getLinkedInventory() ?: return

        if (linkedInv.firstEmpty() == -1) {
            Warnings.CONTAINER_FULL.display(minion)
            return
        }
        Warnings.remove(minion, Warnings.CONTAINER_FULL)

        val items = world.getNearbyEntities(center, range, range, range).filterIsInstance<Item>()
        items.fastFor { item ->
            if (!item.isValid) return@fastFor
            val currentInv = minion.getLinkedInventory() ?: return@fastFor
            if (currentInv.firstEmpty() == -1) {
                Warnings.CONTAINER_FULL.display(minion)
                return@fastFor
            }

            // Verifica o PDC no Item entity OU no ItemMeta do ItemStack
            val ownerUuidStr = item.persistentDataContainer.get(MinionDamageListener.SLAYER_DROP_KEY, PersistentDataType.STRING)
                ?: item.itemStack.itemMeta?.persistentDataContainer?.get(MinionDamageListener.SLAYER_DROP_KEY, PersistentDataType.STRING)
                ?: return@fastFor

            if (ownerUuidStr != minion.getOwnerUUID().toString()) return@fastFor

            val integration = AxMinionsPlugin.integrations.getStackerIntegration()
            val total = integration.getStackSize(item)
            if (total <= 0L) return@fastFor

            // Remove todo NBT/PDC interno para manter hasItemMeta() == false nos itens vanilla (/sell do HeroVender)
            val template = ItemUtils.stripAxMinionsNbt(item.itemStack)
            val maxStack = template.maxStackSize.coerceAtLeast(1)
            template.amount = 1

            var remaining = total
            var collected = 0L

            while (remaining > 0L) {
                val chunkSize = remaining.coerceAtMost(maxStack.toLong()).toInt()
                val piece = template.clone()
                piece.amount = chunkSize

                val leftover = minion.addWithRemaining(piece) ?: break
                val failed = leftover.values.sumOf { it.amount }
                val inserted = chunkSize - failed

                if (inserted <= 0) {
                    Warnings.CONTAINER_FULL.display(minion)
                    break
                }

                collected += inserted.toLong()
                remaining -= inserted.toLong()
            }

            if (collected > 0L) {
                minion.setActions(minion.getActionAmount() + collected)
                AbilityEngine.compactChestIfAbilityEnabled(minion, template.type)
                if (com.artillexstudios.axminions.api.config.Config.DEBUG()) {
                    Bukkit.getLogger().info("[AxMinions-Slayer] Coletou $collected de ${template.type} do chão para o baú do minion.")
                }
            }

            if (remaining <= 0L) {
                item.remove()
            } else {
                integration.setStackSize(item, remaining.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
            }
        }
    }

    private fun attackEntityWithSkills(minion: Minion, entity: LivingEntity) {
        val stackSize = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(entity)
        MinionDamageListener.recordPreAttack(entity.uniqueId, minion, stackSize)
        MinionDamageListener.recordRecentAttack(minion.getOwnerUUID(), entity.location)
        NMSHandler.get().attack(minion, entity)

        val extraHits = AbilityEngine.getExtraStackKillCount(minion, entity) +
            if (AbilityEngine.shouldFrenzyDoubleStrike(minion)) 1 else 0
        if (extraHits > 0) {
            for (i in 0 until extraHits) {
                if (!entity.isValid || entity.isDead) break
                val curStack = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(entity)
                MinionDamageListener.recordPreAttack(entity.uniqueId, minion, curStack)
                MinionDamageListener.recordRecentAttack(minion.getOwnerUUID(), entity.location)
                NMSHandler.get().attack(minion, entity)
            }
        }
        if (!AbilityEngine.shouldSaveDurability(minion)) {
            minion.damageTool()
        }
    }

    private fun canAttack(minion: Minion, entity: LivingEntity): Boolean {
        if (entity is Player) return false
        if (EliteMobsProtection.isEliteEntity(entity)) return false
        if (entity is AbstractVillager) return false
        if (entity is Tameable && entity.isTamed) return false
        if (entity is AbstractHorse && entity.isTamed) return false
        if (entity is Steerable && entity.hasSaddle()) return false
        if (entity.customName != null) {
            val nameStr = ChatColor.stripColor(entity.name) ?: entity.name
            if (!nameStr.contains(" x") && !nameStr.matches(Regex("^\\d+x.*|^x\\d+.*"))) return false
        }
        if (entity.hasMetadata("NPC")) return false
        if (!getConfig().getBoolean("damage-animals") && NMSHandler.get().isAnimal(entity)) return false
        return true
    }
}