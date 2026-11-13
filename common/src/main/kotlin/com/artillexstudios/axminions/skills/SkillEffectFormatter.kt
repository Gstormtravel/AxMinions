package com.artillexstudios.axminions.skills

import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.skills.CustomAbilitySpec
import com.artillexstudios.axminions.api.skills.StatSpec
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

object SkillEffectFormatter {
    private val numFmt = NumberFormat.getNumberInstance(Locale.US)

    fun formatStatPreviewLines(minion: Minion, stat: StatSpec): List<String> {
        val tier = minion.getStatTier(stat.id)
        val nextTier = (tier + 1).coerceAtMost(stat.maxTier)
        val isSeller = minion.getType().getName().equals("seller", ignoreCase = true)

        return when (stat.id) {
            "speed" -> {
                val base = minion.getType().getLong("speed", minion.getLevel()).toInt().coerceAtLeast(1)
                val floor = min(base, stat.minSpeedFloor.coerceAtLeast(1))
                val curTicks = (base + (tier * stat.modifierPerTier).roundToInt()).coerceAtLeast(floor)
                val nextTicks = (base + (nextTier * stat.modifierPerTier).roundToInt()).coerceAtLeast(floor)
                val curSec = String.format(Locale.US, "%.2fs", curTicks / 20.0)
                val nextSec = String.format(Locale.US, "%.2fs", nextTicks / 20.0)
                if (tier < stat.maxTier) {
                    listOf(
                        "<gray>Cadência atual: <#55FF55>$curSec <dark_gray>($curTicks ticks)",
                        "<gray>Próximo nível: <#FFAA00>$nextSec <dark_gray>($nextTicks ticks)"
                    )
                } else {
                    listOf("<gray>Cadência máxima: <#55FF55>$curSec <dark_gray>($curTicks ticks)")
                }
            }
            "range" -> {
                val base = minion.getType().getDouble("range", minion.getLevel())
                val cap = maxOf(base, stat.maxRangeCap)
                val curRange = (base + tier * stat.modifierPerTier).coerceAtMost(cap)
                val nextRange = (base + nextTier * stat.modifierPerTier).coerceAtMost(cap)
                val curStr = String.format(Locale.US, "%.2f", curRange)
                val nextStr = String.format(Locale.US, "%.2f", nextRange)
                if (tier < stat.maxTier) {
                    listOf(
                        "<gray>Alcance atual: <#55FF55>$curStr blocos",
                        "<gray>Próximo nível: <#FFAA00>$nextStr blocos"
                    )
                } else {
                    listOf("<gray>Alcance máximo: <#55FF55>$curStr blocos")
                }
            }
            "durability_save" -> {
                val curPct = (tier * stat.chancePerTier).coerceIn(0.0, 95.0)
                val nextPct = (nextTier * stat.chancePerTier).coerceIn(0.0, 95.0)
                val curStr = String.format(Locale.US, "%.1f%%", curPct)
                val nextStr = String.format(Locale.US, "%.1f%%", nextPct)
                if (tier < stat.maxTier) {
                    listOf(
                        "<gray>Preservação atual: <#55FF55>$curStr da ferramenta",
                        "<gray>Próximo nível: <#FFAA00>$nextStr da ferramenta"
                    )
                } else {
                    listOf("<gray>Preservação máxima: <#55FF55>$curStr da ferramenta")
                }
            }
            "storage" -> {
                val baseStorage = minion.getType().getDouble("storage", minion.getLevel())
                val curVal = calcStorage(baseStorage, stat, tier)
                val nextVal = calcStorage(baseStorage, stat, nextTier)
                val unit = if (isSeller) "$" else " pts XP"
                val curStr = "${numFmt.format(curVal.roundToInt())}$unit"
                val nextStr = "${numFmt.format(nextVal.roundToInt())}$unit"
                if (tier < stat.maxTier) {
                    listOf(
                        "<gray>Capacidade atual: <#55FF55>$curStr",
                        "<gray>Próximo nível: <#FFAA00>$nextStr"
                    )
                } else {
                    listOf("<gray>Capacidade máxima: <#55FF55>$curStr")
                }
            }
            "multiplier" -> {
                val baseMult = minion.getType().getDouble("multiplier", minion.getLevel())
                val curM = baseMult + tier * stat.modifierPerTier
                val nextM = baseMult + nextTier * stat.modifierPerTier
                val curStr = String.format(Locale.US, "%.2fx", curM)
                val nextStr = String.format(Locale.US, "%.2fx", nextM)
                if (tier < stat.maxTier) {
                    listOf(
                        "<gray>Multiplicador atual: <#55FF55>$curStr",
                        "<gray>Próximo nível: <#FFAA00>$nextStr"
                    )
                } else {
                    listOf("<gray>Multiplicador máximo: <#55FF55>$curStr")
                }
            }
            "luck" -> {
                val curL = tier * stat.chancePerTier
                val nextL = nextTier * stat.chancePerTier
                val curStr = String.format(Locale.US, "+%.1f sorte", curL)
                val nextStr = String.format(Locale.US, "+%.1f sorte", nextL)
                if (tier < stat.maxTier) {
                    listOf(
                        "<gray>Sorte atual: <#55FF55>$curStr",
                        "<gray>Próximo nível: <#FFAA00>$nextStr"
                    )
                } else {
                    listOf("<gray>Sorte máxima: <#55FF55>$curStr")
                }
            }
            else -> emptyList()
        }
    }

    fun formatAbilityEffectLines(minion: Minion, ability: CustomAbilitySpec): List<String> {
        val tier = minion.getAbilityTier(ability.id)
        val nextTier = (tier + 1).coerceAtMost(ability.maxTier)
        val activeChance = if (tier > 0) (ability.chance + (tier - 1) * ability.chancePerTier).coerceIn(0.0, 100.0) else 0.0
        val nextChance = (ability.chance + (nextTier - 1) * ability.chancePerTier).coerceIn(0.0, 100.0)

        val lines = mutableListOf<String>()
        val curChanceStr = String.format(Locale.US, "%.1f%%", activeChance)
        val nextChanceStr = String.format(Locale.US, "%.1f%%", nextChance)

        if (ability.chance < 100.0 || ability.chancePerTier > 0.0) {
            if (tier > 0 && tier < ability.maxTier) {
                lines.add("<gray>Chance ativa: <#55FF55>$curChanceStr <gray>» Próx: <#FFAA00>$nextChanceStr")
            } else if (tier >= ability.maxTier) {
                lines.add("<gray>Chance ativa (MAX): <#55FF55>$curChanceStr")
            } else {
                lines.add("<gray>Chance no Tier 1: <#FFAA00>$nextChanceStr")
            }
        }

        val summaryCur = summarizeEffectMagnitude(ability, tier.coerceAtLeast(1))
        val summaryNext = if (tier < ability.maxTier) summarizeEffectMagnitude(ability, nextTier) else null
        if (summaryCur != null) {
            if (tier == 0) {
                lines.add("<gray>Efeito no Tier 1: <#FFAA00>$summaryCur")
            } else if (summaryNext != null && summaryNext != summaryCur) {
                lines.add("<gray>Efeito atual: <#55FF55>$summaryCur <gray>» Próx: <#FFAA00>$summaryNext")
            } else {
                lines.add("<gray>Efeito ativo: <#55FF55>$summaryCur")
            }
        }
        return lines
    }

    private fun summarizeEffectMagnitude(ability: CustomAbilitySpec, tier: Int): String? {
        for (eff in ability.effects) {
            return when (eff.type) {
                "XP_BOOST" -> "+${((eff.factor - 1.0).coerceAtLeast(0.1) * tier * 100).roundToInt()}% XP armazenado"
                "XP_VACUUM" -> "${100 + (tier - 1) * 25}% eficiência de absorção"
                "MULTI_KILL_STACK" -> "+${(eff.amount * tier).roundToInt().coerceAtLeast(1)} abate(s) por golpe"
                "BATCH_CRAFT" -> "+${(eff.amount * tier).roundToInt()} receitas por ciclo"
                "FISHER_EXTRA_CUSTOM_DROP" -> "+${(eff.amount * tier).roundToInt()} custom-drop por fisgada"
                "SELL_BONUS_PERCENT" -> "+${(eff.amount * tier * 100).roundToInt()}% valor de venda"
                "BULK_SELL_BONUS" -> "+${(eff.amount * tier * 100).roundToInt()}% em lotes ≥ 64 itens"
                "SELL_MULTIPLIER_BOOST" -> "+${String.format(Locale.US, "%.2fx", eff.amount * tier)} multiplicador"
                "SELL_SHULKER_BONUS" -> "+${(eff.amount * tier * 100).roundToInt()}% em Shulker Boxes"
                "SELL_LUCKY_COIN" -> "+$${numFmt.format((eff.amount * tier).roundToInt())} bônus por sorteio"
                "MARKET_SURGE_PERIODIC" -> "+${(eff.amount * tier * 100).roundToInt()}% no pico (${ability.cooldownSeconds.toInt()}s cd)"
                "LUMBER_RARE_DROP_BOOST" -> "+${(eff.amount * tier * 100).roundToInt()}% chance de drops raros"
                "COLLECTOR_RANGE_BOOST" -> "+${String.format(Locale.US, "%.1f", eff.amount * tier)} blocos de raio"
                "ASSEMBLY_SPEED_REDUCE" -> "-${(eff.amount * tier * 100).roundToInt()}% tempo de ciclo"
                "AOE_CLEAVE" -> "atinge até ${eff.count + (tier - 1)} alvos"
                "WEATHER_SPEED_BOOST" -> "+$tier fisgada(s) extras na chuva"
                "FISHER_CRATE_BOOST" -> "+${String.format(Locale.US, "%.1f", eff.amount * tier)} sorte em caixas"
                "GIVE_MONEY" -> "+$${numFmt.format((eff.amount * tier).roundToInt())}"
                "GIVE_XP" -> "+${numFmt.format((eff.amount * tier).roundToInt())} XP"
                "AUTO_SMELT" -> "funde resultados automaticamente"
                "AUTO_COMPACT", "CHEST_AUTO_COMPRESS" -> "compacta itens 9x/4x no baú"
                "DIRECT_TO_CHEST" -> "teleporta drops ao baú"
                "AUTO_DEPOSIT_VAULT" -> "deposita saldo direto no Vault"
                "OVERFLOW_TO_ENDERCHEST" -> "envia sobras ao EnderChest"
                "CLEAR_TREE_LEAVES" -> "limpa folhas da copa automaticamente"
                "LOGS_TO_PLANKS" -> "converte toras em 4x tábuas"
                "OBSIDIAN_EXTRA_BREAK" -> "+1 obsidiana extra sem gastar ferramenta"
                else -> null
            }
        }
        return null
    }

    private fun calcStorage(base: Double, stat: StatSpec, tier: Int): Double {
        if (tier <= 0) return base
        return if (stat.modifierPerTier >= 5.0) {
            base + tier * stat.modifierPerTier
        } else {
            base * (1.0 + tier * stat.modifierPerTier)
        }
    }
}
