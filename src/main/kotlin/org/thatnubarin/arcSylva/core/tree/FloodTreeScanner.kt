package org.thatnubarin.arcSylva.core.tree

import org.bukkit.Material
import org.bukkit.block.Block
import java.util.ArrayDeque

/**
 * Flood-fill scanner that collects all connected log blocks (26-directional) up to a max size.
 */
class FloodTreeScanner : TreeScanner {
    override fun scanConnectedLogs(root: Block, allowedLogs: Set<Material>, maxBlocks: Int): List<Block> {
        if (!allowedLogs.contains(root.type) || maxBlocks <= 0) return emptyList()

        val visited = HashSet<Block>(maxBlocks)
        val queue: ArrayDeque<Block> = ArrayDeque()
        val world = root.world

        visited.add(root)
        queue.add(root)

        val result = ArrayList<Block>(maxBlocks)
        while (queue.isNotEmpty() && result.size < maxBlocks) {
            val current = queue.removeFirst()
            result.add(current)

            // Explore 26-directional neighbors (3x3 cube minus center).
            for (offset in OFFSETS) {
                val nx = current.x + offset.first
                val ny = current.y + offset.second
                val nz = current.z + offset.third
                if (ny < world.minHeight || ny >= world.maxHeight) continue

                val neighbor = world.getBlockAt(nx, ny, nz)
                if (!allowedLogs.contains(neighbor.type)) continue

                if (visited.size >= maxBlocks) continue
                if (visited.add(neighbor)) {
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
    }
}
