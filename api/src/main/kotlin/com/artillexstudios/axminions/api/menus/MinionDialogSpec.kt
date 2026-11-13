package com.artillexstudios.axminions.api.menus

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class DialogButtonSpec(
    val label: String,
    val tooltip: String? = null,
    val width: Int = 150,
    val closeAfterClick: Boolean = false,
    val onClick: (Player) -> Unit
)

data class MinionDialogSpec(
    val title: String,
    val bodyLines: List<String>,
    val iconItem: ItemStack? = null,
    val columns: Int = 2,
    val actions: List<DialogButtonSpec>,
    val exitButton: DialogButtonSpec? = null
)
