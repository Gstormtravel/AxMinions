package com.artillexstudios.axminions.nms.v26_3

import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.menus.DialogButtonSpec
import com.artillexstudios.axminions.api.menus.MinionDialogSpec
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.time.Duration

object DialogMenuHandler {
    private val miniMessage = MiniMessage.miniMessage()
    private val hexPattern = Regex("\u00a7x(?:\u00a7([0-9a-fA-F]))(?:\u00a7([0-9a-fA-F]))(?:\u00a7([0-9a-fA-F]))(?:\u00a7([0-9a-fA-F]))(?:\u00a7([0-9a-fA-F]))(?:\u00a7([0-9a-fA-F]))")
    private val legacyMap = mapOf(
        '0' to "<black>", '1' to "<dark_blue>", '2' to "<dark_green>", '3' to "<dark_aqua>",
        '4' to "<dark_red>", '5' to "<dark_purple>", '6' to "<gold>", '7' to "<gray>",
        '8' to "<dark_gray>", '9' to "<blue>", 'a' to "<green>", 'b' to "<aqua>",
        'c' to "<red>", 'd' to "<light_purple>", 'e' to "<yellow>", 'f' to "<white>",
        'k' to "<obfuscated>", 'l' to "<bold>", 'm' to "<strikethrough>",
        'n' to "<underlined>", 'o' to "<italic>", 'r' to "<reset>"
    )

    private val callbackOptions = ClickCallback.Options.builder()
        .uses(ClickCallback.UNLIMITED_USES)
        .lifetime(Duration.ofMinutes(15))
        .build()

    private fun toComponent(text: String): Component {
        var safe = text.replace("<", "\\<")
        safe = hexPattern.replace(safe) { m ->
            "<#${m.groupValues[1]}${m.groupValues[2]}${m.groupValues[3]}${m.groupValues[4]}${m.groupValues[5]}${m.groupValues[6]}>"
        }
        val sb = StringBuilder(safe.length)
        var i = 0
        while (i < safe.length) {
            if (safe[i] == '\u00a7' && i + 1 < safe.length) {
                val tag = legacyMap[safe[i + 1].lowercaseChar()]
                if (tag != null) {
                    sb.append(tag)
                    i += 2
                    continue
                }
            }
            sb.append(safe[i])
            i++
        }
        return miniMessage.deserialize(sb.toString())
    }

    fun openDialog(player: Player, spec: MinionDialogSpec): Boolean {
        val body = arrayListOf<DialogBody>()
        val icon = spec.iconItem
        if (icon != null && !icon.type.isAir) {
            body.add(DialogBody.item(icon, null, false, true, 16, 16))
        }
        for (line in spec.bodyLines) {
            body.add(DialogBody.plainMessage(toComponent(line), 320))
        }

        val actionButtons = spec.actions.map { toActionButton(it) }
        val exitButton = spec.exitButton?.let { toActionButton(it) }

        val dialog = Dialog.create { factory ->
            factory.empty()
                .base(
                    DialogBase.create(
                        toComponent(spec.title),
                        null,
                        true,
                        false,
                        DialogBase.DialogAfterAction.NONE,
                        body,
                        emptyList()
                    )
                )
                .type(DialogType.multiAction(actionButtons, exitButton, spec.columns.coerceIn(1, 4)))
        }

        player.showDialog(dialog)
        return true
    }

    private fun toActionButton(button: DialogButtonSpec): ActionButton {
        val action = DialogAction.customClick({ _, audience ->
            val targetPlayer = audience as? Player ?: return@customClick
            val plugin = AxMinionsAPI.INSTANCE.getAxMinionsInstance()
            targetPlayer.scheduler.run(plugin, { _ ->
                if (button.closeAfterClick) {
                    targetPlayer.closeDialog()
                }
                button.onClick(targetPlayer)
            }, null)
        }, callbackOptions)

        return ActionButton.create(
            toComponent(button.label),
            button.tooltip?.let { toComponent(it) },
            button.width,
            action
        )
    }
}
