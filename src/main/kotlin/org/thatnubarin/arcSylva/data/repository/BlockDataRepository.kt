package org.thatnubarin.arcSylva.data.repository

import org.bukkit.Chunk
import org.bukkit.block.Block

interface BlockDataRepository {
    fun markAsPlayerPlaced(block: Block)
    fun isPlayerPlaced(block: Block): Boolean
    fun removeTracking(block: Block)
    fun clearChunk(chunk: Chunk)
}
