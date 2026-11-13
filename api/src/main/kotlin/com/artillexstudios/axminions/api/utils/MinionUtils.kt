package com.artillexstudios.axminions.api.utils

import com.artillexstudios.axminions.api.config.Config
import java.util.LinkedList
import java.util.Queue
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Waterlogged

object MinionUtils {
    private val FACES =
        arrayOf(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

    @JvmStatic
    fun getPlant(block: Block): ArrayList<Block> {
        val blocks = arrayListOf<Block>()
        var loc = block.location.clone()

        for (i in block.y downTo -65) {
            loc.add(0.0, -1.0, 0.0)
            val locBlock = loc.block
            if (!(locBlock.type == block.type && locBlock.getRelative(BlockFace.DOWN) == block)) break

            blocks.add(locBlock)
        }

        loc = block.location.clone()
        for (i in block.y..328) {
            loc.add(0.0, 1.0, 0.0)
            val locBlock = loc.block
            if (locBlock.type != block.type) break

            blocks.add(locBlock)
        }

        return blocks
    }

    @JvmStatic
    fun isStoneGenerator(location: Location): Boolean {
        var lava = false
        var water = false

        val locBlock = location.block
        FACES.fastFor {
            val relative = locBlock.getRelative(it)
            val type = relative.type
            val state = relative.blockData
            if (!lava) {
                lava = type == Material.LAVA
            }

            if (!water) {
                water = type == Material.WATER || (state as? Waterlogged)?.isWaterlogged ?: return@fastFor
            }

            if (water && lava) {
                return true
            }
        }

        return false
    }

    @JvmStatic
    fun isWoodBlock(material: Material): Boolean {
        val name = material.name
        return name.endsWith("_WOOD") ||
            name.endsWith("_LOG") ||
            name.endsWith("_STEM") ||
            name.endsWith("_HYPHAE") ||
            name == "BAMBOO_BLOCK" ||
            name == "STRIPPED_BAMBOO_BLOCK"
    }

    @JvmStatic
    fun isLeafBlock(material: Material): Boolean {
        val name = material.name
        return name.endsWith("_LEAVES") ||
            name == "MANGROVE_ROOTS" ||
            name == "MUDDY_MANGROVE_ROOTS" ||
            name == "NETHER_WART_BLOCK" ||
            name == "WARPED_WART_BLOCK"
    }

    @JvmStatic
    fun getTree(startBlock: Block): Set<Block> {
        val max: Int = Config.MAX_BREAKS_PER_TICK()
        var count = 0
        val queue: Queue<Block> = LinkedList()
        val visited = HashSet<Block>()
        val tree = LinkedHashSet<Block>()

        if (!isWoodBlock(startBlock.type)) {
            return tree
        }

        queue.add(startBlock)
        visited.add(startBlock)

        while (queue.isNotEmpty()) {
            val block = queue.poll()
            if (isWoodBlock(block.type)) {
                if (count >= max) {
                    return tree
                }
                count++
                tree.add(block)

                for (face in FACES) {
                    val relative = block.getRelative(face)
                    if (visited.add(relative)) {
                        queue.add(relative)
                    }
                }
            }
        }

        return tree
    }

    @JvmStatic
    fun getTreeLeaves(treeBlocks: Set<Block>, maxLeaves: Int = 96): Set<Block> {
        if (treeBlocks.isEmpty() || maxLeaves <= 0) return emptySet()
        val queue: Queue<Block> = LinkedList()
        val visited = HashSet<Block>()
        val leaves = LinkedHashSet<Block>()

        for (wood in treeBlocks) {
            for (face in FACES) {
                val rel = wood.getRelative(face)
                if (visited.add(rel) && isLeafBlock(rel.type)) {
                    queue.add(rel)
                }
            }
        }

        while (queue.isNotEmpty() && leaves.size < maxLeaves) {
            val leaf = queue.poll()
            if (!isLeafBlock(leaf.type)) continue
            leaves.add(leaf)
            for (face in FACES) {
                val rel = leaf.getRelative(face)
                if (visited.add(rel) && isLeafBlock(rel.type)) {
                    queue.add(rel)
                }
            }
        }
        return leaves
    }
}