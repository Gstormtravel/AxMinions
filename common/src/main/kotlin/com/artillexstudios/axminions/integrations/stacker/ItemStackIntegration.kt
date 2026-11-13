package com.artillexstudios.axminions.integrations.stacker

import com.artillexstudios.axminions.api.integrations.types.StackerIntegration
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ItemStackIntegration : StackerIntegration {
    
    // A chave exata descoberta no /data get
    private val extraKey = NamespacedKey.fromString("itemstacker:extra")!!

    override fun getStackSize(entity: LivingEntity): Long {
        return 1
    }

    override fun getStackSize(item: Item): Long {
        val vanillaAmount = item.itemStack.amount.toLong()
        val extraAmount = item.persistentDataContainer.getOrDefault(extraKey, PersistentDataType.LONG, 0L)
        return vanillaAmount + extraAmount
    }

    override fun setStackSize(item: Item, amount: Int) {
        if (amount <= 0) {
            item.remove()
            return
        }

        if (amount <= 64) {
            // Se for 64 ou menos, vai tudo no vanilla e limpa o extra
            item.itemStack.amount = amount
            item.persistentDataContainer.remove(extraKey)
        } else {
            // Se for maior que 64, o vanilla segura 64 e o resto vai pro extra
            item.itemStack.amount = 64
            item.persistentDataContainer.set(extraKey, PersistentDataType.LONG, (amount - 64).toLong())
        }
    }

    override fun dropItemAt(itemStack: ItemStack, amount: Int, location: Location) {
        var remaining = amount
        while (remaining > 0) {
            val drop = itemStack.clone()
            val dropAmount = remaining.coerceAtMost(drop.maxStackSize)
            drop.amount = dropAmount
            location.world?.dropItem(location, drop)
            remaining -= dropAmount
        }
    }

    override fun register() {}
}