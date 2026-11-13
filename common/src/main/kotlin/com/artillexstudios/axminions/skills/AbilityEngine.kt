package com.artillexstudios.axminions.skills

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.skills.AbilityConditionSpec
import com.artillexstudios.axminions.api.skills.AbilityEffectSpec
import com.artillexstudios.axminions.api.skills.AbilityTrigger
import com.artillexstudios.axminions.api.skills.CustomAbilitySpec
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.utils.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.type.Farmland
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min
import kotlin.math.roundToInt

object AbilityEngine {
    private val cooldowns = ConcurrentHashMap<String, Long>()

    private val WOOD_SPECIES = listOf(
        "OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA",
        "DARK_OAK", "MANGROVE", "CHERRY", "PALE_OAK", "POPLAR"
    )

    private val SMELT_MAP: Map<Material, Material> = buildMap {
        putIfPresent("RAW_IRON", "IRON_INGOT")
        putIfPresent("IRON_ORE", "IRON_INGOT")
        putIfPresent("DEEPSLATE_IRON_ORE", "IRON_INGOT")
        putIfPresent("RAW_GOLD", "GOLD_INGOT")
        putIfPresent("GOLD_ORE", "GOLD_INGOT")
        putIfPresent("DEEPSLATE_GOLD_ORE", "GOLD_INGOT")
        putIfPresent("NETHER_GOLD_ORE", "GOLD_INGOT")
        putIfPresent("RAW_COPPER", "COPPER_INGOT")
        putIfPresent("COPPER_ORE", "COPPER_INGOT")
        putIfPresent("DEEPSLATE_COPPER_ORE", "COPPER_INGOT")
        putIfPresent("ANCIENT_DEBRIS", "NETHERITE_SCRAP")
        putIfPresent("BEEF", "COOKED_BEEF")
        putIfPresent("PORKCHOP", "COOKED_PORKCHOP")
        putIfPresent("CHICKEN", "COOKED_CHICKEN")
        putIfPresent("MUTTON", "COOKED_MUTTON")
        putIfPresent("RABBIT", "COOKED_RABBIT")
        putIfPresent("COD", "COOKED_COD")
        putIfPresent("SALMON", "COOKED_SALMON")
        putIfPresent("POTATO", "BAKED_POTATO")
        putIfPresent("KELP", "DRIED_KELP")
        putIfPresent("SAND", "GLASS")
        putIfPresent("RED_SAND", "GLASS")
        putIfPresent("CLAY_BALL", "BRICK")
        putIfPresent("NETHERRACK", "NETHER_BRICK")
        for (species in WOOD_SPECIES) {
            putIfPresent("${species}_LOG", "CHARCOAL")
            putIfPresent("${species}_WOOD", "CHARCOAL")
            putIfPresent("STRIPPED_${species}_LOG", "CHARCOAL")
            putIfPresent("STRIPPED_${species}_WOOD", "CHARCOAL")
        }
    }

    private val PLANKS_MAP: Map<Material, Material> = buildMap {
        for (species in WOOD_SPECIES) {
            putIfPresent("${species}_LOG", "${species}_PLANKS")
            putIfPresent("${species}_WOOD", "${species}_PLANKS")
            putIfPresent("STRIPPED_${species}_LOG", "${species}_PLANKS")
            putIfPresent("STRIPPED_${species}_WOOD", "${species}_PLANKS")
        }
        putIfPresent("CRIMSON_STEM", "CRIMSON_PLANKS")
        putIfPresent("CRIMSON_HYPHAE", "CRIMSON_PLANKS")
        putIfPresent("STRIPPED_CRIMSON_STEM", "CRIMSON_PLANKS")
        putIfPresent("STRIPPED_CRIMSON_HYPHAE", "CRIMSON_PLANKS")
        putIfPresent("WARPED_STEM", "WARPED_PLANKS")
        putIfPresent("WARPED_HYPHAE", "WARPED_PLANKS")
        putIfPresent("STRIPPED_WARPED_STEM", "WARPED_PLANKS")
        putIfPresent("STRIPPED_WARPED_HYPHAE", "WARPED_PLANKS")
        putIfPresent("BAMBOO_BLOCK", "BAMBOO_PLANKS")
        putIfPresent("STRIPPED_BAMBOO_BLOCK", "BAMBOO_PLANKS")
    }

    private val COMPACT_MAP: Map<Material, Pair<Int, Material>> = buildMap {
        putCompact("WHEAT", 9, "HAY_BLOCK")
        putCompact("MELON_SLICE", 9, "MELON")
        putCompact("BAMBOO", 9, "BAMBOO_BLOCK")
        putCompact("DRIED_KELP", 9, "DRIED_KELP_BLOCK")
        putCompact("RAW_IRON", 9, "RAW_IRON_BLOCK")
        putCompact("IRON_INGOT", 9, "IRON_BLOCK")
        putCompact("RAW_GOLD", 9, "RAW_GOLD_BLOCK")
        putCompact("GOLD_INGOT", 9, "GOLD_BLOCK")
        putCompact("RAW_COPPER", 9, "RAW_COPPER_BLOCK")
        putCompact("COPPER_INGOT", 9, "COPPER_BLOCK")
        putCompact("COAL", 9, "COAL_BLOCK")
        putCompact("DIAMOND", 9, "DIAMOND_BLOCK")
        putCompact("EMERALD", 9, "EMERALD_BLOCK")
        putCompact("LAPIS_LAZULI", 9, "LAPIS_BLOCK")
        putCompact("REDSTONE", 9, "REDSTONE_BLOCK")
        putCompact("NETHERITE_INGOT", 9, "NETHERITE_BLOCK")
        putCompact("GOLD_NUGGET", 9, "GOLD_INGOT")
        putCompact("IRON_NUGGET", 9, "IRON_INGOT")
        putCompact("SLIME_BALL", 9, "SLIME_BLOCK")
        putCompact("BONE_MEAL", 9, "BONE_BLOCK")
        putCompact("RESIN_CLUMP", 9, "RESIN_BLOCK")
        putCompact("QUARTZ", 4, "QUARTZ_BLOCK")
        putCompact("GLOWSTONE_DUST", 4, "GLOWSTONE")
        putCompact("AMETHYST_SHARD", 4, "AMETHYST_BLOCK")
    }

    private val FOSSIL_POOL: List<Material> = listOfNotNull(
        Material.matchMaterial("COAL"),
        Material.matchMaterial("BONE"),
        Material.matchMaterial("AMETHYST_SHARD"),
        Material.matchMaterial("RAW_COPPER"),
        Material.matchMaterial("RAW_IRON")
    )

    private val GEODE_POOL: List<Material> = listOfNotNull(
        Material.matchMaterial("AMETHYST_SHARD"),
        Material.matchMaterial("RAW_GOLD"),
        Material.matchMaterial("DIAMOND"),
        Material.matchMaterial("EMERALD"),
        Material.matchMaterial("QUARTZ")
    )

    private val TRASH_FISHER_INPUTS: Set<Material> = setOfNotNull(
        Material.matchMaterial("BOWL"),
        Material.matchMaterial("LEATHER"),
        Material.matchMaterial("LEATHER_BOOTS"),
        Material.matchMaterial("STICK"),
        Material.matchMaterial("STRING"),
        Material.matchMaterial("ROTTEN_FLESH"),
        Material.matchMaterial("BONE"),
        Material.matchMaterial("INK_SAC"),
        Material.matchMaterial("TRIPWIRE_HOOK"),
        Material.matchMaterial("LILY_PAD")
    )

    private val TRASH_FISHER_TREASURES: List<Material> = listOfNotNull(
        Material.matchMaterial("SALMON"),
        Material.matchMaterial("TROPICAL_FISH"),
        Material.matchMaterial("PRISMARINE_CRYSTALS"),
        Material.matchMaterial("PRISMARINE_SHARD"),
        Material.matchMaterial("NAUTILUS_SHELL"),
        Material.matchMaterial("NAME_TAG")
    )

    private val VOID_DEFAULT_TRASH: Set<Material> = setOfNotNull(
        Material.matchMaterial("ROTTEN_FLESH"),
        Material.matchMaterial("POISONOUS_POTATO"),
        Material.matchMaterial("WHEAT_SEEDS"),
        Material.matchMaterial("BEETROOT_SEEDS"),
        Material.matchMaterial("BOWL"),
        Material.matchMaterial("DIRT"),
        Material.matchMaterial("COBBLESTONE")
    )

    private val RARE_ESSENCE_BY_MOB: Map<EntityType, Material> = buildMap {
        put(EntityType.BLAZE, Material.BLAZE_ROD)
        put(EntityType.GHAST, Material.GHAST_TEAR)
        put(EntityType.ENDERMAN, Material.ENDER_PEARL)
        put(EntityType.CREEPER, Material.GUNPOWDER)
        put(EntityType.SLIME, Material.SLIME_BALL)
        put(EntityType.MAGMA_CUBE, Material.MAGMA_CREAM)
        put(EntityType.SPIDER, Material.SPIDER_EYE)
        put(EntityType.CAVE_SPIDER, Material.FERMENTED_SPIDER_EYE)
        put(EntityType.WITHER_SKELETON, Material.COAL)
        put(EntityType.SHULKER, Material.SHULKER_SHELL)
        EntityType.entries.firstOrNull { it.name == "BREEZE" }?.let { breeze ->
            Material.matchMaterial("BREEZE_ROD")?.let { put(breeze, it) }
        }
    }

    private fun MutableMap<Material, Material>.putIfPresent(src: String, dst: String) {
        val s = Material.matchMaterial(src) ?: return
        val d = Material.matchMaterial(dst) ?: return
        put(s, d)
    }

    private fun MutableMap<Material, Pair<Int, Material>>.putCompact(src: String, count: Int, dst: String) {
        val s = Material.matchMaterial(src) ?: return
        val d = Material.matchMaterial(dst) ?: return
        put(s, count to d)
    }

    data class ActionOutcome(
        val cancelToolDamage: Boolean = false,
        val directToChest: Boolean = false,
        val compactCandidates: Set<Material> = emptySet(),
        val extraStackKills: Int = 0
    )

    data class SellerOutcome(
        val finalMoney: Double,
        val autoDepositVault: Boolean
    )

    data class CrafterBonus(
        val maxBatchCrafts: Int,
        val extraOutputChance: Double,
        val smeltOutputs: Boolean,
        val autoCompactAfter: Boolean,
        val recycleChance: Double
    )

    data class FisherBonus(
        val doubleCatchChance: Double,
        val maxCustomDropsBonus: Int,
        val extraLuckFromAbilities: Double,
        val weatherFrenzyRolls: Int
    )

    fun clearCooldowns() {
        cooldowns.clear()
    }

    fun pruneExpiredCooldowns() {
        if (cooldowns.size <= 256) return
        val now = System.currentTimeMillis()
        cooldowns.entries.removeIf { it.value < now }
    }

    fun getEffectiveRange(minion: Minion): Double {
        val base = minion.getType().getDouble("range", minion.getLevel())
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return base

        var range = base
        val stat = spec.stats["range"]
        if (stat != null) {
            val tier = minion.getAbilityTierCompat("range")
            if (tier > 0) {
                val cap = maxOf(base, stat.maxRangeCap)
                range = (base + tier * stat.modifierPerTier).coerceAtMost(cap)
            }
        }

        // Collector dimensional pull extension
        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier <= 0) continue
            for (eff in ab.effects) {
                if (eff.type == "COLLECTOR_RANGE_BOOST") {
                    range += eff.amount * tier
                }
            }
        }
        return range
    }

    fun getEffectiveSpeed(minion: Minion): Int {
        val base = minion.getType().getLong("speed", minion.getLevel()).toInt().coerceAtLeast(1)
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return base

        var speed = base
        val stat = spec.stats["speed"]
        if (stat != null) {
            val tier = minion.getAbilityTierCompat("speed")
            if (tier > 0) {
                val floor = min(base, stat.minSpeedFloor.coerceAtLeast(1))
                speed = (base + (tier * stat.modifierPerTier).roundToInt()).coerceAtLeast(floor)
            }
        }

        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier <= 0) continue
            for (eff in ab.effects) {
                if (eff.type == "ASSEMBLY_SPEED_REDUCE") {
                    val mult = (1.0 - (eff.amount * tier).coerceIn(0.0, 0.5))
                    speed = (speed * mult).roundToInt().coerceAtLeast(10)
                }
            }
        }
        return speed
    }

    fun getEffectiveStorage(minion: Minion, baseStorage: Double): Double {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return baseStorage
        val stat = spec.stats["storage"] ?: return baseStorage
        val tier = minion.getAbilityTierCompat("storage")
        if (tier <= 0) return baseStorage
        return if (stat.modifierPerTier >= 5.0) {
            baseStorage + tier * stat.modifierPerTier
        } else {
            baseStorage * (1.0 + tier * stat.modifierPerTier)
        }
    }

    fun getEffectiveMultiplier(minion: Minion, baseMultiplier: Double): Double {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return baseMultiplier
        var result = baseMultiplier
        val stat = spec.stats["multiplier"]
        if (stat != null) {
            val tier = minion.getAbilityTierCompat("multiplier")
            if (tier > 0) {
                result += tier * stat.modifierPerTier
            }
        }
        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier <= 0) continue
            for (eff in ab.effects) {
                if (eff.type == "SELL_MULTIPLIER_BOOST") {
                    result += eff.amount * tier
                }
            }
        }
        return result
    }

    fun getEffectiveLuckBonus(minion: Minion): Double {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return 0.0
        var luck = 0.0
        val stat = spec.stats["luck"]
        if (stat != null) {
            val tier = minion.getAbilityTierCompat("luck")
            if (tier > 0) luck += tier * stat.chancePerTier
        }
        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier <= 0) continue
            for (eff in ab.effects) {
                if (eff.type == "FISHER_CRATE_BOOST") {
                    luck += eff.amount * tier
                }
            }
        }
        return luck
    }

    fun getXpMultiplier(minion: Minion): Double {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return 1.0
        var mult = 1.0
        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier <= 0) continue
            for (eff in ab.effects) {
                if (eff.type == "XP_BOOST") {
                    val step = (eff.factor - 1.0).coerceAtLeast(0.1)
                    mult += step * tier
                }
            }
        }
        return mult
    }

    fun shouldSaveDurability(minion: Minion): Boolean {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return false
        val stat = spec.stats["durability_save"] ?: return false
        val tier = minion.getAbilityTierCompat("durability_save")
        if (tier <= 0) return false
        val chance = (tier * stat.chancePerTier).coerceIn(0.0, 95.0)
        return chance > 0.0 && ThreadLocalRandom.current().nextDouble(100.0) < chance
    }

    private fun Minion.getAbilityTierCompat(id: String): Int {
        return getSkillAllocations()[id] ?: 0
    }

    fun getLumberRareDropMultiplier(minion: Minion): Double {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return 1.0
        var bonus = 1.0
        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier <= 0) continue
            for (eff in ab.effects) {
                if (eff.type == "LUMBER_RARE_DROP_BOOST") {
                    bonus += eff.amount * tier
                }
            }
        }
        return bonus
    }

    fun shouldClearTreeLeaves(minion: Minion): Boolean {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return false
        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier > 0 && ab.effects.any { it.type == "CLEAR_TREE_LEAVES" }) {
                return true
            }
        }
        return false
    }

    fun getSlayerAoeTargets(minion: Minion): Int {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return 0
        for (ab in spec.abilities.values) {
            if (ab.trigger != AbilityTrigger.ON_ACTION) continue
            if (ab.effects.none { it.type == "AOE_CLEAVE" }) continue
            val tier = canTriggerAbility(minion, ab, null, null) ?: continue
            val count = ab.effects.first { it.type == "AOE_CLEAVE" }.count.coerceAtLeast(1)
            return count + (tier - 1)
        }
        return 0
    }

    fun shouldFrenzyDoubleStrike(minion: Minion): Boolean {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return false
        for (ab in spec.abilities.values) {
            if (ab.trigger != AbilityTrigger.ON_ACTION) continue
            if (ab.effects.none { it.type == "EXECUTIONER_FRENZY" }) continue
            if (canTriggerAbility(minion, ab, null, null) != null) return true
        }
        return false
    }

    fun isMinerOverdriveTriggered(minion: Minion): Boolean {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return false
        for (ab in spec.abilities.values) {
            if (ab.effects.none { it.type == "SPEED_PULSE" }) continue
            val tier = canTriggerAbility(minion, ab, null, null) ?: continue
            spawnParticle(minion.getLocation(), AbilityEffectSpec(type = "PARTICLE", particle = "ELECTRIC_SPARK", count = 8 * tier))
            return true
        }
        return false
    }

    fun processActionDrops(
        minion: Minion,
        drops: MutableList<ItemStack>,
        block: Block? = null,
        entity: LivingEntity? = null
    ): ActionOutcome {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled || spec.abilities.isEmpty()) return ActionOutcome()

        var cancelToolDamage = false
        var directToChest = false
        var extraStackKills = 0
        val compactCandidates = linkedSetOf<Material>()

        for (ability in spec.abilities.values) {
            if (ability.trigger != AbilityTrigger.ON_ACTION) continue
            val tier = canTriggerAbility(minion, ability, block, entity) ?: continue

            for (effect in ability.effects) {
                when (effect.type) {
                    "AUTO_SMELT" -> applyAutoSmelt(drops, effect)
                    "ORE_COMPACT_SMELT" -> {
                        applyAutoSmelt(drops, effect)
                        compactDropsInPlace(drops, effect, compactCandidates)
                    }
                    "LOGS_TO_PLANKS" -> applyLogsToPlanks(drops, effect)
                    "AUTO_COMPACT" -> compactDropsInPlace(drops, effect, compactCandidates)
                    "MULTIPLY_DROPS" -> multiplyDrops(drops, effect, tier)
                    "CANCEL_TOOL_DAMAGE" -> cancelToolDamage = true
                    "DIRECT_TO_CHEST" -> directToChest = true
                    "MULTI_KILL_STACK" -> extraStackKills += (effect.amount * tier).roundToInt().coerceAtLeast(1)
                    "GIVE_MONEY" -> depositMoney(minion, effect.amount * tier)
                    "GIVE_XP" -> addMinionXp(minion, effect.amount * tier)
                    "EXECUTABLE_ITEM" -> buildExecutableItem(minion, effect)?.let { drops.add(it) }
                    "GIVE_ITEM" -> buildCustomItem(effect)?.let { drops.add(it) }
                    "BONUS_DROP" -> rollPoolDrop(FOSSIL_POOL, tier)?.let { drops.add(it) }
                    "RANDOM_MINERAL_CHEST" -> rollGeodeDrop(tier)?.let { drops.add(it) }
                    "OBSIDIAN_EXTRA_BREAK" -> {
                        if (block?.type == Material.OBSIDIAN || block?.type == Material.CRYING_OBSIDIAN) {
                            drops.add(ItemStack(block.type, 1))
                            cancelToolDamage = true
                        }
                    }
                    "BONUS_LOOTING_ROLL" -> applyBonusLooting(drops, tier)
                    "MOB_HEAD_DROP" -> getMobHead(entity)?.let { drops.add(it) }
                    "MOB_RARE_LOOT" -> getRareMobEssence(entity, tier)?.let { drops.add(it) }
                    "FISHER_TRASH_TO_TREASURE" -> convertFisherTrash(drops)
                    "FISHER_GOLDEN_BONUS" -> rollFisherGoldenBonus(minion, drops, tier)
                    "SEEDS_TO_BONEMEAL" -> convertSeedsToBonemeal(drops)
                    "GOLDEN_CROP_UPGRADE" -> upgradeCropsToGolden(drops)
                    "BONUS_CROP_DROP" -> addBonusCrop(drops, tier)
                    "FOREST_SAP_DROP" -> rollForestSap(tier)?.let { drops.add(it) }
                    "VOID_TRASH" -> filterTrashDrops(drops, effect)
                    "CONSOLE_COMMAND" -> runCommands(minion, effect.commands, tier)
                    "PARTICLE" -> spawnParticle(minion.getLocation(), effect)
                    "SOUND" -> playSound(minion.getLocation(), effect)
                }
            }
        }

        return ActionOutcome(
            cancelToolDamage = cancelToolDamage,
            directToChest = directToChest,
            compactCandidates = compactCandidates,
            extraStackKills = extraStackKills
        )
    }

    fun processPeriodicCycle(minion: Minion) {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled || spec.abilities.isEmpty()) return

        for (ability in spec.abilities.values) {
            if (ability.trigger != AbilityTrigger.ON_PERIODIC) continue
            val tier = canTriggerAbility(minion, ability, null, null) ?: continue

            val itemsToDeposit = mutableListOf<ItemStack>()
            for (effect in ability.effects) {
                when (effect.type) {
                    "GIVE_MONEY" -> depositMoney(minion, effect.amount * tier)
                    "GIVE_XP" -> addMinionXp(minion, effect.amount * tier)
                    "EXECUTABLE_ITEM" -> buildExecutableItem(minion, effect)?.let { itemsToDeposit.add(it) }
                    "GIVE_ITEM" -> buildCustomItem(effect)?.let { itemsToDeposit.add(it) }
                    "CHEST_AUTO_COMPRESS" -> compactWholeLinkedChest(minion)
                    "MERGE_GROUND_ITEMS" -> mergeGroundStacks(minion)
                    "CONTINUOUS_VACUUM" -> collectNearbyXpOrbsIfEnabled(minion)
                    "MOISTURIZE_FARMLAND" -> moisturizeNearbyFarmland(minion)
                    "BONEMEAL_CROP_AURA" -> fertilizeNearbyCrops(minion, tier)
                    "CROP_EXTRA_GROWTH" -> boostNearbyCropGrowth(minion, tier)
                    "CONSOLE_COMMAND" -> runCommands(minion, effect.commands, tier)
                    "PARTICLE" -> spawnParticle(minion.getLocation(), effect)
                    "SOUND" -> playSound(minion.getLocation(), effect)
                }
            }
            if (itemsToDeposit.isNotEmpty()) {
                minion.addToContainerOrDrop(itemsToDeposit)
            }
        }
    }

    fun compactChestForCandidates(minion: Minion, candidates: Set<Material>) {
        if (candidates.isEmpty()) return
        val inv = minion.getLinkedInventory() ?: return

        for (srcMat in candidates) {
            val recipe = COMPACT_MAP[srcMat] ?: continue
            val needed = recipe.first
            val resultMat = recipe.second

            val cleanTotal = countCleanMaterial(inv, srcMat)
            val batches = cleanTotal / needed
            if (batches <= 0) continue

            removeCleanMaterial(inv, srcMat, batches * needed)
            val maxStack = resultMat.maxStackSize.coerceAtLeast(1)
            var left = batches
            while (left > 0) {
                val chunk = left.coerceAtMost(maxStack)
                minion.addToContainerOrDrop(ItemStack(resultMat, chunk))
                left -= chunk
            }
        }
    }

    fun compactWholeLinkedChest(minion: Minion) {
        val inv = minion.getLinkedInventory() ?: return
        val present = linkedSetOf<Material>()
        for (stack in inv.contents) {
            if (stack != null && !stack.type.isAir && COMPACT_MAP.containsKey(stack.type)) {
                present.add(stack.type)
            }
        }
        if (present.isNotEmpty()) {
            compactChestForCandidates(minion, present)
        }
    }

    fun compactChestIfAbilityEnabled(minion: Minion, material: Material) {
        if (!COMPACT_MAP.containsKey(material)) return
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return
        for (ability in spec.abilities.values) {
            if (ability.trigger != AbilityTrigger.ON_ACTION) continue
            val compactEffect = ability.effects.firstOrNull {
                it.type == "AUTO_COMPACT" || it.type == "CHEST_AUTO_COMPRESS"
            } ?: continue
            if (material.name in compactEffect.blacklist) continue
            canTriggerAbility(minion, ability, null, null) ?: continue
            compactChestForCandidates(minion, setOf(material))
            return
        }
    }

    fun isVoidTrashFiltered(minion: Minion, stack: ItemStack): Boolean {
        if (stack.type.isAir || stack.hasItemMeta()) return false
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return false
        for (ab in spec.abilities.values) {
            if (!ab.enabled || minion.getLevel() < ab.minLevel) continue
            val tier = minion.getAbilityTier(ab.id)
            if (tier <= 0) continue
            val eff = ab.effects.firstOrNull { it.type == "VOID_TRASH" } ?: continue
            val set = if (eff.blacklist.isNotEmpty()) {
                stack.type.name in eff.blacklist
            } else {
                stack.type in VOID_DEFAULT_TRASH
            }
            if (set) return true
        }
        return false
    }

    fun tryOverflowToOwnerEnderChest(minion: Minion, stack: ItemStack): Boolean {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return false
        val enabled = spec.abilities.values.any { ab ->
            ab.enabled && minion.getLevel() >= ab.minLevel && minion.getAbilityTier(ab.id) > 0 &&
                ab.effects.any { it.type == "OVERFLOW_TO_ENDERCHEST" }
        }
        if (!enabled) return false
        val owner = Bukkit.getPlayer(minion.getOwnerUUID()) ?: return false
        val leftover = owner.enderChest.addItem(stack)
        return leftover.isEmpty()
    }

    fun collectNearbyXpOrbsIfEnabled(minion: Minion) {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return
        val xpAbility = spec.abilities.values.firstOrNull { ab ->
            ab.enabled && minion.getAbilityTier(ab.id) > 0 && minion.getLevel() >= ab.minLevel &&
                ab.effects.any { it.type == "GIVE_XP" || it.type == "XP_VACUUM" }
        } ?: return

        val world = minion.getLocation().world ?: return
        val range = minion.getRange()
        val maxStorage = getEffectiveStorage(
            minion,
            minion.getType().getDouble("storage", minion.getLevel()).coerceAtLeast(250.0)
        )
        if (minion.getStorage() >= maxStorage) return

        val tier = minion.getAbilityTier(xpAbility.id)
        val mult = (1.0 + (tier - 1) * 0.25) * getXpMultiplier(minion)
        for (orb in world.getNearbyEntities(minion.getLocation(), range, range, range).filterIsInstance<ExperienceOrb>()) {
            if (!orb.isValid || orb.experience <= 0) continue
            val gained = orb.experience * mult
            minion.setStorage((minion.getStorage() + gained).coerceIn(0.0, maxStorage))
            orb.remove()
            if (minion.getStorage() >= maxStorage) break
        }
    }

    fun processSaplingFertilizer(minion: Minion, saplingBlock: Block) {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return
        for (ability in spec.abilities.values) {
            if (ability.trigger != AbilityTrigger.ON_ACTION) continue
            if (ability.effects.none { it.type == "BONEMEAL_SAPLING" }) continue
            canTriggerAbility(minion, ability, saplingBlock, null) ?: continue
            saplingBlock.applyBoneMeal(BlockFace.UP)
            for (effect in ability.effects) {
                if (effect.type == "PARTICLE") spawnParticle(saplingBlock.location.add(0.5, 0.5, 0.5), effect)
                if (effect.type == "SOUND") playSound(saplingBlock.location, effect)
            }
        }
    }

    fun shouldSendDropsDirectToChest(minion: Minion, entity: LivingEntity?): Boolean {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return false
        for (ability in spec.abilities.values) {
            if (ability.trigger != AbilityTrigger.ON_ACTION) continue
            if (ability.effects.none { it.type == "DIRECT_TO_CHEST" }) continue
            val tier = minion.getAbilityTier(ability.id)
            if (tier <= 0 || !ability.enabled || minion.getLevel() < ability.minLevel) continue
            if (!matchesConditions(minion, ability.conditions, null, entity)) continue
            val chance = (ability.chance + (tier - 1) * ability.chancePerTier).coerceIn(0.0, 100.0)
            if (ThreadLocalRandom.current().nextDouble(100.0) < chance) {
                return true
            }
        }
        return false
    }

    fun getExtraStackKillCount(minion: Minion, entity: LivingEntity?): Int {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return 0
        var totalExtra = 0
        for (ability in spec.abilities.values) {
            if (ability.trigger != AbilityTrigger.ON_ACTION) continue
            val stackEffects = ability.effects.filter { it.type == "MULTI_KILL_STACK" }
            if (stackEffects.isEmpty()) continue
            val tier = canTriggerAbility(minion, ability, null, entity) ?: continue
            for (effect in stackEffects) {
                totalExtra += (effect.amount * tier).roundToInt().coerceAtLeast(1)
            }
        }
        return totalExtra
    }

    fun applySellerSaleAbilities(minion: Minion, baseEarned: Double, itemCount: Int = 1, fromShulker: Boolean = false): SellerOutcome {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled || baseEarned <= 0.0) return SellerOutcome(baseEarned, false)

        var earned = baseEarned
        var autoDeposit = false

        for (ability in spec.abilities.values) {
            if (ability.trigger != AbilityTrigger.ON_ACTION) continue
            val tier = canTriggerAbility(minion, ability, null, null) ?: continue
            for (effect in ability.effects) {
                when (effect.type) {
                    "AUTO_DEPOSIT_VAULT" -> autoDeposit = true
                    "MULTIPLY_DROPS" -> earned *= effect.factor.coerceAtLeast(1.0)
                    "SELL_BONUS_PERCENT" -> earned *= (1.0 + effect.amount * tier)
                    "BULK_SELL_BONUS" -> if (itemCount >= 64) earned *= (1.0 + effect.amount * tier)
                    "SELL_SHULKER_BONUS" -> if (fromShulker) earned *= (1.0 + effect.amount * tier)
                    "SELL_LUCKY_COIN" -> depositMoney(minion, effect.amount * tier)
                    "MARKET_SURGE_PERIODIC" -> {
                        earned *= (1.0 + effect.amount * tier)
                        spawnParticle(minion.getLocation(), AbilityEffectSpec(type = "PARTICLE", particle = "HAPPY_VILLAGER", count = 8))
                    }
                    "GIVE_MONEY" -> depositMoney(minion, effect.amount * tier)
                    "PARTICLE" -> spawnParticle(minion.getLocation(), effect)
                    "SOUND" -> playSound(minion.getLocation(), effect)
                }
            }
        }

        return SellerOutcome(earned, autoDeposit)
    }

    fun getCrafterAbilityBonus(minion: Minion): CrafterBonus {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return CrafterBonus(Int.MAX_VALUE, 0.0, false, false, 0.0)

        var maxBatch = 4
        var extraOutputChance = 0.0
        var smeltOutputs = false
        var autoCompactAfter = false
        var recycleChance = 0.0

        for (ability in spec.abilities.values) {
            val tier = minion.getAbilityTier(ability.id)
            if (tier <= 0 || !ability.enabled || minion.getLevel() < ability.minLevel) continue
            val chance = (ability.chance + (tier - 1) * ability.chancePerTier).coerceIn(0.0, 100.0)
            for (effect in ability.effects) {
                when (effect.type) {
                    "BATCH_CRAFT" -> maxBatch += (effect.amount * tier).roundToInt().coerceAtLeast(1)
                    "MULTIPLY_DROPS" -> extraOutputChance += chance
                    "AUTO_SMELT" -> smeltOutputs = true
                    "AUTO_COMPACT" -> autoCompactAfter = true
                    "RECYCLE_INGREDIENT" -> recycleChance += chance
                }
            }
        }

        return CrafterBonus(
            maxBatchCrafts = maxBatch,
            extraOutputChance = extraOutputChance.coerceIn(0.0, 100.0),
            smeltOutputs = smeltOutputs,
            autoCompactAfter = autoCompactAfter,
            recycleChance = recycleChance.coerceIn(0.0, 100.0)
        )
    }

    fun smeltSingleItemIfMapped(stack: ItemStack): ItemStack {
        val mapped = SMELT_MAP[stack.type] ?: return stack
        return ItemStack(mapped, stack.amount)
    }

    fun getFisherAbilityBonus(minion: Minion): FisherBonus {
        val spec = minion.getType().getSkillSystemSpec()
        if (!spec.enabled) return FisherBonus(0.0, 0, 0.0, 0)

        var doubleCatchChance = 0.0
        var maxCustomBonus = 0
        var extraLuck = 0.0
        var weatherFrenzyRolls = 0

        val world = minion.getLocation().world
        val isStorming = world != null && (world.hasStorm() || world.isThundering)

        for (ability in spec.abilities.values) {
            val tier = minion.getAbilityTier(ability.id)
            if (tier <= 0 || !ability.enabled || minion.getLevel() < ability.minLevel) continue
            val chance = (ability.chance + (tier - 1) * ability.chancePerTier).coerceIn(0.0, 100.0)
            for (effect in ability.effects) {
                when (effect.type) {
                    "MULTIPLY_DROPS" -> doubleCatchChance += chance
                    "FISHER_EXTRA_CUSTOM_DROP" -> maxCustomBonus += (effect.amount * tier).roundToInt().coerceAtLeast(1)
                    "FISHER_CRATE_BOOST" -> extraLuck += effLuck(effect, tier)
                    "WEATHER_SPEED_BOOST" -> if (isStorming) weatherFrenzyRolls += tier
                }
            }
        }

        return FisherBonus(
            doubleCatchChance.coerceIn(0.0, 100.0),
            maxCustomBonus,
            extraLuck,
            weatherFrenzyRolls
        )
    }

    private fun effLuck(effect: AbilityEffectSpec, tier: Int): Double {
        return effect.amount * tier
    }

    private fun rollPoolDrop(pool: List<Material>, tier: Int): ItemStack? {
        if (pool.isEmpty()) return null
        val mat = pool[ThreadLocalRandom.current().nextInt(pool.size)]
        val amt = if (tier >= 3 && ThreadLocalRandom.current().nextBoolean()) 2 else 1
        return ItemStack(mat, amt)
    }

    private fun rollGeodeDrop(tier: Int): ItemStack? {
        if (tier >= 3 && ThreadLocalRandom.current().nextInt(100) < 12) {
            Material.matchMaterial("NETHERITE_SCRAP")?.let { return ItemStack(it, 1) }
        }
        return rollPoolDrop(GEODE_POOL, tier)
    }

    private fun applyBonusLooting(drops: MutableList<ItemStack>, tier: Int) {
        if (drops.isEmpty()) return
        val extraCount = tier.coerceIn(1, 3)
        val first = drops.firstOrNull { !it.type.isAir && it.amount > 0 } ?: return
        val bonus = first.clone()
        bonus.amount = extraCount
        drops.add(bonus)
    }

    private fun getMobHead(entity: LivingEntity?): ItemStack? {
        val mat = when (entity?.type) {
            EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED -> Material.ZOMBIE_HEAD
            EntityType.SKELETON, EntityType.STRAY -> Material.SKELETON_SKULL
            EntityType.CREEPER -> Material.CREEPER_HEAD
            EntityType.WITHER_SKELETON -> Material.WITHER_SKELETON_SKULL
            EntityType.PIGLIN, EntityType.ZOMBIFIED_PIGLIN -> Material.PIGLIN_HEAD
            else -> null
        } ?: return null
        return ItemStack(mat, 1)
    }

    private fun getRareMobEssence(entity: LivingEntity?, tier: Int): ItemStack? {
        val mat = entity?.type?.let { RARE_ESSENCE_BY_MOB[it] } ?: Material.EXPERIENCE_BOTTLE
        return ItemStack(mat, if (tier >= 3) 2 else 1)
    }

    private fun convertFisherTrash(drops: MutableList<ItemStack>) {
        if (TRASH_FISHER_TREASURES.isEmpty()) return
        val rand = ThreadLocalRandom.current()
        for (i in drops.indices) {
            if (drops[i].type in TRASH_FISHER_INPUTS) {
                val treasure = TRASH_FISHER_TREASURES[rand.nextInt(TRASH_FISHER_TREASURES.size)]
                drops[i] = ItemStack(treasure, 1)
            }
        }
    }

    private fun rollFisherGoldenBonus(minion: Minion, drops: MutableList<ItemStack>, tier: Int) {
        val rand = ThreadLocalRandom.current()
        if (tier >= 3 && rand.nextInt(100) < 10) {
            drops.add(ItemStack(Material.HEART_OF_THE_SEA, 1))
        } else if (rand.nextBoolean()) {
            drops.add(ItemStack(Material.GOLDEN_APPLE, 1))
        } else {
            drops.add(ItemStack(Material.GOLD_INGOT, tier.coerceAtLeast(1)))
        }
        depositMoney(minion, 250.0 * tier)
    }

    private fun convertSeedsToBonemeal(drops: MutableList<ItemStack>) {
        var added = 0
        for (i in drops.indices.reversed()) {
            val mat = drops[i].type
            if (mat == Material.WHEAT_SEEDS || mat == Material.BEETROOT_SEEDS || mat == Material.MELON_SEEDS || mat == Material.PUMPKIN_SEEDS) {
                if (drops[i].amount > 1) {
                    val convert = drops[i].amount / 2
                    drops[i].amount -= convert
                    added += convert
                }
            }
        }
        if (added > 0) {
            drops.add(ItemStack(Material.BONE_MEAL, added.coerceAtMost(64)))
        }
    }

    private fun upgradeCropsToGolden(drops: MutableList<ItemStack>) {
        for (i in drops.indices) {
            when (drops[i].type) {
                Material.CARROT -> drops[i] = ItemStack(Material.GOLDEN_CARROT, 1)
                Material.MELON_SLICE -> drops[i] = ItemStack(Material.GLISTERING_MELON_SLICE, 1)
                else -> {}
            }
        }
    }

    private fun addBonusCrop(drops: MutableList<ItemStack>, tier: Int) {
        val sample = drops.firstOrNull { !it.type.isAir && it.amount > 0 } ?: return
        drops.add(ItemStack(sample.type, tier.coerceIn(1, 3)))
    }

    private fun rollForestSap(tier: Int): ItemStack? {
        val rand = ThreadLocalRandom.current()
        val choices = listOfNotNull(
            Material.HONEYCOMB,
            Material.matchMaterial("RESIN_CLUMP"),
            Material.STICK,
            Material.APPLE
        )
        if (choices.isEmpty()) return null
        val mat = choices[rand.nextInt(choices.size)]
        return ItemStack(mat, if (tier >= 3 && rand.nextBoolean()) 2 else 1)
    }

    private fun applyLogsToPlanks(drops: MutableList<ItemStack>, effect: AbilityEffectSpec) {
        for (i in drops.indices) {
            val stack = drops[i]
            if (stack.type.isAir || stack.amount <= 0) continue
            if (stack.type.name in effect.blacklist) continue
            val plankMat = PLANKS_MAP[stack.type] ?: continue
            drops[i] = ItemStack(plankMat, (stack.amount * 4).coerceAtMost(64))
        }
    }

    private fun filterTrashDrops(drops: MutableList<ItemStack>, effect: AbilityEffectSpec) {
        drops.removeIf { stack ->
            !stack.hasItemMeta() && if (effect.blacklist.isNotEmpty()) {
                stack.type.name in effect.blacklist
            } else {
                stack.type in VOID_DEFAULT_TRASH
            }
        }
    }

    private fun mergeGroundStacks(minion: Minion) {
        val world = minion.getLocation().world ?: return
        val r = minion.getRange().coerceAtMost(6.0)
        val items = world.getNearbyEntities(minion.getLocation(), r, r, r).filterIsInstance<Item>()
        if (items.size <= 1) return

        for (i in items.indices) {
            val primary = items[i]
            if (!primary.isValid) continue
            val pStack = primary.itemStack
            if (pStack.amount >= pStack.maxStackSize) continue
            for (j in i + 1 until items.size) {
                val secondary = items[j]
                if (!secondary.isValid) continue
                val sStack = secondary.itemStack
                if (!pStack.isSimilar(sStack)) continue
                val space = pStack.maxStackSize - pStack.amount
                if (space <= 0) break
                val move = sStack.amount.coerceAtMost(space)
                pStack.amount += move
                primary.itemStack = pStack
                if (sStack.amount - move <= 0) {
                    secondary.remove()
                } else {
                    sStack.amount -= move
                    secondary.itemStack = sStack
                }
            }
        }
    }

    private fun moisturizeNearbyFarmland(minion: Minion) {
        val loc = minion.getLocation()
        val world = loc.world ?: return
        val rad = minion.getRange().roundToInt().coerceIn(1, 5)
        for (x in -rad..rad) {
            for (z in -rad..rad) {
                for (y in -1..1) {
                    val block = world.getBlockAt(loc.blockX + x, loc.blockY + y, loc.blockZ + z)
                    val data = block.blockData
                    if (data is Farmland && data.moisture < data.maximumMoisture) {
                        data.moisture = data.maximumMoisture
                        block.blockData = data
                    }
                }
            }
        }
    }

    private fun fertilizeNearbyCrops(minion: Minion, tier: Int) {
        val loc = minion.getLocation()
        val world = loc.world ?: return
        val rad = minion.getRange().roundToInt().coerceIn(1, 5)
        var applied = 0
        for (x in -rad..rad) {
            for (z in -rad..rad) {
                if (applied >= tier) return
                val block = world.getBlockAt(loc.blockX + x, loc.blockY, loc.blockZ + z)
                val data = block.blockData
                if (data is Ageable && data.age < data.maximumAge) {
                    block.applyBoneMeal(BlockFace.UP)
                    applied++
                }
            }
        }
    }

    private fun boostNearbyCropGrowth(minion: Minion, tier: Int) {
        val loc = minion.getLocation()
        val world = loc.world ?: return
        val rad = minion.getRange().roundToInt().coerceIn(1, 5)
        var boosted = 0
        for (x in -rad..rad) {
            for (z in -rad..rad) {
                if (boosted >= tier) return
                val block = world.getBlockAt(loc.blockX + x, loc.blockY, loc.blockZ + z)
                val data = block.blockData
                if (data is Ageable && data.age < data.maximumAge) {
                    data.age = (data.age + 1).coerceAtMost(data.maximumAge)
                    block.blockData = data
                    boosted++
                }
            }
        }
    }

    private fun canTriggerAbility(
        minion: Minion,
        ability: CustomAbilitySpec,
        block: Block?,
        entity: LivingEntity?
    ): Int? {
        if (!ability.enabled) return null
        val tier = minion.getAbilityTier(ability.id)
        if (tier <= 0 || minion.getLevel() < ability.minLevel) return null
        if (!matchesConditions(minion, ability.conditions, block, entity)) return null

        val chance = (ability.chance + (tier - 1) * ability.chancePerTier).coerceIn(0.0, 100.0)
        if (chance < 100.0 && ThreadLocalRandom.current().nextDouble(100.0) >= chance) return null

        if (ability.cooldownSeconds > 0.0) {
            val cdKey = "${minion.getLocationId()}:${ability.id}"
            val now = System.currentTimeMillis()
            val readyAt = cooldowns[cdKey] ?: 0L
            if (now < readyAt) return null
            cooldowns[cdKey] = now + (ability.cooldownSeconds * 1000.0).toLong()
        }

        return tier
    }

    private fun matchesConditions(
        minion: Minion,
        cond: AbilityConditionSpec,
        block: Block?,
        entity: LivingEntity?
    ): Boolean {
        if (cond.requireOwnerOnline && !minion.isOwnerOnline()) return false

        val worldName = minion.getLocation().world?.name
        if (cond.worlds.isNotEmpty() && (worldName == null || worldName !in cond.worlds)) return false

        if (cond.targetMaterials.isNotEmpty()) {
            val matName = block?.type?.name ?: return false
            if (matName !in cond.targetMaterials) return false
        }

        if (cond.targetEntities.isNotEmpty()) {
            val entityName = entity?.type?.name ?: return false
            if (entityName !in cond.targetEntities) return false
        }

        if (cond.toolEnchants.isNotEmpty()) {
            val enchants = minion.getTool()?.itemMeta?.enchants ?: emptyMap()
            for ((reqKey, minLvl) in cond.toolEnchants) {
                val found = enchants.entries.firstOrNull {
                    it.key.key.key.equals(reqKey, true) || it.key.name.equals(reqKey, true)
                }?.value ?: 0
                if (found < minLvl) return false
            }
        }

        return true
    }

    private fun applyAutoSmelt(drops: MutableList<ItemStack>, effect: AbilityEffectSpec) {
        for (i in drops.indices) {
            val stack = drops[i]
            if (stack.type.isAir || stack.amount <= 0) continue
            if (stack.type.name in effect.blacklist) continue
            val smelted = SMELT_MAP[stack.type] ?: continue
            drops[i] = ItemStack(smelted, stack.amount)
        }
    }

    private fun compactDropsInPlace(
        drops: MutableList<ItemStack>,
        effect: AbilityEffectSpec,
        compactCandidates: MutableSet<Material>
    ) {
        for (drop in drops) {
            if (drop.type.isAir || drop.type.name in effect.blacklist) continue
            if (COMPACT_MAP.containsKey(drop.type)) {
                compactCandidates.add(drop.type)
            }
        }
    }

    private fun multiplyDrops(drops: MutableList<ItemStack>, effect: AbilityEffectSpec, tier: Int) {
        val rawFactor = if (effect.factor > 1.0) {
            effect.factor
        } else {
            1.0 + tier
        }
        val guaranteed = rawFactor.toInt().coerceAtLeast(1)
        val remainder = (rawFactor - guaranteed).coerceIn(0.0, 1.0)
        val extra = if (remainder > 0.0 && ThreadLocalRandom.current().nextDouble() < remainder) 1 else 0
        val finalMult = guaranteed + extra
        if (finalMult <= 1) return

        val additions = mutableListOf<ItemStack>()
        for (drop in drops) {
            if (drop.type.isAir || drop.amount <= 0) continue
            val total = drop.amount * finalMult
            val maxStack = drop.maxStackSize.coerceAtLeast(1)
            if (total <= maxStack) {
                drop.amount = total
            } else {
                drop.amount = maxStack
                var left = total - maxStack
                while (left > 0) {
                    val piece = drop.clone()
                    piece.amount = left.coerceAtMost(maxStack)
                    additions.add(piece)
                    left -= piece.amount
                }
            }
        }
        drops.addAll(additions)
    }

    private fun countCleanMaterial(inv: Inventory, mat: Material): Int {
        var total = 0
        for (stack in inv.contents) {
            if (stack != null && stack.type == mat && !stack.hasItemMeta()) {
                total += stack.amount
            }
        }
        return total
    }

    private fun removeCleanMaterial(inv: Inventory, mat: Material, toRemove: Int) {
        var left = toRemove
        val contents = inv.contents
        for (i in contents.indices) {
            if (left <= 0) break
            val stack = contents[i] ?: continue
            if (stack.type != mat || stack.hasItemMeta()) continue
            if (stack.amount <= left) {
                left -= stack.amount
                inv.setItem(i, null)
            } else {
                stack.amount -= left
                left = 0
            }
        }
    }

    private fun depositMoney(minion: Minion, amount: Double) {
        if (amount <= 0.0) return
        val eco = AxMinionsPlugin.integrations.getEconomyIntegration() ?: return
        val owner = minion.getOwner() ?: return
        eco.giveBalance(owner, amount)
    }

    private fun addMinionXp(minion: Minion, amount: Double) {
        if (amount <= 0.0) return
        val maxStorage = getEffectiveStorage(
            minion,
            minion.getType().getDouble("storage", minion.getLevel()).coerceAtLeast(200.0)
        )
        minion.setStorage((minion.getStorage() + amount).coerceIn(0.0, maxStorage))
    }

    private fun buildExecutableItem(minion: Minion, effect: AbilityEffectSpec): ItemStack? {
        if (effect.id.isBlank()) return null
        val owner = Bukkit.getPlayer(minion.getOwnerUUID())
        return ItemUtils.buildExecutableItem(effect.id, effect.count.coerceAtLeast(1), owner)
    }

    private fun buildCustomItem(effect: AbilityEffectSpec): ItemStack? {
        if (effect.material.isBlank()) return null
        val mat = Material.matchMaterial(effect.material.uppercase(Locale.ENGLISH)) ?: return null
        val item = ItemStack(mat, effect.count.coerceAtLeast(1))
        if (effect.name.isBlank() && effect.lore.isEmpty()) return item
        val meta = item.itemMeta ?: return item
        if (effect.name.isNotBlank()) {
            meta.setDisplayName(StringUtils.formatToString(effect.name))
        }
        if (effect.lore.isNotEmpty()) {
            meta.lore = effect.lore.map { StringUtils.formatToString(it) }
        }
        item.itemMeta = meta
        return item
    }

    private fun runCommands(minion: Minion, commands: List<String>, tier: Int) {
        if (commands.isEmpty()) return
        val ownerName = minion.getOwner()?.name ?: return
        val loc = minion.getLocation()
        val worldName = loc.world?.name ?: "world"

        Scheduler.get().run { _ ->
            for (raw in commands) {
                val cmd = raw
                    .replace("<player>", ownerName)
                    .replace("%player%", ownerName)
                    .replace("<world>", worldName)
                    .replace("<minion_x>", loc.blockX.toString())
                    .replace("<minion_y>", loc.blockY.toString())
                    .replace("<minion_z>", loc.blockZ.toString())
                    .replace("<level>", minion.getLevel().toString())
                    .replace("<tier>", tier.toString())
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
            }
        }
    }

    private fun spawnParticle(loc: Location, effect: AbilityEffectSpec) {
        if (effect.particle.isBlank()) return
        val world = loc.world ?: return
        try {
            val particle = Particle.valueOf(effect.particle.uppercase(Locale.ENGLISH))
            world.spawnParticle(particle, loc.clone().add(0.5, 1.0, 0.5), effect.count.coerceIn(1, 30), 0.3, 0.3, 0.3, 0.02)
        } catch (_: Exception) {
        }
    }

    private fun playSound(loc: Location, effect: AbilityEffectSpec) {
        if (effect.sound.isBlank()) return
        val world = loc.world ?: return
        try {
            val sound = Sound.valueOf(effect.sound.uppercase(Locale.ENGLISH))
            world.playSound(loc, sound, 0.8f, 1.1f)
        } catch (_: Exception) {
        }
    }
}
