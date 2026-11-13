package com.artillexstudios.axminions.listeners

import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.events.MinionToolEvent
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.listeners.LinkingListener
import com.artillexstudios.axminions.logging.MinionLogger
import com.artillexstudios.axminions.menus.MenuRouter
import com.artillexstudios.axminions.menus.MinionActions
import com.artillexstudios.axminions.menus.MinionSkillsGui
import com.artillexstudios.axminions.api.utils.CoolDown
import com.artillexstudios.axminions.api.utils.Keys
import com.artillexstudios.axminions.api.utils.fastFor
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MinionInventoryListener : Listener {
    private val coolDown = CoolDown<Player>()

    @EventHandler
    fun onInventoryDragEvent(event: InventoryDragEvent) {
        val holder = event.inventory.holder
        if (holder is Minion || holder is MinionSkillsGui.SkillsGuiHolder || holder is MinionSkillsGui.ToolSwapHolder || holder is MinionSkillsGui.PickupGuiHolder || holder is MinionSkillsGui.AdminMinionsHolder) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val holder = event.inventory.holder
        if (holder is MinionSkillsGui.SkillsGuiHolder) {
            handleSkillsClick(event, holder.minion)
            return
        }
        if (holder is MinionSkillsGui.ToolSwapHolder) {
            handleToolSwapClick(event, holder.minion)
            return
        }
        if (holder is MinionSkillsGui.PickupGuiHolder) {
            handlePickupClick(event, holder.minion)
            return
        }
        if (holder is MinionSkillsGui.AdminMinionsHolder) {
            handleAdminClick(event, holder)
            return
        }
        val minion = holder as? Minion ?: return
        if (event.clickedInventory == null) return
        event.isCancelled = true
        val player = event.whoClicked as Player

        if (coolDown.contains(player)) {
            return
        }

        coolDown.add(player, 250)

        if (event.clickedInventory == player.inventory && event.currentItem != null) {
            if (equipToolFromPlayer(minion, player, event.currentItem!!, event.clickedInventory!!)) {
                minion.updateInventories()
            }
            return
        }

        if (event.slot == AxMinionsAPI.INSTANCE.getConfig().get("gui.items.item.slot")) {
            if (unequipToolToPlayer(minion, player)) {
                minion.updateInventories()
            }
            return
        }

        if (!(event.clickedInventory?.getItem(event.slot)?.hasItemMeta() ?: return)) {
            return
        }

        val meta = event.clickedInventory?.getItem(event.slot)?.itemMeta ?: return
        if (!meta.persistentDataContainer.has(Keys.GUI, PersistentDataType.STRING)) return
        val type = meta.persistentDataContainer.get(Keys.GUI, PersistentDataType.STRING)

        when (type) {
            "rotate" -> MinionActions.rotate(minion)

            "link" -> MinionActions.toggleLink(minion, player, closeGuiOnStart = true)

            "upgrade" -> MinionActions.upgrade(minion, player)

            "statistics" -> MinionActions.collectStorage(minion, player)

            "abilities" -> {
                minion.openSkillsInventory(player)
                return
            }

            "pickup" -> {
                MinionSkillsGui.openPickupGui(minion, player)
                return
            }

            "menu_switch" -> {
                MenuRouter.setOrToggleMenuMode(player, "DIALOG", minion)
                return
            }

            "charge" -> {
                if (event.isShiftClick) {
                    val itemCache = mutableListOf<Pair<ItemStack, String>>()
                    val section = Config.CHARGE_ITEMS()

                    for (key in section.keys) {
                        itemCache.add(Pair(ItemBuilder.create(section.getSection(key.toString())).get(), key.toString()))
                    }

                    while (true) {
                        val chargeSeconds = (minion.getCharge() - System.currentTimeMillis()) / 1000

                        if ((Config.MAX_CHARGE() * 60) - chargeSeconds < Config.MINIMUM_CHARGE()) {
                            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE_NOT_ENOUGH_TIME_PASSED()))
                            return
                        }

                        var chargeAmount = Config.CHARGE_AMOUNT()
                        var itemCharge = false

                        for (pair in itemCache) {
                            if (player.inventory.containsAtLeast(pair.first, 1)) {
                                itemCharge = true
                                chargeAmount = section.getSection(pair.second).getInt("charge")
                                player.inventory.removeItem(pair.first)
                                break
                            }
                        }

                        if (Config.CHARGE_PRICE() <= 0 && !itemCharge) {
                            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE_FAIL()))
                            return
                        }

                        if (!itemCharge) {
                            if ((AxMinionsPlugin.integrations.getEconomyIntegration()?.getBalance(player)
                                    ?: return) < Config.CHARGE_PRICE()
                            ) {
                                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE_FAIL()))
                                return
                            }

                            AxMinionsPlugin.integrations.getEconomyIntegration()?.let {
                                minion.getOwner()?.let { player ->
                                    it.takeBalance(player, Config.CHARGE_PRICE())
                                }
                            }
                        }

                        if (chargeSeconds + chargeAmount > Config.MAX_CHARGE() * 60L) {
                            minion.setCharge(System.currentTimeMillis() + Config.MAX_CHARGE() * 60L * 1000L)
                            return
                        }

                        if (minion.getCharge() < System.currentTimeMillis()) {
                            minion.setCharge(System.currentTimeMillis() + chargeAmount * 1000)
                        } else {
                            minion.setCharge(minion.getCharge() + chargeAmount * 1000)
                        }

                        if (Messages.CHARGE().isNotBlank()) {
                            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE()))
                        }
                    }
                } else {
                    val chargeSeconds = (minion.getCharge() - System.currentTimeMillis()) / 1000

                    if ((Config.MAX_CHARGE() * 60) - chargeSeconds < Config.MINIMUM_CHARGE()) {
                        player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE_NOT_ENOUGH_TIME_PASSED()))
                        return
                    }

                    var chargeAmount = Config.CHARGE_AMOUNT()
                    var itemCharge = false
                    val section = Config.CHARGE_ITEMS()

                    for (key in section.keys) {
                        val item = ItemBuilder.create(section.getSection(key.toString())).get()
                        if (player.inventory.containsAtLeast(item, 1)) {
                            itemCharge = true
                            chargeAmount = section.getSection(key.toString()).getInt("charge")
                            player.inventory.removeItem(item)
                            break
                        }
                    }

                    if (Config.CHARGE_PRICE() <= 0 && !itemCharge) {
                        player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE_FAIL()))
                        return
                    }

                    if (!itemCharge) {
                        if ((AxMinionsPlugin.integrations.getEconomyIntegration()?.getBalance(player)
                                ?: return) < Config.CHARGE_PRICE()
                        ) {
                            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE_FAIL()))
                            return
                        }

                        AxMinionsPlugin.integrations.getEconomyIntegration()?.let {
                            minion.getOwner()?.let { player ->
                                it.takeBalance(player, Config.CHARGE_PRICE())
                            }
                        }
                    }

                    if (chargeSeconds + chargeAmount > Config.MAX_CHARGE() * 60L) {
                        minion.setCharge(System.currentTimeMillis() + Config.MAX_CHARGE() * 60L * 1000L)
                        return
                    }

                    if (minion.getCharge() < System.currentTimeMillis()) {
                        minion.setCharge(System.currentTimeMillis() + chargeAmount * 1000)
                    } else {
                        minion.setCharge(minion.getCharge() + chargeAmount * 1000)
                    }

                    if (Messages.CHARGE().isNotBlank()) {
                        player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.CHARGE()))
                    }
                }
            }
        }

        minion.updateInventories()
    }

    @EventHandler
    fun onInventoryCloseEvent(event: InventoryCloseEvent) {
        val holder = event.inventory.holder as? Minion ?: return

        holder.removeOpenInventory(event.inventory)
    }

    private fun handleSkillsClick(event: InventoryClickEvent, minion: Minion) {
        if (event.clickedInventory == null) return
        event.isCancelled = true
        val player = event.whoClicked as Player
        if (event.clickedInventory == player.inventory) return

        if (coolDown.contains(player)) return
        coolDown.add(player, 200)

        val clicked = event.clickedInventory?.getItem(event.slot) ?: return
        val tag = clicked.itemMeta?.persistentDataContainer?.get(Keys.GUI, PersistentDataType.STRING) ?: return

        when {
            tag == "back_main" -> minion.openChestInventory(player)
            tag == "respec_skills" -> {
                if (minion.respecSkills(player)) {
                    minion.openSkillsInventory(player)
                }
            }
            tag == "menu_switch" -> MenuRouter.setOrToggleMenuMode(player, "DIALOG", minion)
            tag.startsWith("skill:") -> {
                val skillId = tag.removePrefix("skill:")
                if (minion.upgradeSkill(skillId, player)) {
                    minion.openSkillsInventory(player)
                }
            }
        }
    }

    private fun handleToolSwapClick(event: InventoryClickEvent, minion: Minion) {
        if (event.clickedInventory == null) return
        event.isCancelled = true
        val player = event.whoClicked as Player

        if (coolDown.contains(player)) return
        coolDown.add(player, 200)

        if (event.clickedInventory == player.inventory && event.currentItem != null) {
            if (equipToolFromPlayer(minion, player, event.currentItem!!, event.clickedInventory!!)) {
                minion.openToolSwapInventory(player)
            }
            return
        }

        if (event.slot == 13) {
            if (unequipToolToPlayer(minion, player)) {
                minion.openToolSwapInventory(player)
            }
            return
        }

        val tag = event.clickedInventory?.getItem(event.slot)?.itemMeta?.persistentDataContainer?.get(Keys.GUI, PersistentDataType.STRING)
        if (tag == "back_main") {
            MenuRouter.openMinionMenu(minion, player)
        }
    }

    private fun handlePickupClick(event: InventoryClickEvent, minion: Minion) {
        if (event.clickedInventory == null) return
        event.isCancelled = true
        val player = event.whoClicked as Player
        if (event.clickedInventory == player.inventory) return

        if (coolDown.contains(player)) return
        coolDown.add(player, 200)

        val tag = event.clickedInventory?.getItem(event.slot)?.itemMeta?.persistentDataContainer?.get(Keys.GUI, PersistentDataType.STRING) ?: return
        when (tag) {
            "back_main" -> minion.openChestInventory(player)
            "pickup_with_tool" -> {
                player.closeInventory()
                minion.pickupByPlayer(player, true)
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.PICKUP_WITH_TOOL()))
            }
            "pickup_without_tool" -> {
                player.closeInventory()
                minion.pickupByPlayer(player, false)
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.PICKUP_WITHOUT_TOOL()))
            }
        }
    }

    private fun equipToolFromPlayer(
        minion: Minion,
        player: Player,
        clickedItem: ItemStack,
        playerInv: org.bukkit.inventory.Inventory
    ): Boolean {
        if (clickedItem.type.isAir) return false
        val allowedTools = arrayListOf<Material>()
        run breaking@{
            minion.getType().getConfig().getStringList("tool.material").fastFor {
                if (it.equals("*")) {
                    allowedTools.addAll(Material.entries)
                    return@breaking
                }
                allowedTools.add(Material.matchMaterial(it) ?: return@fastFor)
            }
        }

        if (clickedItem.type !in allowedTools) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.WRONG_TOOL()))
            return false
        }

        val minionToolEvent = MinionToolEvent(minion, player, clickedItem.clone(), minion.getTool()!!.clone())
        Bukkit.getPluginManager().callEvent(minionToolEvent)
        if (minionToolEvent.isCancelled) return false

        if (minion.getTool()?.type != Material.AIR) {
            val current = minionToolEvent.newTool
            val oldTool = minionToolEvent.oldTool
            minion.setTool(current)
            minion.updateArmour()
            clickedItem.amount = 0
            playerInv.addItem(oldTool)
            MinionLogger.log(minion, "TOOL_SWAP", "${player.name} trocou ${oldTool.type.name} por ${current.type.name}")
        } else {
            minion.setTool(minionToolEvent.newTool)
            clickedItem.amount = 0
            MinionLogger.log(minion, "TOOL_EQUIP", "${player.name} equipou ${minionToolEvent.newTool.type.name}")
        }
        return true
    }

    private fun handleAdminClick(event: InventoryClickEvent, holder: MinionSkillsGui.AdminMinionsHolder) {
        if (event.clickedInventory == null) return
        event.isCancelled = true
        val player = event.whoClicked as Player
        if (event.clickedInventory == player.inventory) return

        if (coolDown.contains(player)) return
        coolDown.add(player, 200)

        val clicked = event.clickedInventory?.getItem(event.slot) ?: return
        val tag = clicked.itemMeta?.persistentDataContainer?.get(Keys.GUI, PersistentDataType.STRING) ?: return

        when {
            tag == "admin_close" -> player.closeInventory()
            tag == "admin_logs" -> {
                player.closeInventory()
                val recent = MinionLogger.getRecentLogs(10)
                player.sendMessage(StringUtils.formatToString("<#00CCFF><b>[AxMinions Logs]</b> <gray>Ultimos ${recent.size} eventos:"))
                for (line in recent) {
                    player.sendMessage("§7$line")
                }
            }
            tag.startsWith("admin_page:") -> {
                val targetPage = tag.removePrefix("admin_page:").toIntOrNull() ?: 0
                MinionSkillsGui.openAdminGui(player, targetPage, holder.filterOwner)
            }
            tag.startsWith("admin_minion:") -> {
                val parts = tag.removePrefix("admin_minion:").split(":")
                if (parts.size != 4) return
                val worldName = parts[0]
                val x = parts[1].toIntOrNull() ?: return
                val y = parts[2].toIntOrNull() ?: return
                val z = parts[3].toIntOrNull() ?: return

                val target = AxMinionsAPI.INSTANCE.getMinions().firstOrNull { m ->
                    val l = m.getLocation()
                    l.world?.name == worldName && l.blockX == x && l.blockY == y && l.blockZ == z
                } ?: run {
                    player.sendMessage("§cMinion nao encontrado em $worldName ($x, $y, $z).")
                    MinionSkillsGui.openAdminGui(player, holder.page, holder.filterOwner)
                    return
                }

                if (event.isShiftClick && event.isRightClick) {
                    MinionLogger.log(target, "ADMIN_REMOVE", "Admin ${player.name} removeu o minion pelo painel.")
                    target.remove()
                    player.sendMessage("§aMinion removido em $worldName ($x, $y, $z).")
                    MinionSkillsGui.openAdminGui(player, holder.page, holder.filterOwner)
                } else {
                    player.closeInventory()
                    player.teleport(target.getLocation().clone().add(0.5, 1.0, 0.5))
                    player.sendMessage("§aTeleportado para ${target.getType().getName()} em $worldName ($x, $y, $z).")
                }
            }
        }
    }

    private fun unequipToolToPlayer(minion: Minion, player: Player): Boolean {
        if (minion.getTool()?.type == Material.AIR) return false
        if (player.inventory.firstEmpty() == -1) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.ERROR_INVENTORY_FULL()))
            return false
        }
        val tool = minion.getTool()?.clone() ?: return false
        val minionToolEvent = MinionToolEvent(minion, player, ItemStack(Material.AIR), tool)
        Bukkit.getPluginManager().callEvent(minionToolEvent)
        if (minionToolEvent.isCancelled) return false

        minion.setTool(ItemStack(Material.AIR))
        minion.updateArmour()
        val toolMeta = minionToolEvent.oldTool.itemMeta ?: return false
        toolMeta.persistentDataContainer.remove(Keys.GUI)
        minionToolEvent.oldTool.setItemMeta(toolMeta)
        player.inventory.addItem(minionToolEvent.oldTool)
        MinionLogger.log(minion, "TOOL_UNEQUIP", "${player.name} retirou ${tool.type.name}")
        return true
    }
}