package com.artillexstudios.axminions.nms.v26_3

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.craftbukkit.block.CraftBlockState
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import java.util.UUID

class NMSHandler : com.artillexstudios.axminions.nms.NMSHandler {

    override fun attack(source: Minion, target: Entity) {
        DamageHandler.damage(source, target)
    }

    override fun generateRandomFishingLoot(minion: Minion, waterLocation: Location): List<ItemStack> {
        return LootHandler.generateFishingLoot(minion, waterLocation)
    }

    override fun isAnimal(entity: Entity): Boolean {
        val nms = (entity as CraftEntity).handle
        return nms.type.category == net.minecraft.world.entity.MobCategory.CREATURE
    }

    override fun getAnimalUUID(): UUID = DamageHandler.getUUID()

    override fun getMinion(): Minion? = DamageHandler.getMinion()

    override fun getExp(block: Block, itemStack: ItemStack): Int {
        return try {
            val craftBlock = block as CraftBlock
            val state = craftBlock.state as CraftBlockState
            val nmsBlock = state.handle.block
            val serverLevel = (block.world as CraftWorld).handle
            nmsBlock.getExpDrop(
                state.handle,
                serverLevel,
                net.minecraft.core.BlockPos(block.x, block.y, block.z),
                CraftItemStack.asNMSCopy(itemStack),
                true
            )
        } catch (e: Exception) { 0 }
    }

    override fun getDrops(block: Block, itemStack: ItemStack): List<ItemStack> {
        return try {
            val craftBlock = block as CraftBlock
            val state = craftBlock.state as CraftBlockState
            val serverLevel = (block.world as CraftWorld).handle
            val nmsPos = net.minecraft.core.BlockPos(block.x, block.y, block.z)
            val nmsTool = CraftItemStack.asNMSCopy(itemStack)
            val nmsDrops = net.minecraft.world.level.block.Block.getDrops(
                state.handle, serverLevel, nmsPos, null, null, nmsTool
            )
            nmsDrops.map { CraftItemStack.asBukkitCopy(it) }
        } catch (e: Exception) {
            block.getDrops(itemStack).toList()
        }
    }

    override fun openMinionDialog(player: org.bukkit.entity.Player, spec: com.artillexstudios.axminions.api.menus.MinionDialogSpec): Boolean {
        return DialogMenuHandler.openDialog(player, spec)
    }
}