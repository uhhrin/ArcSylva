package org.thatnubarin.arcSylva.core.tree

import org.bukkit.Material
import org.bukkit.block.Block

/**
 * Legacy vertical-only scanner; kept for compatibility but currently unused.
 */
class VerticalTreeScanner : TreeScanner {
    override fun scanConnectedLogs(root: Block, allowedLogs: Set<Material>, maxBlocks: Int): List<Block> {
        val logs = mutableListOf<Block>()
        var current: Block? = root
        var count = 0
        while (current != null && count < maxBlocks && allowedLogs.contains(current.type)) {
            logs.add(current)
            count++
            current = current.getRelative(0, 1, 0)
        }
        return logs
    }
}
