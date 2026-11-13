package com.artillexstudios.axminions.listeners

import com.artillexstudios.axminions.api.utils.fastFor
import com.artillexstudios.axminions.minions.Minion
import com.artillexstudios.axminions.minions.Minions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class ChunkListener : Listener {

    @EventHandler
    fun onChunkLoadEvent(event: ChunkLoadEvent) {
        Minions.startTicking(event.chunk)

        // Se o chunk do baú ou do próprio minion carregar, atualiza o inventário do minion
        Minions.getMinions().fastFor { minion ->
            val chest = minion.getLinkedChest()
            val minionLoc = minion.getLocation()

            val isChestChunk = chest != null && chest.world?.uid == event.world.uid &&
                    (chest.blockX shr 4) == event.chunk.x && (chest.blockZ shr 4) == event.chunk.z
            val isMinionChunk = minionLoc.world?.uid == event.world.uid &&
                    (minionLoc.blockX shr 4) == event.chunk.x && (minionLoc.blockZ shr 4) == event.chunk.z

            if (isChestChunk || isMinionChunk) {
                (minion as? Minion)?.updateLinkedInventory()
            }
        }
    }

    @EventHandler
    fun onChunkUnloadEvent(event: ChunkUnloadEvent) {
        Minions.stopTicking(event.chunk)

        // NOVO: Se o chunk do baú descarregar, limpa a referência para evitar bugs
        Minions.getMinions().fastFor { minion ->
            val chest = minion.getLinkedChest()
            if (chest != null && chest.world?.uid == event.world.uid && chest.blockX shr 4 == event.chunk.x && chest.blockZ shr 4 == event.chunk.z) {
                (minion as? Minion)?.clearLinkedInventory()
            }
        }
    }
}