package com.artillexstudios.axminions.menus

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.menus.DialogButtonSpec
import com.artillexstudios.axminions.api.menus.MinionDialogSpec
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.listeners.LinkingListener
import com.artillexstudios.axminions.nms.NMSHandler
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Method
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object MenuRouter {
    private val playerModeCache = ConcurrentHashMap<UUID, String>()
    private val numberFormat = NumberFormat.getCompactNumberInstance(Locale.ENGLISH, NumberFormat.Style.SHORT)

    private var floodgateInitAttempted = false
    private var floodgateGetInstance: Method? = null
    private var floodgateIsBedrock: Method? = null

    private var viaInitAttempted = false
    private var viaGetApi: Method? = null
    private var viaGetPlayerVersion: Method? = null
    private var minDialogProtocol = 771 // 1.21.6

    fun clearCache(uuid: UUID? = null) {
        if (uuid == null) {
            playerModeCache.clear()
        } else {
            playerModeCache.remove(uuid)
        }
    }

    fun isBedrockPlayer(player: Player): Boolean {
        initFloodgate()
        val getInst = floodgateGetInstance ?: return false
        val isBedrock = floodgateIsBedrock ?: return false
        return try {
            val api = getInst.invoke(null) ?: return false
            (isBedrock.invoke(api, player.uniqueId) as? Boolean) == true
        } catch (_: Throwable) {
            false
        }
    }

    fun supportsDialogProtocol(player: Player): Boolean {
        if (isBedrockPlayer(player)) return false
        initViaVersion()
        val getApi = viaGetApi ?: return true
        val getVersion = viaGetPlayerVersion ?: return true
        return try {
            val api = getApi.invoke(null) ?: return true
            val pv = (getVersion.invoke(api, player.uniqueId) as? Int) ?: return true
            if (pv < 0) true else pv >= minDialogProtocol
        } catch (_: Throwable) {
            true
        }
    }

    fun canUseDialog(player: Player): Boolean {
        return Config.USE_DIALOG_MENU() && supportsDialogProtocol(player)
    }

    fun getEffectiveMenuMode(player: Player): String {
        if (!canUseDialog(player)) return "GUI"
        val cached = playerModeCache[player.uniqueId]
        if (cached != null) return cached

        val persisted = try {
            AxMinionsPlugin.dataHandler.getUserMenuMode(player.uniqueId)
        } catch (_: Throwable) {
            null
        }
        val resolved = if (persisted.equals("GUI", true)) "GUI" else "DIALOG"
        playerModeCache[player.uniqueId] = resolved
        return resolved
    }

    fun setOrToggleMenuMode(player: Player, explicitMode: String? = null, reopenMinion: Minion? = null) {
        if (isBedrockPlayer(player)) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.MENU_MODE_BEDROCK()))
            if (reopenMinion != null) reopenMinion.openChestInventory(player)
            return
        }

        if (!supportsDialogProtocol(player)) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.MENU_MODE_OLD_CLIENT()))
            if (reopenMinion != null) reopenMinion.openChestInventory(player)
            return
        }

        val current = getEffectiveMenuMode(player)
        val target = when (explicitMode?.uppercase(Locale.ENGLISH)) {
            "GUI", "CLASSIC" -> "GUI"
            "DIALOG", "MODERN", "NOVO" -> "DIALOG"
            else -> if (current == "DIALOG") "GUI" else "DIALOG"
        }

        if (target == "DIALOG" && !Config.USE_DIALOG_MENU()) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.MENU_MODE_OLD_CLIENT()))
            if (reopenMinion != null) reopenMinion.openChestInventory(player)
            return
        }

        playerModeCache[player.uniqueId] = target
        val uuid = player.uniqueId
        val name = player.name
        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.setUserMenuMode(uuid, name, target)
        }

        val modeLabel = if (target == "DIALOG") "Menu Moderno (Dialog)" else "Menu Clássico (GUI)"
        player.sendMessage(
            StringUtils.formatToString(
                Messages.PREFIX() + Messages.MENU_MODE_SET(),
                Placeholder.unparsed("mode", modeLabel)
            )
        )

        if (reopenMinion != null) {
            if (target == "DIALOG") {
                openMainDialogOrFallback(reopenMinion, player)
            } else {
                reopenMinion.openChestInventory(player)
            }
        }
    }

    fun openMinionMenu(minion: Minion, player: Player) {
        LinkingListener.linking.remove(player)
        if (getEffectiveMenuMode(player) == "DIALOG") {
            openMainDialogOrFallback(minion, player)
        } else {
            minion.openChestInventory(player)
        }
    }

    fun openMainDialogOrFallback(minion: Minion, player: Player) {
        val spec = buildMainDialog(minion, player)
        val opened = try {
            NMSHandler.get().openMinionDialog(player, spec)
        } catch (t: Throwable) {
            if (Config.DEBUG()) {
                Bukkit.getLogger().warning("[AxMinions] Dialog fallback para GUI: ${t.message}")
            }
            false
        }
        if (!opened) {
            minion.openChestInventory(player)
        }
    }

    fun openSkillsDialog(minion: Minion, player: Player) {
        val spec = buildSkillsDialog(minion, player)
        if (!NMSHandler.get().openMinionDialog(player, spec)) {
            minion.openSkillsInventory(player)
        }
    }

    fun openPickupDialog(minion: Minion, player: Player) {
        val spec = buildPickupDialog(minion, player)
        if (!NMSHandler.get().openMinionDialog(player, spec)) {
            MinionSkillsGui.openPickupGui(minion, player)
        }
    }

    fun buildPickupDialog(minion: Minion, player: Player): MinionDialogSpec {
        val currentTool = minion.getTool()?.takeIf { !it.type.isAir }
        val toolLabel = Messages.TOOL_NAME(currentTool?.type)
        val title = StringUtils.formatToString("<red><b>⛏ Recolher Minion</b>")
        val bodyLines = listOf(
            StringUtils.formatToString("<gray>Ferramenta atual: <white>$toolLabel"),
            StringUtils.formatToString("<gray>Escolha como deseja recolher este minion:")
        )

        val actions = listOf(
            DialogButtonSpec(
                label = StringUtils.formatToString("<green>✔ Coletar Minion COM Ferramenta"),
                tooltip = StringUtils.formatToString("<gray>Guarda a ferramenta dentro do item do minion (mantém encantamentos e funciona em baús)."),
                width = 210,
                closeAfterClick = true,
                onClick = { clicker ->
                    minion.pickupByPlayer(clicker, true)
                    clicker.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.PICKUP_WITH_TOOL()))
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString("<yellow>⛏ Coletar Minion SEM Ferramenta"),
                tooltip = StringUtils.formatToString("<gray>Recolhe o minion e devolve a ferramenta separadamente no inventário."),
                width = 210,
                closeAfterClick = true,
                onClick = { clicker ->
                    minion.pickupByPlayer(clicker, false)
                    clicker.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.PICKUP_WITHOUT_TOOL()))
                }
            )
        )

        val exitButton = DialogButtonSpec(
            label = StringUtils.formatToString("<gray>⬅ Voltar ao Minion"),
            width = 160,
            onClick = { clicker ->
                openMainDialogOrFallback(minion, clicker)
            }
        )

        return MinionDialogSpec(
            title = title,
            bodyLines = bodyLines,
            iconItem = minion.getAsItem(),
            columns = 1,
            actions = actions,
            exitButton = exitButton
        )
    }

    fun buildMainDialog(minion: Minion, player: Player): MinionDialogSpec {
        val type = minion.getType()
        val level = minion.getLevel()
        val title = StringUtils.formatToString(
            type.getConfig().get("name"),
            Placeholder.parsed("level_color", Messages.LEVEL_COLOR(level)),
            Placeholder.unparsed("level", level.toString()),
            Placeholder.unparsed("owner", minion.getOwner()?.name ?: "???")
        )

        val toolName = Messages.TOOL_NAME(minion.getTool()?.takeIf { !it.type.isAir }?.type)
        val chestLoc = minion.getLinkedChest()
        val chestLabel = if (chestLoc != null) {
            "<green>${chestLoc.blockX}, ${chestLoc.blockY}, ${chestLoc.blockZ}</green>"
        } else {
            "<red>Não vinculado</red>"
        }

        val isSeller = type == MinionTypes.getMinionTypes()["seller"]
        val storageLabel = if (isSeller) "${numberFormat.format(minion.getStorage())}$" else "${numberFormat.format(minion.getStorage())} XP"
        val freePoints = minion.getAvailableSkillPoints()
        val maxReached = type.hasReachedMaxLevel(minion)

        val bodyLines = listOf(
            StringUtils.formatToString("<gray>Nível: <#FFEE55>$level</#FFEE55> <dark_gray>| <gray>Pontos Livres: <#55FF55>$freePoints pts</#55FF55>"),
            StringUtils.formatToString("<gray>Ações: <#00EEFF>${minion.getActionAmount()}</#00EEFF> <dark_gray>| <gray>Armazenado: <#CC66FF>$storageLabel</#CC66FF>"),
            StringUtils.formatToString("<gray>Ferramenta: <white>$toolName <dark_gray>| <gray>Baú: $chestLabel"),
            StringUtils.formatToString("<gray>Direção: <white>${Messages.ROTATION_NAME(minion.getDirection())} <dark_gray>| <gray>Alcance efetivo: <aqua>${String.format(Locale.US, "%.1f", minion.getEffectiveRange())}")
        )

        val upgradeLabel = if (maxReached) {
            "<green>✔ Nível Máximo ($level)"
        } else {
            "<gold>⬆ Melhorar (Lv $level → ${level + 1})"
        }
        val upgradeTooltip = if (maxReached) {
            "<green>Este minion já atingiu o nível máximo!"
        } else {
            val reqMoney = type.getDouble("requirements.money", level + 1)
            val reqActions = type.getDouble("requirements.actions", level + 1)
            "<gray>Custo: <#55FF55>${numberFormat.format(reqMoney)}$ <dark_gray>| <gray>Ações: <#55FF55>${numberFormat.format(reqActions)}"
        }

        val linkLabel = if (chestLoc != null) "<red>📦 Desvincular Baú" else "<green>📦 Vincular Baú"
        val collectLabel = if (isSeller) "<#CC66FF>💰 Coletar (${numberFormat.format(minion.getStorage())}$)" else "<#CC66FF>✨ Coletar XP (${minion.getStorage().toInt()})"

        val actions = mutableListOf(
            DialogButtonSpec(
                label = StringUtils.formatToString(upgradeLabel),
                tooltip = StringUtils.formatToString(upgradeTooltip),
                onClick = { clicker ->
                    MinionActions.upgrade(minion, clicker)
                    openMainDialogOrFallback(minion, clicker)
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString("<aqua>✦ Habilidades ($freePoints pts)"),
                tooltip = StringUtils.formatToString("<gray>Ganhe pontos a cada nível para evoluir atributos e desbloquear poderes especiais."),
                onClick = { clicker ->
                    openSkillsDialog(minion, clicker)
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString("<yellow>✋ Ferramenta"),
                tooltip = StringUtils.formatToString("<gray>Abrir slot para equipar ou remover a ferramenta."),
                closeAfterClick = true,
                onClick = { clicker ->
                    minion.openToolSwapInventory(clicker)
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString(collectLabel),
                tooltip = StringUtils.formatToString("<gray>Coleta as moedas ou XP armazenados no minion."),
                onClick = { clicker ->
                    MinionActions.collectStorage(minion, clicker)
                    openMainDialogOrFallback(minion, clicker)
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString("<white>🧭 Rotação (${Messages.ROTATION_NAME(minion.getDirection())})"),
                tooltip = StringUtils.formatToString("<gray>Gira a direção de trabalho do minion."),
                onClick = { clicker ->
                    MinionActions.rotate(minion)
                    openMainDialogOrFallback(minion, clicker)
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString(linkLabel),
                tooltip = StringUtils.formatToString("<gray>Clique para vincular ou desvincular um baú."),
                closeAfterClick = chestLoc == null,
                onClick = { clicker ->
                    val started = MinionActions.toggleLink(minion, clicker, closeGuiOnStart = false)
                    if (!started) {
                        openMainDialogOrFallback(minion, clicker)
                    }
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString("<red>⛏ Recolher Minion"),
                tooltip = StringUtils.formatToString("<gray>Escolha entre recolher com ou sem a ferramenta."),
                closeAfterClick = false,
                onClick = { clicker ->
                    openPickupDialog(minion, clicker)
                }
            ),
            DialogButtonSpec(
                label = StringUtils.formatToString("<gray>🔄 Trocar p/ Menu GUI"),
                tooltip = StringUtils.formatToString("<gray>Alterna sua preferência para o menu clássico de baú."),
                closeAfterClick = true,
                onClick = { clicker ->
                    setOrToggleMenuMode(clicker, "GUI", minion)
                }
            )
        )

        val exitButton = DialogButtonSpec(
            label = StringUtils.formatToString("<dark_gray>✖ Fechar"),
            width = 160,
            closeAfterClick = true,
            onClick = {}
        )

        return MinionDialogSpec(
            title = title,
            bodyLines = bodyLines,
            iconItem = minion.getAsItem(),
            columns = 2,
            actions = actions,
            exitButton = exitButton
        )
    }

    fun buildSkillsDialog(minion: Minion, player: Player): MinionDialogSpec {
        val spec = minion.getType().getSkillSystemSpec()
        val available = minion.getAvailableSkillPoints()
        val spent = minion.getSpentSkillPoints()
        val total = minion.getTotalSkillPoints()

        val title = StringUtils.formatToString("<aqua><b>✦ Habilidades <dark_gray>(<green>$available pts</green><dark_gray>)")
        val bodyLines = listOf(
            StringUtils.formatToString("<gray>Nível do Minion: <#FFEE55>${minion.getLevel()}/20 <dark_gray>| <gray>Pontos: <#55FF55>$available livres</#55FF55> <gray>($spent/$total gastos)"),
            StringUtils.formatToString("<gray>Cada nível concede pontos de evolução. Clique em um atributo ou poder abaixo para investir.")
        )

        val buttons = mutableListOf<DialogButtonSpec>()

        for (stat in spec.stats.values) {
            val tier = minion.getStatTier(stat.id)
            val cost = spec.getCostForNextTier(stat.costPerTier, tier)
            val bar = MinionSkillsGui.renderBar(tier, stat.maxTier, plain = false)
            val costTag = if (tier >= stat.maxTier) "<green>MAX" else "<yellow>${cost}p"
            val previews = com.artillexstudios.axminions.skills.SkillEffectFormatter.formatStatPreviewLines(minion, stat)
            val tooltipLines = (listOf("${stat.displayName} <white>($tier/${stat.maxTier})") + stat.description + listOf("") + previews).joinToString("\n")
            buttons.add(
                DialogButtonSpec(
                    label = StringUtils.formatToString("${stat.displayName} $bar $costTag"),
                    tooltip = StringUtils.formatToString(tooltipLines),
                    width = 180,
                    onClick = { clicker ->
                        minion.upgradeSkill(stat.id, clicker)
                        openSkillsDialog(minion, clicker)
                    }
                )
            )
        }

        for (ability in spec.abilities.values) {
            if (!ability.enabled) continue
            val tier = minion.getAbilityTier(ability.id)
            val unlocked = minion.getLevel() >= ability.minLevel
            val cost = spec.getCostForNextTier(ability.costPerTier, tier)
            val icon = when {
                !unlocked -> "<red>🔒"
                tier > 0 -> "<green>🟢"
                else -> "<yellow>🟡"
            }
            val suffix = when {
                !unlocked -> "<red>(Lv ${ability.minLevel})"
                tier >= ability.maxTier -> "<green>MAX"
                else -> "<white>($tier/${ability.maxTier}) <yellow>${cost}p"
            }
            val effectLines = com.artillexstudios.axminions.skills.SkillEffectFormatter.formatAbilityEffectLines(minion, ability)
            val tooltipLines = (listOf("${ability.displayName} $suffix") + ability.description + listOf("") + effectLines).joinToString("\n")
            buttons.add(
                DialogButtonSpec(
                    label = StringUtils.formatToString("$icon ${ability.displayName} $suffix"),
                    tooltip = StringUtils.formatToString(tooltipLines),
                    width = 180,
                    onClick = { clicker ->
                        minion.upgradeSkill(ability.id, clicker)
                        openSkillsDialog(minion, clicker)
                    }
                )
            )
        }

        buttons.add(
            DialogButtonSpec(
                label = StringUtils.formatToString("<red>♻ Respec (${numberFormat.format(spec.respecCostMoney)}$)"),
                tooltip = StringUtils.formatToString("<gray>Reseta os $spent pontos investidos para refazer a build."),
                width = 180,
                onClick = { clicker ->
                    minion.respecSkills(clicker)
                    openSkillsDialog(minion, clicker)
                }
            )
        )

        val exitButton = DialogButtonSpec(
            label = StringUtils.formatToString("<gray>⬅ Voltar ao Minion"),
            width = 160,
            onClick = { clicker ->
                openMainDialogOrFallback(minion, clicker)
            }
        )

        return MinionDialogSpec(
            title = title,
            bodyLines = bodyLines,
            iconItem = null,
            columns = 2,
            actions = buttons,
            exitButton = exitButton
        )
    }

    @Synchronized
    private fun initFloodgate() {
        if (floodgateInitAttempted) return
        floodgateInitAttempted = true
        try {
            val clazz = Class.forName("org.geysermc.floodgate.api.FloodgateApi")
            floodgateGetInstance = clazz.getMethod("getInstance")
            floodgateIsBedrock = clazz.getMethod("isFloodgatePlayer", UUID::class.java)
        } catch (_: Throwable) {
        }
    }

    @Synchronized
    private fun initViaVersion() {
        if (viaInitAttempted) return
        viaInitAttempted = true
        try {
            val viaClazz = Class.forName("com.viaversion.viaversion.api.Via")
            val apiClazz = Class.forName("com.viaversion.viaversion.api.ViaAPI")
            viaGetApi = viaClazz.getMethod("getAPI")
            viaGetPlayerVersion = apiClazz.getMethod("getPlayerVersion", UUID::class.java)

            val pvClazz = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion")
            val pvField = pvClazz.getField("v1_21_6").get(null)
            minDialogProtocol = pvClazz.getMethod("getVersion").invoke(pvField) as? Int ?: 771
        } catch (_: Throwable) {
        }
    }
}
