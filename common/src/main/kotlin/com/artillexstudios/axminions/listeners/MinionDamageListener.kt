package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.events.MinionKillEntityEvent
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.integrations.protection.EliteMobsProtection
import com.artillexstudios.axminions.integrations.stacker.RoseStackerIntegration
import com.artillexstudios.axminions.integrations.stacker.WildStackerIntegration
import com.artillexstudios.axminions.nms.NMSHandler
import com.artillexstudios.axminions.skills.AbilityEngine
import com.artillexstudios.axminions.utils.ItemUtils
import dev.rosewood.rosestacker.api.RoseStackerAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.lang.ref.WeakReference
import java.util.concurrent.ThreadLocalRandom
import java.util.Random
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.entity.ItemSpawnEvent

class MinionDamageListener : Listener {
    private val random = Random()
    private val minionKilledEntities = ConcurrentHashMap<UUID, UUID>()
    companion object {
        data class RecentKill(
            val ownerUuid: UUID,
            val worldName: String,
            val x: Double,
            val y: Double,
            val z: Double,
            val timestampMs: Long
        )

        val SLAYER_DROP_KEY = NamespacedKey(AxMinionsPlugin.INSTANCE, "slayer_drop")
        private const val RECENT_KILL_TTL_MS = 1200L
        private const val RECENT_KILL_RADIUS_SQ = 12.25 // 3.5 blocks
        private val recentKills = CopyOnWriteArrayList<RecentKill>()
        private val killedEntityMinions = ConcurrentHashMap<UUID, WeakReference<Minion>>()
        private val preAttackStackSizes = ConcurrentHashMap<UUID, Long>()

        fun recordRecentAttack(ownerUuid: UUID, loc: Location) {
            val worldName = loc.world?.name ?: return
            val now = System.currentTimeMillis()
            recentKills.removeIf { now - it.timestampMs > RECENT_KILL_TTL_MS }
            recentKills.add(RecentKill(ownerUuid, worldName, loc.x, loc.y, loc.z, now))
        }

        fun recordPreAttack(entityUuid: UUID, minion: Minion, stackSize: Long) {
            if (killedEntityMinions.size > 2048) killedEntityMinions.clear()
            if (preAttackStackSizes.size > 2048) preAttackStackSizes.clear()
            killedEntityMinions[entityUuid] = WeakReference(minion)
            preAttackStackSizes[entityUuid] = stackSize.coerceAtLeast(1L)
        }
    }

    private fun recordRecentKill(ownerUuid: UUID, loc: Location) {
        recordRecentAttack(ownerUuid, loc)
    }

    private fun findRecentKillOwner(loc: Location): UUID? {
        val worldName = loc.world?.name ?: return null
        val now = System.currentTimeMillis()
        var matched: UUID? = null
        val expired = mutableListOf<RecentKill>()
        for (entry in recentKills) {
            if (now - entry.timestampMs > RECENT_KILL_TTL_MS) {
                expired.add(entry)
                continue
            }
            if (entry.worldName != worldName) continue
            val dx = entry.x - loc.x
            val dy = entry.y - loc.y
            val dz = entry.z - loc.z
            if (dx * dx + dy * dy + dz * dz <= RECENT_KILL_RADIUS_SQ) {
                matched = entry.ownerUuid
                break
            }
        }
        if (expired.isNotEmpty()) {
            recentKills.removeAll(expired.toSet())
        }
        return matched
    }

    @EventHandler
    fun onMinionKillEntityEvent(event: MinionKillEntityEvent) {
        val minion = event.minion
        val stackSize = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(event.target).coerceAtLeast(1L)
        val recordedSize = preAttackStackSizes[event.target.uniqueId]
        val entitySize = maxOf(stackSize, recordedSize ?: 1L)
        killedEntityMinions[event.target.uniqueId] = WeakReference(minion)
        preAttackStackSizes[event.target.uniqueId] = entitySize

        if (AxMinionsPlugin.integrations.getStackerIntegration() is RoseStackerIntegration && entitySize > 1) {
            val chance = minion.getType().getDouble("chance-kill-stacked-amount", minion.getLevel())
            val amount = minion.getType().getLong("stacked-amount", minion.getLevel())
            val stackedEntity = RoseStackerAPI.getInstance().getStackedEntity(event.target)!!
            val amountToKill = if (entitySize > amount) amount else entitySize
            if (random.nextDouble() <= chance) {
                stackedEntity.killPartialStack(null, amountToKill.toInt())
                minion.setActions(minion.getActionAmount() + (amountToKill - 1L))
            }
        } else {
            event.minion.setActions(event.minion.getActionAmount() + entitySize)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        val minion = NMSHandler.get().getMinion() ?: return
        val entity = event.entity as? LivingEntity ?: return
        if (EliteMobsProtection.isEliteEntity(entity)) {
            event.isCancelled = true
            return
        }
        val stackSize = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(entity).coerceAtLeast(1L)
        minionKilledEntities[entity.uniqueId] = minion.getOwnerUUID()
        killedEntityMinions[entity.uniqueId] = WeakReference(minion)
        preAttackStackSizes.putIfAbsent(entity.uniqueId, stackSize)
        if (entity.health - event.finalDamage <= 0.0) {
            recordRecentKill(minion.getOwnerUUID(), entity.location)
            Bukkit.getPluginManager().callEvent(MinionKillEntityEvent(minion, entity))
        }
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity is LivingEntity) {
            val minion = NMSHandler.get().getMinion()
            if (minion != null || event.damager.uniqueId == NMSHandler.get().getAnimalUUID()) {
                val resolvedMinion = minion ?: NMSHandler.get().getMinion() ?: return
                if (EliteMobsProtection.isEliteEntity(entity)) {
                    event.isCancelled = true
                    return
                }
                val stackSize = AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(entity).coerceAtLeast(1L)
                minionKilledEntities[entity.uniqueId] = resolvedMinion.getOwnerUUID()
                killedEntityMinions[entity.uniqueId] = WeakReference(resolvedMinion)
                preAttackStackSizes.putIfAbsent(entity.uniqueId, stackSize)
                if (entity.health - event.finalDamage <= 0.0) {
                    recordRecentKill(resolvedMinion.getOwnerUUID(), entity.location)
                    Bukkit.getPluginManager().callEvent(MinionKillEntityEvent(resolvedMinion, entity))
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDeath(event: EntityDeathEvent) {
        val currentMinion = NMSHandler.get().getMinion()
        val mappedMinion = killedEntityMinions.remove(event.entity.uniqueId)?.get()
        val resolvedMinion = currentMinion ?: mappedMinion
        val ownerUuid = resolvedMinion?.getOwnerUUID()
            ?: minionKilledEntities.remove(event.entity.uniqueId)
            ?: return
        minionKilledEntities.remove(event.entity.uniqueId)
        val preStackSize = preAttackStackSizes.remove(event.entity.uniqueId)
            ?: AxMinionsPlugin.integrations.getStackerIntegration().getStackSize(event.entity).coerceAtLeast(1L)
        recordRecentKill(ownerUuid, event.entity.location)

        val stacker = AxMinionsPlugin.integrations.getStackerIntegration()
        val rawDroppedExp = event.droppedExp.coerceAtLeast(0)
        val baseTotalExp = if (rawDroppedExp > 0) {
            if (stacker is WildStackerIntegration && preStackSize > 1L && rawDroppedExp < preStackSize) {
                stacker.calculateStackedExp(event.entity, rawDroppedExp)
            } else if (preStackSize > 1L && rawDroppedExp < preStackSize) {
                (rawDroppedExp.toLong() * preStackSize).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            } else {
                rawDroppedExp
            }
        } else {
            (ThreadLocalRandom.current().nextInt(1, 4).toLong() * preStackSize).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }
        val xpMult = resolvedMinion?.let { AbilityEngine.getXpMultiplier(it) } ?: 1.0
        val finalExp = (baseTotalExp * xpMult).toInt().coerceAtLeast(0)

        if (event.droppedExp > 0) {
            event.droppedExp = 0
        }

        if (resolvedMinion != null && finalExp > 0) {
            val maxStorage = AbilityEngine.getEffectiveStorage(
                resolvedMinion,
                resolvedMinion.getType().getLong("storage", resolvedMinion.getLevel()).toDouble()
            )
            val coerced = (resolvedMinion.getStorage() + finalExp).coerceIn(0.0, maxStorage)
            resolvedMinion.setStorage(coerced)
            if (com.artillexstudios.axminions.api.config.Config.DEBUG()) {
                Bukkit.getLogger().info("[AxMinions-Slayer] XP armazenado no minion: +$finalExp (raw=$rawDroppedExp, stack=$preStackSize, mult=$xpMult, total=$coerced/$maxStorage).")
            }
        } else if (finalExp > 0) {
            event.entity.killer?.takeIf { it.isOnline }?.giveExp(finalExp)
        }

        if (resolvedMinion != null && event.drops.isNotEmpty() && resolvedMinion.getLinkedInventory() != null &&
            AbilityEngine.shouldSendDropsDirectToChest(resolvedMinion, event.entity)
        ) {
            val cleaned = event.drops
                .filter { !it.type.isAir && it.amount > 0 }
                .map { ItemUtils.stripAxMinionsNbt(it) }
                .toMutableList()
            val outcome = AbilityEngine.processActionDrops(resolvedMinion, cleaned, entity = event.entity)
            val leftovers = mutableListOf<org.bukkit.inventory.ItemStack>()
            var insertedTotal = 0L

            for (stack in cleaned) {
                val origAmt = stack.amount
                val rem = resolvedMinion.addWithRemaining(stack)
                if (rem == null) {
                    leftovers.add(stack)
                } else {
                    val notInserted = rem.values.sumOf { it.amount }
                    insertedTotal += (origAmt - notInserted).coerceAtLeast(0).toLong()
                    rem.values.forEach { if (!it.type.isAir && it.amount > 0) leftovers.add(it) }
                }
            }

            if (insertedTotal > 0L) {
                resolvedMinion.setActions(resolvedMinion.getActionAmount() + insertedTotal)
                AbilityEngine.compactChestForCandidates(resolvedMinion, outcome.compactCandidates)
            }

            event.drops.clear()
            for (leftover in leftovers) {
                event.drops.add(leftover)
            }
            return
        }

        if (resolvedMinion != null && event.drops.isNotEmpty()) {
            AbilityEngine.processActionDrops(resolvedMinion, event.drops, entity = event.entity)
        }

        var taggedCount = 0
        for (drop in event.drops) {
            if (drop.type.isAir || drop.amount <= 0) continue
            val meta = drop.itemMeta ?: continue
            meta.persistentDataContainer.set(SLAYER_DROP_KEY, PersistentDataType.STRING, ownerUuid.toString())
            drop.itemMeta = meta
            taggedCount++
        }

        if (com.artillexstudios.axminions.api.config.Config.DEBUG()) {
            Bukkit.getLogger().info("[AxMinions-Slayer] Mob ${event.entity.type} (stack=$preStackSize) morto. $taggedCount drops etiquetados para $ownerUuid.")
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDropItem(event: EntityDropItemEvent) {
        val itemStack = event.itemDrop.itemStack
        val meta = itemStack.itemMeta
        val ownerUuidStr = meta?.persistentDataContainer?.get(SLAYER_DROP_KEY, PersistentDataType.STRING)
            ?: NMSHandler.get().getMinion()?.getOwnerUUID()?.toString()
            ?: findRecentKillOwner(event.itemDrop.location)?.toString()
            ?: return
        event.itemDrop.persistentDataContainer.set(SLAYER_DROP_KEY, PersistentDataType.STRING, ownerUuidStr)
        event.itemDrop.itemStack = ItemUtils.stripAxMinionsNbt(itemStack)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onItemSpawn(event: ItemSpawnEvent) {
        val itemEntity = event.entity
        val itemStack = itemEntity.itemStack
        val metaOwner = itemStack.itemMeta?.persistentDataContainer?.get(SLAYER_DROP_KEY, PersistentDataType.STRING)
        val ownerUuidStr = metaOwner
            ?: NMSHandler.get().getMinion()?.getOwnerUUID()?.toString()
            ?: findRecentKillOwner(itemEntity.location)?.toString()

        if (ownerUuidStr != null) {
            itemEntity.persistentDataContainer.set(SLAYER_DROP_KEY, PersistentDataType.STRING, ownerUuidStr)
            itemEntity.itemStack = ItemUtils.stripAxMinionsNbt(itemStack)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemMerge(event: ItemMergeEvent) {
        val ownerUuidStr = event.entity.persistentDataContainer.get(SLAYER_DROP_KEY, PersistentDataType.STRING)
            ?: event.target.persistentDataContainer.get(SLAYER_DROP_KEY, PersistentDataType.STRING)
            ?: return
        event.target.persistentDataContainer.set(SLAYER_DROP_KEY, PersistentDataType.STRING, ownerUuidStr)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityDamageByEntityEvente(event: EntityDamageByEntityEvent) {
        if (event.damager.uniqueId != NMSHandler.get().getAnimalUUID()) return
        if (event.entity is Player) {
            event.isCancelled = true
            event.damage = 0.0
            return
        }
        event.isCancelled = false
    }
}