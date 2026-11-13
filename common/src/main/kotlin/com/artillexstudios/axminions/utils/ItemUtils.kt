package com.artillexstudios.axminions.utils

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.utils.Keys
import com.artillexstudios.axminions.listeners.MinionDamageListener
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.Repairable
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Method
import java.util.Optional

object ItemUtils {
    private val warnedExecutableIds = mutableSetOf<String>()
    private var eiInitAttempted = false
    private var eiGetManagerMethod: Method? = null
    private var eiGetItemMethod: Method? = null
    private var eiBuildItemMethod: Method? = null

    /**
     * Removes AxMinions internal PDC/custom_data tags so vanilla drops collected by
     * Slayer/Collector retain `hasItemMeta() == false` and work with shop/sell plugins (e.g. HeroVender).
     */
    fun stripAxMinionsNbt(stack: ItemStack): ItemStack {
        if (stack.type.isAir || !stack.hasItemMeta()) return stack
        val meta = stack.itemMeta ?: return stack

        val hadSlayer = meta.persistentDataContainer.has(
            MinionDamageListener.SLAYER_DROP_KEY,
            org.bukkit.persistence.PersistentDataType.STRING
        )
        val hadGui = meta.persistentDataContainer.has(
            Keys.GUI,
            org.bukkit.persistence.PersistentDataType.STRING
        )

        if (hadSlayer) {
            meta.persistentDataContainer.remove(MinionDamageListener.SLAYER_DROP_KEY)
        }
        if (hadGui) {
            meta.persistentDataContainer.remove(Keys.GUI)
        }

        if (isEffectivelyEmptyMeta(stack, meta)) {
            return ItemStack(stack.type, stack.amount)
        }

        val clone = stack.clone()
        clone.itemMeta = meta
        return clone
    }

    private fun isEffectivelyEmptyMeta(stack: ItemStack, meta: ItemMeta): Boolean {
        if (!meta.persistentDataContainer.isEmpty) return false
        if (meta.hasDisplayName() || meta.hasLore() || meta.hasEnchants() ||
            meta.hasCustomModelData() || meta.itemFlags.isNotEmpty() || meta.hasAttributeModifiers()
        ) {
            return false
        }
        if (meta is Damageable && meta.hasDamage()) return false
        if (meta is EnchantmentStorageMeta && meta.hasStoredEnchants()) return false
        if (meta is Repairable && meta.hasRepairCost()) return false
        if (meta is LeatherArmorMeta && meta.color != Bukkit.getItemFactory().defaultLeatherColor) return false
        if (meta is ArmorMeta && meta.hasTrim()) return false
        if (meta is BannerMeta && meta.numberOfPatterns() > 0) return false
        if (meta is BookMeta && (meta.hasTitle() || meta.hasAuthor() || meta.hasPages())) return false
        if (meta is FireworkMeta && meta.effectsSize > 0) return false
        if (meta is FireworkEffectMeta && meta.hasEffect()) return false
        if (meta is PotionMeta && (meta.basePotionType != null || meta.hasCustomEffects() || meta.hasColor())) return false
        if (meta is SkullMeta && meta.hasOwner()) return false

        val serialized = try {
            meta.serialize()
        } catch (_: Throwable) {
            emptyMap()
        }
        for ((k, v) in serialized) {
            if (k == "==" || k == "v") continue
            if (k == "PublicBukkitValues" && (v as? Map<*, *>)?.isEmpty() == true) continue
            return false
        }
        return true
    }

    @Synchronized
    private fun initExecutableItemsReflection() {
        if (eiInitAttempted) return
        eiInitAttempted = true
        try {
            val apiClass = Class.forName("com.ssomar.score.api.executableitems.ExecutableItemsAPI")
            val managerInterface = Class.forName("com.ssomar.score.api.executableitems.config.ExecutableItemsManagerInterface")
            val itemInterface = Class.forName("com.ssomar.score.api.executableitems.config.ExecutableItemInterface")
            eiGetManagerMethod = apiClass.getMethod("getExecutableItemsManager")
            eiGetItemMethod = managerInterface.getMethod("getExecutableItem", String::class.java)
            eiBuildItemMethod = itemInterface.getMethod("buildItem", Int::class.javaPrimitiveType, Optional::class.java)
        } catch (t: Throwable) {
            AxMinionsPlugin.INSTANCE.logger.warning("[AxMinions] ExecutableItems API unavailable: ${t.message}")
        }
    }

    fun buildExecutableItem(id: String, amount: Int, owner: Player?): ItemStack? {
        return try {
            initExecutableItemsReflection()
            val getManager = eiGetManagerMethod ?: return null
            val getItem = eiGetItemMethod ?: return null
            val buildItem = eiBuildItemMethod ?: return null

            val manager = getManager.invoke(null) ?: return null
            val optItem = getItem.invoke(manager, id) as? Optional<*> ?: return null
            if (optItem.isEmpty) {
                if (warnedExecutableIds.add(id)) {
                    AxMinionsPlugin.INSTANCE.logger.warning("[AxMinions] ExecutableItem '$id' not found!")
                }
                return null
            }
            buildItem.invoke(optItem.get(), amount.coerceAtLeast(1), Optional.ofNullable(owner)) as? ItemStack
        } catch (t: Throwable) {
            if (warnedExecutableIds.add("err:$id")) {
                AxMinionsPlugin.INSTANCE.logger.warning("[AxMinions] Failed to build ExecutableItem '$id': ${t.message}")
            }
            null
        }
    }
}
