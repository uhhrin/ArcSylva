package org.thatnubarin.arcSylva.data.repository

import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

/**
 * Chunk PDC-backed repository to track player-placed blocks.
 */
class PDCBlockRepository(private val plugin: JavaPlugin) : BlockDataRepository {
    private val key = NamespacedKey(plugin, "placed_blocks")

    override fun markAsPlayerPlaced(block: Block) {
        val container = block.chunk.persistentDataContainer
        val set = loadSet(container)
        set.add(encode(block))
        saveSet(container, set)
    }

    override fun isPlayerPlaced(block: Block): Boolean {
        val container = block.chunk.persistentDataContainer
        val set = loadSet(container)
        return set.contains(encode(block))
    }

    override fun removeTracking(block: Block) {
        val container = block.chunk.persistentDataContainer
        val set = loadSet(container)
        if (set.remove(encode(block))) {
            saveSet(container, set)
        }
    }

    override fun clearChunk(chunk: Chunk) {
        chunk.persistentDataContainer.remove(key)
    }

    private fun loadSet(container: PersistentDataContainer): MutableSet<Int> {
        val encoded = container.get(key, PersistentDataType.INTEGER_ARRAY) ?: return mutableSetOf()
        return encoded.toMutableSet()
    }

    private fun saveSet(container: PersistentDataContainer, set: Set<Int>) {
        container.set(key, PersistentDataType.INTEGER_ARRAY, set.toIntArray())
    }

    private fun encode(block: Block): Int {
        val chunkOriginX = block.chunk.x shl 4
        val chunkOriginZ = block.chunk.z shl 4
        val relX = (block.x - chunkOriginX) and 0xF       // 4 bits
        val relZ = (block.z - chunkOriginZ) and 0xF       // 4 bits
        val y = (block.y + 64) and 0x1FF                  // 9 bits (0-511) covers -64..447, we use -64..319
        return (relX shl 13) or (y shl 4) or relZ         // pack into 32-bit int
    }
}
