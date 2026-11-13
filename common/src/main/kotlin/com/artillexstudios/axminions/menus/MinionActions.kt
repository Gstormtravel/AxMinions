package com.artillexstudios.axminions.menus

import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Direction
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.minions.miniontype.MinionTypes
import com.artillexstudios.axminions.listeners.LinkingListener
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.Locale

object MinionActions {

    fun rotate(minion: Minion) {
        when (minion.getDirection()) {
            Direction.NORTH -> minion.setDirection(Direction.WEST)
            Direction.EAST -> minion.setDirection(Direction.NORTH)
            Direction.SOUTH -> minion.setDirection(Direction.EAST)
            Direction.WEST -> minion.setDirection(Direction.SOUTH)
        }
    }

    fun toggleLink(minion: Minion, player: Player, closeGuiOnStart: Boolean = true): Boolean {
        if (minion.getLinkedChest() != null) {
            minion.setLinkedChest(null)
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_UNLINK()))
            com.artillexstudios.axminions.logging.MinionLogger.log(
                minion,
                "UNLINK_CHEST",
                "Baú desvinculado por ${player.name}."
            )
            return false
        }

        player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.LINK_START()))
        LinkingListener.linking[player] = minion
        if (closeGuiOnStart) {
            player.closeInventory()
        }
        return true
    }

    fun upgrade(minion: Minion, player: Player): Boolean {
        val money = minion.getType().getDouble("requirements.money", minion.getLevel() + 1)
        val actions = minion.getType().getDouble("requirements.actions", minion.getLevel() + 1)

        if (minion.getType().hasReachedMaxLevel(minion)) {
            return false
        }

        if (minion.getActionAmount() < actions) {
            sendUpgradeFail(player)
            return false
        }

        AxMinionsPlugin.integrations.getEconomyIntegration()?.let {
            if (it.getBalance(player) < money) {
                sendUpgradeFail(player)
                return false
            }

            it.takeBalance(player, money)
        }

        if (Config.UPGRADE_SOUND().isNotBlank()) {
            try {
                player.playSound(
                    player,
                    Sound.valueOf(Config.UPGRADE_SOUND().uppercase(Locale.ENGLISH)),
                    1.0f,
                    1.0f
                )
            } catch (_: Exception) {
            }
        }

        val nextLevel = minion.getLevel() + 1
        minion.setLevel(nextLevel)
        com.artillexstudios.axminions.logging.MinionLogger.log(
            minion,
            "UPGRADE",
            "Evoluído para Nível $nextLevel por ${player.name} (custo: $${money.toLong()})."
        )
        return true
    }

    fun collectStorage(minion: Minion, player: Player): Boolean {
        val stored = minion.getStorage()
        if (stored <= 0.0) return false

        if (minion.getType() == MinionTypes.getMinionTypes()["seller"]) {
            AxMinionsPlugin.integrations.getEconomyIntegration()?.let { eco ->
                val owner = minion.getOwner() ?: return false
                eco.giveBalance(owner, stored)
                minion.setStorage(0.0)
            }
        } else {
            player.giveExp(stored.toInt())
            minion.setStorage(0.0)
        }

        AxMinionsPlugin.dataQueue.submit {
            AxMinionsPlugin.dataHandler.saveMinion(minion)
        }
        return true
    }

    fun sendUpgradeFail(player: Player) {
        when (Config.UPGRADE_FAIL()) {
            "title" -> {
                player.closeInventory()
                player.sendTitle(StringUtils.formatToString(Messages.UPGRADE_FAIL()), "", 10, 70, 20)
            }

            "subtitle" -> {
                player.closeInventory()
                player.sendTitle("", StringUtils.formatToString(Messages.UPGRADE_FAIL()), 10, 70, 20)
            }

            "actionbar" -> {
                player.closeInventory()
                player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    *TextComponent.fromLegacyText(StringUtils.formatToString(Messages.UPGRADE_FAIL()))
                )
            }

            else -> {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX() + Messages.UPGRADE_FAIL()))
            }
        }
    }
}
