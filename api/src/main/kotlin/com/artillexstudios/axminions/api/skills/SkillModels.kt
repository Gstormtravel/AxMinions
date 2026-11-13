package com.artillexstudios.axminions.api.skills

enum class AbilityTrigger {
    ON_ACTION,
    ON_PERIODIC
}

data class StatSpec(
    val id: String,
    val displayName: String,
    val description: List<String>,
    val material: String,
    val slot: Int,
    val maxTier: Int,
    val costPerTier: List<Int>,
    val modifierPerTier: Double,
    val chancePerTier: Double,
    val minSpeedFloor: Int = 30,
    val maxRangeCap: Double = 6.0
)

data class AbilityConditionSpec(
    val targetMaterials: Set<String> = emptySet(),
    val targetEntities: Set<String> = emptySet(),
    val toolEnchants: Map<String, Int> = emptyMap(),
    val worlds: Set<String> = emptySet(),
    val requireOwnerOnline: Boolean = false
)

data class AbilityEffectSpec(
    val type: String,
    val factor: Double = 1.0,
    val amount: Double = 1.0,
    val id: String = "",
    val material: String = "",
    val name: String = "",
    val lore: List<String> = emptyList(),
    val commands: List<String> = emptyList(),
    val recipes: List<String> = emptyList(),
    val particle: String = "",
    val sound: String = "",
    val count: Int = 1,
    val blacklist: Set<String> = emptySet()
)

data class CustomAbilitySpec(
    val id: String,
    val enabled: Boolean,
    val slot: Int,
    val material: String,
    val displayName: String,
    val description: List<String>,
    val minLevel: Int,
    val maxTier: Int,
    val costPerTier: List<Int>,
    val trigger: AbilityTrigger,
    val chance: Double,
    val chancePerTier: Double,
    val cooldownSeconds: Double,
    val conditions: AbilityConditionSpec,
    val effects: List<AbilityEffectSpec>
)

data class SkillSystemSpec(
    val enabled: Boolean = false,
    val pointsPerLevel: Int = 1,
    val bonusPointsLevels: Set<Int> = setOf(5, 10, 15, 20),
    val respecCostMoney: Double = 1000000.0,
    val stats: Map<String, StatSpec> = emptyMap(),
    val abilities: Map<String, CustomAbilitySpec> = emptyMap()
) {
    fun getTotalEarnedPoints(level: Int): Int {
        if (!enabled || level <= 1) return 0
        var total = (level - 1) * pointsPerLevel
        for (bonusLevel in bonusPointsLevels) {
            if (level >= bonusLevel) {
                total += 1
            }
        }
        return total
    }

    fun getCostForNextTier(costs: List<Int>, currentTier: Int): Int {
        if (costs.isEmpty()) return 1
        val index = currentTier.coerceIn(0, costs.size - 1)
        return costs[index].coerceAtLeast(0)
    }

    fun getCostUpToTier(costs: List<Int>, targetTier: Int): Int {
        var sum = 0
        for (i in 0 until targetTier.coerceAtLeast(0)) {
            sum += getCostForNextTier(costs, i)
        }
        return sum
    }

    fun getSpentPoints(allocations: Map<String, Int>): Int {
        var spent = 0
        for ((key, tier) in allocations) {
            if (tier <= 0) continue
            val stat = stats[key]
            if (stat != null) {
                spent += getCostUpToTier(stat.costPerTier, tier.coerceAtMost(stat.maxTier))
                continue
            }
            val ability = abilities[key]
            if (ability != null) {
                spent += getCostUpToTier(ability.costPerTier, tier.coerceAtMost(ability.maxTier))
            }
        }
        return spent
    }

    companion object {
        fun serializeAllocations(allocations: Map<String, Int>): String {
            if (allocations.isEmpty()) return ""
            return allocations.entries
                .filter { it.value > 0 }
                .sortedBy { it.key }
                .joinToString(";") { "${it.key}:${it.value}" }
        }

        fun deserializeAllocations(raw: String?): MutableMap<String, Int> {
            val map = linkedMapOf<String, Int>()
            if (raw.isNullOrBlank()) return map
            for (part in raw.split(";")) {
                val trimmed = part.trim()
                if (trimmed.isEmpty()) continue
                val sep = trimmed.indexOf(':')
                if (sep <= 0) continue
                val id = trimmed.substring(0, sep).trim()
                val tier = trimmed.substring(sep + 1).trim().toIntOrNull() ?: continue
                if (tier > 0) {
                    map[id] = tier
                }
            }
            return map
        }
    }
}
