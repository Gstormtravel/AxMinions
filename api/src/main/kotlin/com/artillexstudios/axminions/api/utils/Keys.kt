package com.artillexstudios.axminions.api.utils

import com.artillexstudios.axminions.api.AxMinionsAPI
import org.bukkit.NamespacedKey

object Keys {
    @JvmField
    val MINION_TYPE = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "minion_type")
    @JvmField
    val LEVEL = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "level")
    @JvmField
    val GUI = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "gui_item")
    @JvmField
    val STATISTICS = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "statistics")
    @JvmField
    val PLACED = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "placed")
    @JvmField
    val CHARGE = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "charge")
    @JvmField
    val SKILLS = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "skills")
    @JvmField
    val TOOL = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "tool")
    @JvmField
    val FIRST_OWNER = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "first_owner")
    @JvmField
    val OWNER_HISTORY = NamespacedKey(AxMinionsAPI.INSTANCE.getAxMinionsInstance(), "owner_history")
}