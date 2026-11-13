package com.artillexstudios.axminions.api.skills

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section
import java.util.Locale

object SkillConfigParser {

    fun parse(config: Config): SkillSystemSpec {
        val doc = config.backingDocument
        val skillSec = doc.getSection("skill-system")

        val enabled = skillSec?.getBoolean("enabled", false) ?: false
        val pointsPerLevel = skillSec?.getInt("points-per-level", 1) ?: 1
        val rawBonusLevels = skillSec?.getIntList("bonus-points-levels").orEmpty()
        val bonusPointsLevels = if (rawBonusLevels.isEmpty()) setOf(5, 10, 15, 20) else rawBonusLevels.toSet()
        val respecCostMoney = skillSec?.getDouble("respec-cost-money", 1000000.0) ?: 1000000.0

        val stats = linkedMapOf<String, StatSpec>()
        val statsSec = skillSec?.getSection("stats")
        if (statsSec != null) {
            for (statId in statsSec.getRoutesAsStrings(false)) {
                val sec = statsSec.getSection(statId) ?: continue
                stats[statId] = parseStatSpec(statId, sec)
            }
        }

        val abilities = linkedMapOf<String, CustomAbilitySpec>()
        val abilitiesSec = doc.getSection("custom-abilities")
        if (abilitiesSec != null) {
            for (abilityId in abilitiesSec.getRoutesAsStrings(false)) {
                val sec = abilitiesSec.getSection(abilityId) ?: continue
                abilities[abilityId] = parseAbilitySpec(abilityId, sec)
            }
        }

        return SkillSystemSpec(
            enabled = enabled,
            pointsPerLevel = pointsPerLevel,
            bonusPointsLevels = bonusPointsLevels,
            respecCostMoney = respecCostMoney,
            stats = stats,
            abilities = abilities
        )
    }

    private fun parseStatSpec(id: String, sec: Section): StatSpec {
        val rawCosts = sec.getIntList("cost-per-tier")
        val maxTier = sec.getInt("max-tier", 5).coerceAtLeast(1)
        val costs = if (rawCosts.isNotEmpty()) rawCosts else List(maxTier) { 1 }
        return StatSpec(
            id = id,
            displayName = sec.getString("display-name", id) ?: id,
            description = sec.getStringList("description").orEmpty(),
            material = sec.getString("material", "REDSTONE") ?: "REDSTONE",
            slot = sec.getInt("slot", 10),
            maxTier = maxTier,
            costPerTier = costs,
            modifierPerTier = sec.getDouble("modifier-per-tier", 0.0),
            chancePerTier = sec.getDouble("chance-per-tier", 0.0),
            minSpeedFloor = sec.getInt("min-speed-floor", 30),
            maxRangeCap = sec.getDouble("max-range-cap", 6.0)
        )
    }

    private fun parseAbilitySpec(id: String, sec: Section): CustomAbilitySpec {
        val rawCosts = sec.getIntList("cost-per-tier")
        val maxTier = sec.getInt("max-tier", 3).coerceAtLeast(1)
        val costs = if (rawCosts.isNotEmpty()) rawCosts else listOf(2, 3, 4)
        val triggerStr = (sec.getString("trigger", "ON_ACTION") ?: "ON_ACTION").uppercase(Locale.ENGLISH)
        val trigger = try {
            AbilityTrigger.valueOf(triggerStr)
        } catch (_: Exception) {
            AbilityTrigger.ON_ACTION
        }

        val condSec = sec.getSection("conditions")
        val conditions = if (condSec != null) parseConditions(condSec) else AbilityConditionSpec()

        val effects = sec.getMapList("effects").orEmpty().mapNotNull { parseEffect(it) }

        return CustomAbilitySpec(
            id = id,
            enabled = sec.getBoolean("enabled", true),
            slot = sec.getInt("slot", 29),
            material = sec.getString("material", "NETHER_STAR") ?: "NETHER_STAR",
            displayName = sec.getString("display-name", id) ?: id,
            description = sec.getStringList("description").orEmpty(),
            minLevel = sec.getInt("min-level", 1),
            maxTier = maxTier,
            costPerTier = costs,
            trigger = trigger,
            chance = sec.getDouble("chance", 100.0),
            chancePerTier = sec.getDouble("chance-per-tier", 0.0),
            cooldownSeconds = sec.getDouble("cooldown-seconds", 0.0),
            conditions = conditions,
            effects = effects
        )
    }

    private fun parseConditions(sec: Section): AbilityConditionSpec {
        val targetMaterials = sec.getStringList("target-materials")
            .orEmpty()
            .map { it.trim().uppercase(Locale.ENGLISH) }
            .filter { it.isNotEmpty() }
            .toSet()

        val targetEntities = sec.getStringList("target-entities")
            .orEmpty()
            .map { it.trim().uppercase(Locale.ENGLISH) }
            .filter { it.isNotEmpty() }
            .toSet()

        val worlds = sec.getStringList("worlds")
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val enchants = linkedMapOf<String, Int>()
        val enchSec = sec.getSection("tool-enchants")
        if (enchSec != null) {
            for (enchKey in enchSec.getRoutesAsStrings(false)) {
                enchants[enchKey.lowercase(Locale.ENGLISH)] = enchSec.getInt(enchKey, 1)
            }
        } else {
            for (entry in sec.getStringList("tool-enchants").orEmpty()) {
                val parts = entry.split(":")
                val name = parts[0].trim().lowercase(Locale.ENGLISH)
                val level = if (parts.size > 1) parts[1].trim().toIntOrNull() ?: 1 else 1
                if (name.isNotEmpty()) {
                    enchants[name] = level
                }
            }
        }

        return AbilityConditionSpec(
            targetMaterials = targetMaterials,
            targetEntities = targetEntities,
            toolEnchants = enchants,
            worlds = worlds,
            requireOwnerOnline = sec.getBoolean("require-owner-online", false)
        )
    }

    private fun parseEffect(raw: Map<*, *>): AbilityEffectSpec? {
        val type = (raw["type"]?.toString() ?: "").trim().uppercase(Locale.ENGLISH)
        if (type.isEmpty()) return null

        return AbilityEffectSpec(
            type = type,
            factor = toDouble(raw["factor"], 1.0),
            amount = toDouble(raw["amount"], 1.0),
            id = raw["id"]?.toString()?.trim().orEmpty(),
            material = raw["material"]?.toString()?.trim().orEmpty(),
            name = raw["name"]?.toString().orEmpty(),
            lore = toStringList(raw["lore"]),
            commands = toStringList(raw["commands"]),
            recipes = toStringList(raw["recipes"]),
            particle = raw["particle"]?.toString()?.trim().orEmpty(),
            sound = raw["sound"]?.toString()?.trim().orEmpty(),
            count = toInt(raw["count"], 1),
            blacklist = toStringList(raw["blacklist"]).map { it.uppercase(Locale.ENGLISH) }.toSet()
        )
    }

    private fun toDouble(value: Any?, default: Double): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: default
            else -> default
        }
    }

    private fun toInt(value: Any?, default: Int): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: default
            else -> default
        }
    }

    private fun toStringList(value: Any?): List<String> {
        if (value !is Iterable<*>) return emptyList()
        return value.mapNotNull { it?.toString() }
    }
}
