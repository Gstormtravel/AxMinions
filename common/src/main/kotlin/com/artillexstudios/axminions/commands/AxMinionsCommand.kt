package com.artillexstudios.axminions.commands

import com.artillexstudios.axapi.scheduler.Scheduler
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.data.DataHandler
import com.artillexstudios.axminions.api.minions.miniontype.MinionType
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.api.utils.MinionUtils
import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.converter.LitMinionsConverter
import com.artillexstudios.axminions.integrations.island.SuperiorSkyBlock2Integration
import com.artillexstudios.axminions.integrations.protection.EliteMobsProtection
import com.artillexstudios.axminions.logging.MinionLogger
import com.artillexstudios.axminions.menus.MenuRouter
import com.artillexstudios.axminions.menus.MinionSkillsGui
import com.artillexstudios.axminions.minions.Minion
import com.artillexstudios.axminions.minions.Minions
import com.artillexstudios.axminions.skills.AbilityEngine
import com.artillexstudios.axminions.skills.SkillEffectFormatter
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World.Environment
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.util.concurrent.CompletableFuture

@Command("axminions", "minion", "minions")
class AxMinionsCommand {

    @Subcommand("give")
    @CommandPermission("axminions.command.give")
    @Description("Give a minion to a player")
    @AutoComplete("* @minionTypes * *")
    fun give(
        sender: CommandSender,
        receiver: Player,
        minionType: MinionType,
        @Default("1") level: Int,
        @Default("1") @Range(min = 1.0, max = 64.0) amount: Int
    ) {
        val maxLevel = minionType.getConfig().getSection("upgrades")?.keys?.size ?: 10

        if (level > maxLevel || level < 1) {
            sender.sendMessage(
                StringUtils.formatToString(
                    Messages.PREFIX() + Messages.GIVE_MAX_LEVEL(),
                    Placeholder.unparsed("max_level", maxLevel.toString()),
                    Placeholder.unparsed("level", level.toString())
                )
            )
            return
        }

        val item = minionType.getItem(level)
        item.amount = amount

        receiver.inventory.addItem(item)
    }

    @Subcommand("fuel give")
    @CommandPermission("axminions.command.fuel.give")
    fun give(sender: CommandSender, id: String, receiver: Player, @Default("1") amount: Int) {
        val item = ItemBuilder.create(Config.CHARGE_ITEMS().getSection(id)).get()
        item.amount = amount
        receiver.inventory.addItem(item)
    }

    @Subcommand("fuel add")
    @CommandPermission("axminions.command.fuel.add")
    fun add(player: Player, id: String, charge: Int) {
        val item = ItemBuilder.create(player.inventory.itemInMainHand).serialize(true)
        item["charge"] = charge
        AxMinionsPlugin.config.getConfig().set("charge.items.$id", item)
        AxMinionsPlugin.config.getConfig().save()
    }

    @Subcommand("reload")
    @CommandPermission("axminions.command.reload")
    @Description("Reload the configurations of the plugin")
    fun reload(sender: CommandSender) {
        val start = System.currentTimeMillis()
        AxMinionsPlugin.config.reload()
        AxMinionsPlugin.INSTANCE.reloadMessages()

        MinionTypes.getMinionTypes().fastFor { _, v ->
            v.getConfig().reload()
            v.invalidateSkillCache()
        }

        AbilityEngine.clearCooldowns()
        MenuRouter.clearCache()

        AxMinionsAPI.INSTANCE.getMinions().fastFor {
            it.markDirty()
            // NOVO: Força a atualização do baú no reload
            (it as? com.artillexstudios.axminions.minions.Minion)?.updateLinkedInventory()
        }

        AxMinionsPlugin.integrations.reload()

        sender.sendMessage(
            StringUtils.formatToString(
                Messages.PREFIX() + Messages.RELOAD_SUCCESS(),
                Placeholder.unparsed("time", (System.currentTimeMillis() - start).toString())
            )
        )
    }

    @Subcommand("menu")
    @CommandPermission("axminions.command.menu")
    @Description("Alterna entre o menu moderno (Dialog) e o menu clássico (GUI)")
    fun menu(player: Player, @Default("") mode: String) {
        MenuRouter.setOrToggleMenuMode(player, mode.ifBlank { null })
    }

    @Subcommand("convert")
    @CommandPermission("axminions.command.convert")
    @Description("Convert from a different plugin")
    fun convert(sender: CommandSender) {
        val converter = LitMinionsConverter()
        converter.convert()
    }

    @Subcommand("stats", "statistics")
    @CommandPermission("axminions.command.statistics")
    @Description("Get statistics of plugin")
    fun stats(sender: CommandSender) {
        val minions = AxMinionsAPI.INSTANCE.getMinions()
        var loaded = 0
        val total = minions.size

        minions.fastFor {
            if (it.isTicking()) {
                loaded++
            }
        }

        sender.sendMessage(
            StringUtils.formatToString(
                Messages.STATISTICS(),
                Placeholder.unparsed("ticking", loaded.toString()),
                Placeholder.unparsed("not-ticking", (total - loaded).toString()),
                Placeholder.unparsed("total", total.toString())
            )
        )
    }

    @Subcommand("reset")
    @CommandPermission("axminions.command.reset")
    fun reset(sender: CommandSender, offlinePlayer: OfflinePlayer) {
        val minions = AxMinionsAPI.INSTANCE.getMinions()
        val ownerUUID = offlinePlayer.uniqueId
        AxMinionsPlugin.dataQueue.submit {
            minions.fastFor {
                if (it.getOwnerUUID() == ownerUUID) {
                    it.remove()
                }
            }

            sender.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.RESET(), Placeholder.unparsed("player", offlinePlayer.name ?: "---")))
        }
    }

    @Subcommand("extraslot")
    @CommandPermission("axminions.command.extraslot")
    fun extraSlot(commandSender: CommandSender, offlinePlayer: OfflinePlayer, amount: Int) {
        AxMinionsPlugin.dataQueue.submit {
            val original = AxMinionsPlugin.dataHandler.getExtraSlots(offlinePlayer.uniqueId)
            AxMinionsPlugin.dataHandler.addUser(offlinePlayer.uniqueId, offlinePlayer.name ?: "---")
            AxMinionsPlugin.dataHandler.addExtraSlot(offlinePlayer.uniqueId, amount)
            commandSender.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SLOT_GIVE(), Placeholder.unparsed("player", offlinePlayer.name ?: "???"), Placeholder.unparsed("amount", amount.toString())))
            offlinePlayer.player?.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.SLOT_RECEIVE(), Placeholder.unparsed("amount", amount.toString()), Placeholder.unparsed("from", original.toString()), Placeholder.unparsed("to", (original + amount).toString())))
        }
    }

    /**
     * Vincula programaticamente um minion a um baú, sem precisar de GUI.
     * Uso: /axminions debug link <minion_x> <minion_y> <minion_z> <chest_x> <chest_y> <chest_z>
     * O minion é buscado por localização (posição real, sem o +0.5 do spawn).
     */
    @Subcommand("debug link")
    @CommandPermission("axminions.*")
    @Description("Debug: Vincula um minion ao bau sem GUI")
    fun debugLink(
        sender: CommandSender,
        minionX: Int, minionY: Int, minionZ: Int,
        chestX: Int, chestY: Int, chestZ: Int
    ) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: run {
            sender.sendMessage("[AxMinions-Debug] Nenhum mundo encontrado!")
            return
        }

        // O minion fica em X+0.5, Y, Z+0.5 após o spawn()
        val minionSearchLoc = org.bukkit.Location(world, minionX + 0.5, minionY.toDouble(), minionZ + 0.5)
        val chestLoc = org.bukkit.Location(world, chestX.toDouble(), chestY.toDouble(), chestZ.toDouble())

        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        val target = allMinions.firstOrNull { minion ->
            val loc = minion.getLocation()
            loc.world?.uid == world.uid &&
            Math.abs(loc.x - minionSearchLoc.x) < 0.6 &&
            Math.abs(loc.y - minionSearchLoc.y) < 0.6 &&
            Math.abs(loc.z - minionSearchLoc.z) < 0.6
        }

        if (target == null) {
            sender.sendMessage("[AxMinions-Debug] Nenhum minion encontrado em ($minionX, $minionY, $minionZ)!")
            sender.sendMessage("[AxMinions-Debug] Minions carregados: ${allMinions.size}")
            allMinions.fastFor { m ->
                sender.sendMessage("  -> ${m.getType().getName()} em ${m.getLocation().blockX},${m.getLocation().blockY},${m.getLocation().blockZ} ticking=${m.isTicking()}")
            }
            return
        }

        // Verifica se o bloco é um container válido
        val chestBlock = chestLoc.block
        if (chestBlock.state !is org.bukkit.block.Container) {
            sender.sendMessage("[AxMinions-Debug] Bloco em ($chestX, $chestY, $chestZ) não é um container! Tipo: ${chestBlock.type}")
            return
        }

        target.setLinkedChest(chestLoc)
        sender.sendMessage("[AxMinions-Debug] ✅ Minion ${target.getType().getName()} em (${target.getLocation().blockX},${target.getLocation().blockY},${target.getLocation().blockZ}) vinculado ao baú em ($chestX, $chestY, $chestZ)")
        sender.sendMessage("[AxMinions-Debug] isTicking=${target.isTicking()} linkedInv=${target.getLinkedInventory() != null}")
    }

    /**
     * Exibe info detalhada de todos os minions (debug).
     * Uso: /axminions debug info
     */
    @Subcommand("debug info")
    @CommandPermission("axminions.*")
    @Description("Debug: Lista todos os minions e seus estados")
    fun debugInfo(sender: CommandSender) {
        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        sender.sendMessage("[AxMinions-Debug] Total de minions: ${allMinions.size} | Logs em buffer: ${MinionLogger.getRecentLogs(500).size}")
        allMinions.fastFor { m ->
            val loc = m.getLocation()
            val chest = m.getLinkedChest()
            val hasInv = m.getLinkedInventory() != null
            val firstOwner = m.getFirstOwner().ifBlank { m.getOwner()?.name ?: "?" }
            val activeAb = m.getSkillAllocations().filter { it.value > 0 }.entries.joinToString(",") { "${it.key}:${it.value}" }
            sender.sendMessage(
                "  [${m.getType().getName()}] L${m.getLevel()} (${loc.blockX},${loc.blockY},${loc.blockZ}) | " +
                "1oDono=$firstOwner | ticking=${m.isTicking()} | range=${m.getRange()} | next=${m.getNextAction()}t | " +
                "storage=${m.getStorage().toLong()} | bau=${if (chest != null) "(${chest.blockX},${chest.blockY},${chest.blockZ})[$hasInv]" else "null"} | " +
                "tool=${m.getTool()?.type ?: "AIR"} | skills=[$activeAb]"
            )
        }
    }

    @Subcommand("admin")
    @CommandPermission("axminions.command.admin")
    @Description("Abre o painel administrativo de supervisao dos minions")
    fun adminGui(player: Player, @Default("") filterOwner: String) {
        MinionSkillsGui.openAdminGui(player, 0, filterOwner.ifBlank { null })
    }

    @Subcommand("list")
    @CommandPermission("axminions.command.admin")
    @Description("Lista minions por dono com historico de posse")
    fun adminList(sender: CommandSender, @Default("") ownerFilter: String, @Default("1") page: Int) {
        val all = AxMinionsAPI.INSTANCE.getMinions().filter { m ->
            ownerFilter.isBlank() ||
                m.getOwner()?.name?.contains(ownerFilter, ignoreCase = true) == true ||
                m.getFirstOwner().contains(ownerFilter, ignoreCase = true) ||
                m.getOwnershipHistoryString().contains(ownerFilter, ignoreCase = true)
        }
        val perPage = 10
        val totalPages = ((all.size + perPage - 1) / perPage).coerceAtLeast(1)
        val safePage = page.coerceIn(1, totalPages)
        val slice = all.drop((safePage - 1) * perPage).take(perPage)

        sender.sendMessage("§b[AxMinions Admin] §fMinions (${all.size}) - Pagina $safePage/$totalPages:")
        for (m in slice) {
            val l = m.getLocation()
            val owner = m.getOwner()?.name ?: m.getOwnerUUID().toString().take(8)
            val first = m.getFirstOwner().ifBlank { owner }
            val hist = m.getOwnershipHistoryString().ifBlank { first }
            sender.sendMessage(
                " §8• §e${m.getType().getName()} Lv${m.getLevel()} §7em §b${l.world?.name}(${l.blockX},${l.blockY},${l.blockZ}) " +
                "§7| Atual: §a$owner §7| 1º: §e$first §7| Hist: §f$hist"
            )
        }
    }

    @Subcommand("inspect")
    @CommandPermission("axminions.command.admin")
    @Description("Inspeciona o minion mais proximo ou nas coordenadas")
    fun adminInspect(player: Player, @Default("0") x: Int, @Default("-999") y: Int, @Default("0") z: Int) {
        val loc = if (y == -999) player.location else Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
        val target = AxMinionsAPI.INSTANCE.getMinions()
            .filter { it.getLocation().world?.uid == loc.world?.uid }
            .minByOrNull { it.getLocation().distanceSquared(loc) }
            ?.takeIf { y != -999 || it.getLocation().distanceSquared(loc) <= 64.0 }

        if (target == null) {
            player.sendMessage("§cNenhum minion encontrado perto dessa posicao.")
            return
        }

        val l = target.getLocation()
        val owner = target.getOwner()?.name ?: target.getOwnerUUID().toString()
        val first = target.getFirstOwner().ifBlank { owner }
        val hist = target.getOwnershipHistoryString().ifBlank { first }
        val spec = target.getType().getSkillSystemSpec()

        player.sendMessage("§b§l[AxMinions Inspect] §e${target.getType().getName()} Lv ${target.getLevel()} §7@ (${l.blockX}, ${l.blockY}, ${l.blockZ})")
        player.sendMessage(" §7Primeiro Dono: §a$first §7| Dono Atual: §a$owner")
        player.sendMessage(" §7Historico de Maos: §f$hist")
        player.sendMessage(" §7Acoes: §e${target.getActionAmount()} §7| Reserva: §6${target.getStorage().toLong()} §7| Ticking: §f${target.isTicking()}")
        player.sendMessage(" §7Ferramenta: §f${Messages.TOOL_NAME(target.getTool()?.type)} §7| Pontos: §a${target.getSpentSkillPoints()}/${target.getTotalSkillPoints()}")
        val skillsStr = target.getSkillAllocations().filter { it.value > 0 }
            .entries.joinToString("§7, §b") { "${it.key}=${it.value}" }
        player.sendMessage(" §7Skills: §b${skillsStr.ifBlank { "Nenhuma" }}")
        val unlockedCount = spec.abilities.values.count { target.getAbilityTier(it.id) > 0 }
        player.sendMessage(" §7Poderes ativos: §d$unlockedCount/${spec.abilities.size}")
    }

    @Subcommand("logs")
    @CommandPermission("axminions.command.admin")
    @Description("Lista os ultimos registros de auditoria dos minions")
    fun adminLogs(sender: CommandSender, @Default("15") limit: Int, @Default("") filter: String) {
        val entries = MinionLogger.getRecentLogs(limit.coerceIn(1, 50), filter)
        if (entries.isEmpty()) {
            sender.sendMessage("§e[AxMinions Logs] Nenhum registro encontrado para esse filtro.")
            return
        }
        sender.sendMessage("§b[AxMinions Logs] §fExibindo ${entries.size} evento(s) (pasta plugins/AxMinions/logs/):")
        for (line in entries) {
            sender.sendMessage("§7$line")
        }
    }

    @Subcommand("tp")
    @CommandPermission("axminions.command.admin")
    @Description("Teleporta para um minion pelas coordenadas")
    fun adminTp(player: Player, x: Int, y: Int, z: Int, @Default("") worldName: String) {
        val w = if (worldName.isBlank()) player.world else Bukkit.getWorld(worldName) ?: player.world
        val target = AxMinionsAPI.INSTANCE.getMinions().firstOrNull { m ->
            val l = m.getLocation()
            l.world?.uid == w.uid &&
                Math.abs(l.blockX - x) <= 1 &&
                Math.abs(l.blockY - y) <= 1 &&
                Math.abs(l.blockZ - z) <= 1
        }
        if (target == null) {
            player.sendMessage("§cNenhum minion encontrado em ${w.name} ($x, $y, $z).")
            return
        }
        player.teleport(target.getLocation().clone().add(0.5, 1.0, 0.5))
        player.sendMessage("§aTeleportado para ${target.getType().getName()} em ${w.name} ($x, $y, $z).")
    }

    @Subcommand("debug abilities")
    @CommandPermission("axminions.*")
    @Description("Debug: Inspeciona efeitos e metricas calculadas do minion")
    fun debugAbilities(sender: CommandSender, x: Int, y: Int, z: Int) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: return
        val target = AxMinionsAPI.INSTANCE.getMinions().firstOrNull { m ->
            val l = m.getLocation()
            l.world?.uid == world.uid && Math.abs(l.blockX - x) <= 1 && Math.abs(l.blockY - y) <= 1 && Math.abs(l.blockZ - z) <= 1
        } ?: run {
            sender.sendMessage("[AxMinions-Debug] Nenhum minion em ($x, $y, $z).")
            return
        }
        val spec = target.getType().getSkillSystemSpec()
        sender.sendMessage("[AxMinions-Debug] Skills de ${target.getType().getName()} L${target.getLevel()} (${target.getAvailableSkillPoints()} pts livres):")
        for (stat in spec.stats.values) {
            val preview = SkillEffectFormatter.formatStatPreviewLines(target, stat).joinToString(" | ")
            sender.sendMessage("  Stat ${stat.id} T${target.getStatTier(stat.id)}/${stat.maxTier} -> $preview")
        }
        for (ab in spec.abilities.values) {
            val preview = SkillEffectFormatter.formatAbilityEffectLines(target, ab).joinToString(" | ")
            sender.sendMessage("  Ability ${ab.id} (Lv${ab.minLevel}) T${target.getAbilityTier(ab.id)}/${ab.maxTier} -> $preview")
        }
    }

    @Subcommand("debug xp")
    @CommandPermission("axminions.*")
    @Description("Debug: Verifica XP/stack/EliteMobs ao redor do jogador")
    fun debugXp(player: Player) {
        val stacker = AxMinionsPlugin.integrations.getStackerIntegration()
        val entities = player.world.getNearbyEntities(player.location, 8.0, 6.0, 8.0)
            .filterIsInstance<org.bukkit.entity.LivingEntity>()
        player.sendMessage("[AxMinions-Debug-XP] Entidades num raio de 8 blocos (${entities.size}):")
        for (e in entities.take(10)) {
            val stackSize = stacker.getStackSize(e)
            val isElite = EliteMobsProtection.isEliteEntity(e)
            player.sendMessage("  ${e.type} stack=$stackSize eliteBlocked=$isElite hp=${String.format("%.1f", e.health)}")
        }
    }

    @Subcommand("debug wood")
    @CommandPermission("axminions.*")
    @Description("Debug: Testa deteccao de madeiras e folhas MC 26.3 ao redor")
    fun debugWood(player: Player) {
        var woodCount = 0
        var leafCount = 0
        val foundTypes = mutableSetOf<String>()
        val center = player.location.block
        for (dx in -4..4) for (dy in -2..4) for (dz in -4..4) {
            val b = center.getRelative(dx, dy, dz)
            if (MinionUtils.isWoodBlock(b.type)) {
                woodCount++
                foundTypes.add(b.type.name)
            } else if (MinionUtils.isLeafBlock(b.type)) {
                leafCount++
            }
        }
        player.sendMessage("[AxMinions-Debug-Wood] Madeiras=$woodCount | Folhas=$leafCount | Tipos=[${foundTypes.joinToString()}]")
    }

    @Subcommand("debug logs")
    @CommandPermission("axminions.*")
    @Description("Debug: Escreve log de teste e mostra status da auditoria")
    fun debugLogs(sender: CommandSender) {
        MinionLogger.logSystem(sender.name, sender.name, sender.name, "DEBUG", (sender as? Player)?.location, "DEBUG_TEST", "Verificacao manual do sistema de logs.")
        sender.sendMessage("[AxMinions-Debug-Logs] Buffer=${MinionLogger.getRecentLogs(500).size} | Arquivo=${MinionLogger.getLatestLogFile()?.name ?: "nenhum ainda"}")
    }

    /**
     * Força a re-sincronização de todos os inventários (igual ao boot delay).
     * Uso: /axminions debug sync
     */
    @Subcommand("debug sync")
    @CommandPermission("axminions.*")
    @Description("Debug: Força sincronização de todos os baús vinculados")
    fun debugSync(sender: CommandSender) {
        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        var count = 0
        allMinions.fastFor { m ->
            (m as? Minion)?.updateLinkedInventory()
            if (m.getLinkedChest() != null) count++
        }
        sender.sendMessage("[AxMinions-Debug] ✅ Sincronização forçada em $count minions com baú vinculado.")
    }

    /**
     * Dá uma ferramenta ao minion especificado.
     * Uso: /axminions debug settool <x> <y> <z> [material] [encantamentos: lure:3,luck_of_the_sea:3]
     */
    @Subcommand("debug settool")
    @CommandPermission("axminions.*")
    @Description("Debug: Seta a ferramenta do minion com material e encantamentos opcionais")
    fun debugSetTool(
        sender: CommandSender,
        minionX: Int, minionY: Int, minionZ: Int,
        @Default("AUTO") materialName: String,
        @Default("") enchantStr: String
    ) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: return
        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        val target = allMinions.firstOrNull { minion ->
            val loc = minion.getLocation()
            loc.world?.uid == world.uid &&
            Math.abs(loc.blockX - minionX) <= 1 &&
            Math.abs(loc.blockY - minionY) <= 1 &&
            Math.abs(loc.blockZ - minionZ) <= 1
        }

        if (target == null) {
            sender.sendMessage("[AxMinions-Debug] Nenhum minion em ($minionX, $minionY, $minionZ)")
            return
        }

        val mat = if (materialName.equals("AUTO", true)) {
            when (target.getType().getName().lowercase()) {
                "fisher" -> Material.FISHING_ROD
                "miner" -> Material.DIAMOND_PICKAXE
                "lumber" -> Material.DIAMOND_AXE
                "farmer" -> Material.DIAMOND_HOE
                "slayer" -> Material.DIAMOND_SWORD
                else -> Material.DIAMOND_SWORD
            }
        } else {
            Material.matchMaterial(materialName) ?: Material.DIAMOND_SWORD
        }

        val item = org.bukkit.inventory.ItemStack(mat)
        if (enchantStr.isNotBlank()) {
            enchantStr.split(",").forEach { part ->
                val tokens = part.split(":")
                val enchName = tokens[0].trim()
                val enchLevel = if (tokens.size > 1) tokens[1].trim().toIntOrNull() ?: 1 else 1
                val ench = org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(enchName.lowercase()))
                    ?: org.bukkit.enchantments.Enchantment.getByName(enchName.uppercase())
                if (ench != null) {
                    item.addUnsafeEnchantment(ench, enchLevel)
                }
            }
        }

        target.setTool(item, true)
        sender.sendMessage("[AxMinions-Debug] ✅ Ferramenta ${item.type} (ench=${item.enchantments}) setada no ${target.getType().getName()} em ($minionX, $minionY, $minionZ). canUseTool=${target.canUseTool()}")
    }

    /**
     * Define o nível do minion.
     * Uso: /axminions debug setlevel <x> <y> <z> <level>
     */
    @Subcommand("debug setlevel")
    @CommandPermission("axminions.*")
    @Description("Debug: Altera o nível do minion")
    fun debugSetLevel(sender: CommandSender, minionX: Int, minionY: Int, minionZ: Int, level: Int) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: return
        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        val target = allMinions.firstOrNull { minion ->
            val loc = minion.getLocation()
            loc.world?.uid == world.uid &&
            Math.abs(loc.blockX - minionX) <= 1 &&
            Math.abs(loc.blockY - minionY) <= 1 &&
            Math.abs(loc.blockZ - minionZ) <= 1
        }

        if (target == null) {
            sender.sendMessage("[AxMinions-Debug] Nenhum minion em ($minionX, $minionY, $minionZ)")
            return
        }

        target.setLevel(level)
        target.markDirty()
        sender.sendMessage("[AxMinions-Debug] ✅ Nível do ${target.getType().getName()} alterado para $level! nextAction=${target.getNextAction()}")
    }

    /**
     * Abre a GUI do minion diretamente para o jogador.
     * Uso: /axminions debug open <x> <y> <z> [player]
     */
    @Subcommand("debug open")
    @CommandPermission("axminions.*")
    @Description("Debug: Abre a interface do minion para um jogador")
    fun debugOpen(
        sender: CommandSender,
        minionX: Int, minionY: Int, minionZ: Int,
        @Optional targetPlayer: Player? = null
    ) {
        val player = targetPlayer ?: (sender as? Player) ?: run {
            sender.sendMessage("[AxMinions-Debug] Especifique um jogador para abrir a GUI!")
            return
        }
        val world = player.world
        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        val target = allMinions.firstOrNull { minion ->
            val loc = minion.getLocation()
            loc.world?.uid == world.uid &&
            Math.abs(loc.blockX - minionX) <= 1 &&
            Math.abs(loc.blockY - minionY) <= 1 &&
            Math.abs(loc.blockZ - minionZ) <= 1
        }

        if (target == null) {
            sender.sendMessage("[AxMinions-Debug] Nenhum minion em ($minionX, $minionY, $minionZ)")
            return
        }

        target.openInventory(player)
        sender.sendMessage("[AxMinions-Debug] ✅ GUI do ${target.getType().getName()} aberta para ${player.name}!")
    }

    /**
     * Inspeciona o inventário do baú.
     * Uso: /axminions debug checkchest <x> <y> <z>
     */
    @Subcommand("debug checkchest")
    @CommandPermission("axminions.*")
    @Description("Debug: Inspeciona itens no baú")
    fun debugCheckChest(sender: CommandSender, x: Int, y: Int, z: Int) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: return
        val block = world.getBlockAt(x, y, z)
        val container = block.state as? org.bukkit.block.Container ?: run {
            sender.sendMessage("[AxMinions-Debug] Bloco em ($x, $y, $z) não é container! Tipo: ${block.type}")
            return
        }

        val contents = container.inventory.contents.filterNotNull().filter { !it.type.isAir }
        sender.sendMessage("[AxMinions-Debug] Baú em ($x, $y, $z) - Slots ocupados: ${contents.size}")
        contents.forEach {
            sender.sendMessage("  -> ${it.type} x${it.amount}")
        }
    }

    /**
     * Spawna mob fraco para teste do Slayer.
     * Uso: /axminions debug spawnmob <x> <y> <z> [type] [health]
     */
    @Subcommand("debug spawnmob")
    @CommandPermission("axminions.*")
    @Description("Debug: Spawna mob de teste com HP reduzido")
    fun debugSpawnMob(
        sender: CommandSender,
        x: Int, y: Int, z: Int,
        @Default("ZOMBIE") typeName: String,
        @Default("1") health: Double
    ) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: return
        val entityType = try { EntityType.valueOf(typeName.uppercase()) } catch (_: Exception) { EntityType.ZOMBIE }
        val loc = Location(world, x + 0.5, y.toDouble(), z + 0.5)
        val entity = world.spawnEntity(loc, entityType) as? org.bukkit.entity.LivingEntity ?: return
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.baseValue = health
        entity.health = health
        sender.sendMessage("[AxMinions-Debug] ✅ $entityType spawnado em ($x, $y, $z) com HP=$health!")
    }

    /**
     * Desvincula minion do baú.
     * Uso: /axminions debug unlink <x> <y> <z>
     */
    @Subcommand("debug unlink")
    @CommandPermission("axminions.*")
    @Description("Debug: Remove vínculo de baú do minion")
    fun debugUnlink(sender: CommandSender, minionX: Int, minionY: Int, minionZ: Int) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: return
        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        val target = allMinions.firstOrNull { minion ->
            val loc = minion.getLocation()
            loc.world?.uid == world.uid &&
            Math.abs(loc.blockX - minionX) <= 1 &&
            Math.abs(loc.blockY - minionY) <= 1 &&
            Math.abs(loc.blockZ - minionZ) <= 1
        }

        if (target == null) {
            sender.sendMessage("[AxMinions-Debug] Nenhum minion em ($minionX, $minionY, $minionZ)")
            return
        }

        target.setLinkedChest(null)
        sender.sendMessage("[AxMinions-Debug] ✅ Baú desvinculado do ${target.getType().getName()} em ($minionX, $minionY, $minionZ).")
    }

    /**
     * Remove / quebra o minion.
     * Uso: /axminions debug break <x> <y> <z>
     */
    @Subcommand("debug break")
    @CommandPermission("axminions.*")
    @Description("Debug: Remove minion")
    fun debugBreak(sender: CommandSender, minionX: Int, minionY: Int, minionZ: Int) {
        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: return
        val allMinions = AxMinionsAPI.INSTANCE.getMinions()
        val target = allMinions.firstOrNull { minion ->
            val loc = minion.getLocation()
            loc.world?.uid == world.uid &&
            Math.abs(loc.blockX - minionX) <= 1 &&
            Math.abs(loc.blockY - minionY) <= 1 &&
            Math.abs(loc.blockZ - minionZ) <= 1
        }

        if (target == null) {
            sender.sendMessage("[AxMinions-Debug] Nenhum minion em ($minionX, $minionY, $minionZ)")
            return
        }

        target.remove()
        sender.sendMessage("[AxMinions-Debug] ✅ Minion em ($minionX, $minionY, $minionZ) removido com sucesso!")
    }

    /**
     * Spawna / posiciona um minion programaticamente em (x, y, z).
     * Uso: /axminions debug place <tipo> <x> <y> <z> [level] [owner]
     */
    @Subcommand("debug place")
    @CommandPermission("axminions.*")
    @Description("Debug: Spawna um minion nas coordenadas indicadas")
    fun debugPlace(
        sender: CommandSender,
        typeName: String,
        x: Int, y: Int, z: Int,
        @Default("1") level: Int,
        @Default("") ownerName: String
    ) {
        val minionType = com.artillexstudios.axminions.api.minions.miniontype.MinionTypes.valueOf(typeName) ?: run {
            sender.sendMessage("[AxMinions-Debug] Tipo inválido: $typeName. Válidos: ${com.artillexstudios.axminions.api.minions.miniontype.MinionTypes.getMinionTypes().keys.joinToString()}")
            return
        }

        val world = if (sender is Player) sender.world else Bukkit.getWorlds().firstOrNull() ?: run {
            sender.sendMessage("[AxMinions-Debug] Nenhum mundo encontrado!")
            return
        }

        val loc = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
        val chunk = loc.chunk
        if (!chunk.isLoaded) {
            chunk.load()
        }

        val player = if (ownerName.isNotBlank()) Bukkit.getPlayer(ownerName) else (sender as? Player)
        val ownerUuid = player?.uniqueId ?: (sender as? Player)?.uniqueId ?: java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")
        val offlinePlayer = Bukkit.getOfflinePlayer(ownerUuid)

        if (AxMinionsPlugin.dataHandler.isMinion(loc)) {
            sender.sendMessage("[AxMinions-Debug] Já existe um minion em ($x, $y, $z)!")
            return
        }

        val locationId = AxMinionsPlugin.dataHandler.getLocationID(loc)
        val minion = Minion(
            loc,
            ownerUuid,
            offlinePlayer,
            minionType,
            level,
            ItemStack(Material.AIR),
            null,
            com.artillexstudios.axminions.api.minions.Direction.NORTH,
            0L,
            0.0,
            locationId,
            0,
            0L
        )

        minion.setOwnerOnline(offlinePlayer.isOnline)
        AxMinionsPlugin.dataHandler.saveMinion(minion)
        Minions.startTicking(chunk)

        sender.sendMessage("[AxMinions-Debug] ✅ Minion ${minionType.getName()} spawnado com sucesso em ($x, $y, $z) L$level! ticking=${minion.isTicking()}")
    }
}