package com.artillexstudios.axminions.menus

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.skills.CustomAbilitySpec
import com.artillexstudios.axminions.api.skills.StatSpec
import com.artillexstudios.axminions.api.utils.Keys
import com.artillexstudios.axminions.logging.MinionLogger
import com.artillexstudios.axminions.skills.SkillEffectFormatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.text.NumberFormat
import java.util.Locale

object MinionSkillsGui {
    private val numberFormat = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT)

    class SkillsGuiHolder(val minion: Minion) : InventoryHolder {
        override fun getInventory(): Inventory = Bukkit.createInventory(this, 54)
    }

    class ToolSwapHolder(val minion: Minion) : InventoryHolder {
        override fun getInventory(): Inventory = Bukkit.createInventory(this, 27)
    }

    class PickupGuiHolder(val minion: Minion) : InventoryHolder {
        override fun getInventory(): Inventory = Bukkit.createInventory(this, 27)
    }

    class AdminMinionsHolder(val page: Int, val filterOwner: String? = null) : InventoryHolder {
        override fun getInventory(): Inventory = Bukkit.createInventory(this, 54)
    }

    fun renderBar(current: Int, max: Int, plain: Boolean = false): String {
        val filled = current.coerceIn(0, max)
        val empty = (max - filled).coerceAtLeast(0)
        return if (plain) {
            "■".repeat(filled) + "□".repeat(empty)
        } else {
            "<green>" + "■".repeat(filled) + "</green><dark_gray>" + "□".repeat(empty) + "</dark_gray>"
        }
    }

    fun openSkillsGui(minion: Minion, player: Player) {
        val holder = SkillsGuiHolder(minion)
        val available = minion.getAvailableSkillPoints()
        val rawTitle = StringUtils.formatToString(
            "<#00CCFF><b>Habilidades</b> <dark_gray>| <yellow>$available pts"
        )
        val inv = Bukkit.createInventory(holder, 54, rawTitle)

        val filler = makeButton(Material.GRAY_STAINED_GLASS_PANE, " ", emptyList(), null)
        for (i in 0 until 54) {
            inv.setItem(i, filler)
        }

        val totalPts = minion.getTotalSkillPoints()
        val spentPts = minion.getSpentSkillPoints()
        val spec = minion.getType().getSkillSystemSpec()

        inv.setItem(
            4,
            makeButton(
                Material.EXPERIENCE_BOTTLE,
                "<#00EEFF><b>Árvore de Pontos <dark_gray>(<white>Lv ${minion.getLevel()}<dark_gray>)",
                listOf(
                    "",
                    " <gray>- <white>Pontos Livres: <#55FF55><b>$available</b>",
                    " <gray>- <white>Pontos Gastos: <#FFAA00>$spentPts <dark_gray>/ $totalPts",
                    "",
                    " <gray>Cada nível concede pontos para evoluir",
                    " <gray>atributos e desbloquear poderes especiais!"
                ),
                null
            )
        )

        for (stat in spec.stats.values) {
            if (stat.slot !in 0..44) continue
            inv.setItem(stat.slot, buildStatItem(minion, spec, stat))
        }

        for (ability in spec.abilities.values) {
            if (!ability.enabled || ability.slot !in 0..44) continue
            inv.setItem(ability.slot, buildAbilityItem(minion, spec, ability))
        }

        inv.setItem(
            45,
            makeButton(
                Material.ARROW,
                "<red><b>⬅ Voltar</b>",
                listOf("<gray>Retorna ao menu do minion."),
                "back_main"
            )
        )

        inv.setItem(
            49,
            makeButton(
                Material.GRINDSTONE,
                "<#FF5555><b>♻ Resetar Pontos (Respec)</b>",
                listOf(
                    "",
                    " <gray>Devolve todos os <#55FF55>$spentPts pontos <gray>investidos",
                    " <gray>para você montar outra estratégia.",
                    "",
                    " <gray>Taxa: <#FFAA00>${numberFormat.format(spec.respecCostMoney)}$",
                    "",
                    " <#FF5555><b>(!)</b> Clique para resetar!"
                ),
                "respec_skills"
            )
        )

        if (MenuRouter.canUseDialog(player)) {
            inv.setItem(
                53,
                makeButton(
                    Material.COMPARATOR,
                    "<#00CCFF><b>🔄 Trocar para Menu Moderno</b>",
                    listOf(
                        "<gray>Alterna sua interface para o",
                        "<aqua>Painel Nativo Moderno</aqua>!"
                    ),
                    "menu_switch"
                )
            )
        }

        player.openInventory(inv)
    }

    fun openAdminGui(player: Player, page: Int = 0, filterOwner: String? = null) {
        val all = AxMinionsAPI.INSTANCE.getMinions()
            .filter { m ->
                filterOwner.isNullOrBlank() ||
                    m.getOwner()?.name?.contains(filterOwner, ignoreCase = true) == true ||
                    m.getFirstOwner().contains(filterOwner, ignoreCase = true)
            }
        val perPage = 45
        val totalPages = ((all.size + perPage - 1) / perPage).coerceAtLeast(1)
        val safePage = page.coerceIn(0, totalPages - 1)

        val holder = AdminMinionsHolder(safePage, filterOwner)
        val filterLabel = if (filterOwner.isNullOrBlank()) "" else " [$filterOwner]"
        val title = StringUtils.formatToString("<#FF5555><b>Admin Minions$filterLabel</b> <dark_gray>(<white>${safePage + 1}/$totalPages<dark_gray>)")
        val inv = Bukkit.createInventory(holder, 54, title)

        val filler = makeButton(Material.BLACK_STAINED_GLASS_PANE, " ", emptyList(), null)
        for (i in 45 until 54) {
            inv.setItem(i, filler)
        }

        val start = safePage * perPage
        val slice = all.drop(start).take(perPage)
        slice.forEachIndexed { idx, minion ->
            val loc = minion.getLocation()
            val ownerName = minion.getOwner()?.name ?: minion.getOwnerUUID().toString().take(8)
            val firstOwner = minion.getFirstOwner().ifBlank { ownerName }
            val history = minion.getOwnershipHistoryString().ifBlank { firstOwner }
            val toolName = Messages.TOOL_NAME(minion.getTool()?.type)
            val chestStr = minion.getLinkedChest()?.let { "${it.world?.name} (${it.blockX}, ${it.blockY}, ${it.blockZ})" } ?: "Nenhum"
            val storageLabel = if (minion.getType().getName().equals("seller", true)) {
                "${numberFormat.format(minion.getStorage())}$"
            } else {
                "${minion.getStorage().toLong()} XP"
            }

            val mat = Material.matchMaterial(minion.getType().getConfig().getString("item.material", "PLAYER_HEAD").uppercase(Locale.ENGLISH))
                ?: Material.PLAYER_HEAD

            val item = makeButton(
                mat,
                "<#FFEE55><b>${minion.getType().getName()} Lv ${minion.getLevel()}</b> <dark_gray>(<white>$ownerName<dark_gray>)",
                listOf(
                    "",
                    " <gray>Primeiro dono: <#55FF55>$firstOwner",
                    " <gray>Historico donos: <white>$history",
                    " <gray>Localizacao: <aqua>${loc.world?.name} (${loc.blockX}, ${loc.blockY}, ${loc.blockZ})",
                    " <gray>Bau vinculado: <white>$chestStr",
                    " <gray>Ferramenta: <white>$toolName",
                    " <gray>Acoes: <yellow>${minion.getActionAmount()} <gray>| Reserva: <#FFAA00>$storageLabel",
                    " <gray>Status: ${if (minion.isTicking()) "<green>Ativo" else "<red>Inativo"}",
                    "",
                    " <#55FF55>Clique Esquerdo: <gray>Teleportar",
                    " <#FF5555>Shift + Clique Direito: <gray>Remover minion"
                ),
                "admin_minion:${loc.world?.name}:${loc.blockX}:${loc.blockY}:${loc.blockZ}",
                glow = minion.isTicking()
            )
            inv.setItem(idx, item)
        }

        if (safePage > 0) {
            inv.setItem(45, makeButton(Material.ARROW, "<yellow><b>⬅ Pagina Anterior</b>", listOf("<gray>Ir para pagina $safePage"), "admin_page:${safePage - 1}"))
        }
        val recentCount = MinionLogger.getRecentLogs(500).size
        inv.setItem(49, makeButton(Material.BOOK, "<#00CCFF><b>Logs & Resumo</b>", listOf(
            "",
            " <gray>Total de minions carregados: <#55FF55>${all.size}",
            " <gray>Eventos recentes em memoria: <yellow>$recentCount",
            "",
            " <gray>Use <white>/axminions logs [qtd] [filtro]",
            " <gray>para ler o historico completo no chat."
        ), "admin_logs"))
        if (safePage + 1 < totalPages) {
            inv.setItem(53, makeButton(Material.ARROW, "<yellow><b>Proxima Pagina ➡</b>", listOf("<gray>Ir para pagina ${safePage + 2}"), "admin_page:${safePage + 1}"))
        }
        inv.setItem(48, makeButton(Material.BARRIER, "<red><b>Fechar</b>", listOf("<gray>Fecha o painel administrativo."), "admin_close"))

        player.openInventory(inv)
    }

    fun openToolSwapGui(minion: Minion, player: Player) {
        val holder = ToolSwapHolder(minion)
        val inv = Bukkit.createInventory(holder, 27, StringUtils.formatToString("<#00AAFF><b>Ferramenta do Minion</b>"))
        val filler = makeButton(Material.BLACK_STAINED_GLASS_PANE, " ", emptyList(), null)
        for (i in 0 until 27) {
            inv.setItem(i, filler)
        }

        inv.setItem(
            4,
            makeButton(
                Material.OAK_SIGN,
                "<#FFEE55><b>Gerenciar Ferramenta</b>",
                listOf(
                    "",
                    " <gray>Clique em uma ferramenta no seu inventário",
                    " <gray>para equipar neste minion.",
                    " <gray>Clique no slot central para retirar a atual."
                ),
                null
            )
        )

        val tool = minion.getTool()
        inv.setItem(13, if (tool != null && !tool.type.isAir) tool.clone() else null)

        inv.setItem(
            22,
            makeButton(
                Material.ARROW,
                "<red><b>⬅ Voltar</b>",
                listOf("<gray>Voltar para o menu do minion."),
                "back_main"
            )
        )

        player.openInventory(inv)
    }

    fun openPickupGui(minion: Minion, player: Player) {
        val holder = PickupGuiHolder(minion)
        val inv = Bukkit.createInventory(holder, 27, StringUtils.formatToString("<#FF5555><b>Recolher Minion</b>"))
        val filler = makeButton(Material.BLACK_STAINED_GLASS_PANE, " ", emptyList(), null)
        for (i in 0 until 27) {
            inv.setItem(i, filler)
        }

        val currentTool = minion.getTool()?.takeIf { !it.type.isAir }
        val toolLabel = Messages.TOOL_NAME(currentTool?.type)

        inv.setItem(
            11,
            makeButton(
                Material.CHEST_MINECART,
                "<#55FF55><b>✔ Coletar COM a Ferramenta</b>",
                listOf(
                    "",
                    " <gray>Ferramenta atual: <white>$toolLabel",
                    "",
                    " <gray>Guarda a ferramenta dentro do próprio item",
                    " <gray>do minion (mantendo encantamentos e durabilidade)",
                    " <gray>para guardar no inventário ou em baús.",
                    "",
                    " <#55FF55><b>(!)</b> Clique para recolher junto!"
                ),
                "pickup_with_tool",
                glow = currentTool != null
            )
        )

        inv.setItem(
            15,
            makeButton(
                Material.IRON_PICKAXE,
                "<#FFAA00><b>⛏ Coletar SEM a Ferramenta</b>",
                listOf(
                    "",
                    " <gray>Ferramenta atual: <white>$toolLabel",
                    "",
                    " <gray>Recolhe o minion e devolve a ferramenta",
                    " <gray>separadamente para o seu inventário.",
                    "",
                    " <#FFAA00><b>(!)</b> Clique para recolher separado!"
                ),
                "pickup_without_tool"
            )
        )

        inv.setItem(
            22,
            makeButton(
                Material.ARROW,
                "<red><b>⬅ Voltar</b>",
                listOf("<gray>Voltar para o menu do minion."),
                "back_main"
            )
        )

        player.openInventory(inv)
    }

    private fun buildStatItem(
        minion: Minion,
        spec: com.artillexstudios.axminions.api.skills.SkillSystemSpec,
        stat: StatSpec
    ): ItemStack {
        val tier = minion.getStatTier(stat.id)
        val maxTier = stat.maxTier
        val cost = spec.getCostForNextTier(stat.costPerTier, tier)
        val bar = renderBar(tier, maxTier)
        val mat = Material.matchMaterial(stat.material.uppercase(Locale.ENGLISH)) ?: Material.REDSTONE

        val lore = mutableListOf<String>()
        lore.add("")
        lore.add(" <gray>Barra: $bar <white>($tier/$maxTier)")
        lore.add("")
        for (line in stat.description) {
            lore.add(" $line")
        }
        val previews = SkillEffectFormatter.formatStatPreviewLines(minion, stat)
        if (previews.isNotEmpty()) {
            lore.add("")
            for (line in previews) {
                lore.add(" $line")
            }
        }
        lore.add("")
        if (tier >= maxTier) {
            lore.add(" <green><b>✔ Nível Máximo Atingido!</b>")
        } else {
            val canAfford = minion.getAvailableSkillPoints() >= cost
            val color = if (canAfford) "<#55FF55>" else "<red>"
            lore.add(" <gray>Próximo nível: $color$cost ponto(s)")
            lore.add("")
            lore.add(if (canAfford) "<yellow><b>(!)</b> Clique para evoluir!" else "<red>Pontos insuficientes!")
        }

        return makeButton(mat, stat.displayName, lore, "skill:${stat.id}", glow = tier > 0)
    }

    private fun buildAbilityItem(
        minion: Minion,
        spec: com.artillexstudios.axminions.api.skills.SkillSystemSpec,
        ability: CustomAbilitySpec
    ): ItemStack {
        val tier = minion.getAbilityTier(ability.id)
        val maxTier = ability.maxTier
        val cost = spec.getCostForNextTier(ability.costPerTier, tier)
        val bar = renderBar(tier, maxTier)
        val unlockedLevel = minion.getLevel() >= ability.minLevel
        val mat = if (unlockedLevel) {
            Material.matchMaterial(ability.material.uppercase(Locale.ENGLISH)) ?: Material.NETHER_STAR
        } else {
            Material.IRON_BARS
        }

        val status = when {
            !unlockedLevel -> "<red>🔒 Requer Minion Lv ${ability.minLevel}"
            tier > 0 -> "<green>🟢 ATIVA <gray>(Tier $tier/$maxTier)"
            else -> "<yellow>🟡 DISPONÍVEL"
        }

        val lore = mutableListOf<String>()
        lore.add(" $status")
        lore.add(" <gray>Progresso: $bar <white>($tier/$maxTier)")
        lore.add("")
        for (line in ability.description) {
            lore.add(" $line")
        }
        val effectLines = SkillEffectFormatter.formatAbilityEffectLines(minion, ability)
        if (effectLines.isNotEmpty()) {
            lore.add("")
            for (line in effectLines) {
                lore.add(" $line")
            }
        }
        lore.add("")
        if (unlockedLevel) {
            if (tier >= maxTier) {
                lore.add(" <green><b>✔ Poder no Nível Máximo!</b>")
            } else {
                val canAfford = minion.getAvailableSkillPoints() >= cost
                val color = if (canAfford) "<#55FF55>" else "<red>"
                lore.add(" <gray>Custo próximo tier: $color$cost ponto(s)")
                lore.add("")
                lore.add(if (canAfford) "<yellow><b>(!)</b> Clique para evoluir!" else "<red>Pontos insuficientes!")
            }
        }

        return makeButton(mat, ability.displayName, lore, "skill:${ability.id}", glow = tier > 0)
    }

    private fun makeButton(
        material: Material,
        name: String,
        lore: List<String>,
        guiTag: String?,
        glow: Boolean = false
    ): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.setDisplayName(StringUtils.formatToString(name))
        if (lore.isNotEmpty()) {
            meta.lore = lore.map { StringUtils.formatToString(it) }
        }
        if (glow) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        if (guiTag != null) {
            meta.persistentDataContainer.set(Keys.GUI, PersistentDataType.STRING, guiTag)
        }
        item.itemMeta = meta
        return item
    }
}
