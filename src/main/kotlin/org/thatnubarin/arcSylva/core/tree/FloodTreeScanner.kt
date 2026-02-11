package org.thatnubarin.arcSylva.core.tree

import org.bukkit.Material
import org.bukkit.block.Block
import java.util.ArrayDeque

/**
 * Flood-fill scanner that collects all connected log blocks (26-directional) up to a max size.
 */
class FloodTreeScanner : TreeScanner {
    override fun scanConnectedLogs(root: Block, allowedLogs: Set<Material>, maxBlocks: Int, timeoutNanos: Long): List<Block> {
        if (!allowedLogs.contains(root.type) || maxBlocks <= 0) return emptyList()

        val visited = HashSet<Long>(maxBlocks)
        val queue: ArrayDeque<Block> = ArrayDeque()
        val world = root.world
        val deadline = System.nanoTime() + timeoutNanos
        val pollBudget = (maxBlocks * 26).coerceAtMost(8000)

        visited.add(encode(root))
        queue.add(root)

        val result = ArrayList<Block>(maxBlocks)
        var polls = 0
        while (queue.isNotEmpty() && result.size < maxBlocks && System.nanoTime() <= deadline && polls < pollBudget) {
            val current = queue.removeFirst()
            result.add(current)
            polls++

            // Explore 26-directional neighbors (3x3 cube minus center).
            for (offset in OFFSETS) {
                val nx = current.x + offset.first
                val ny = current.y + offset.second
                val nz = current.z + offset.third
                if (ny < world.minHeight || ny >= world.maxHeight) continue

                val neighbor = world.getBlockAt(nx, ny, nz)
                if (!allowedLogs.contains(neighbor.type)) continue

                if (visited.size >= maxBlocks) continue
                if (visited.add(encode(neighbor))) {
                    queue.add(neighbor)
                }
            }
        }
        return result
    }

    private companion object {
        private val OFFSETS = buildList {
            for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                add(Triple(dx, dy, dz))
            }
        }.toTypedArray()

        private fun encode(block: Block): Long {
            val x = block.x.toLong() and 0x3FFFFFF
            val y = (block.y.toLong() and 0xFFF)
            val z = block.z.toLong() and 0x3FFFFFF
            return (x shl 38) or (z shl 12) or y
        }
    }
}
