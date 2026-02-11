package org.thatnubarin.arcSylva.core.tree

import org.bukkit.block.Block

interface TreeScanner {
    /**
     * Collects connected log blocks starting from root, bounded by maxBlocks and timeoutNanos.
     */
    fun scanConnectedLogs(root: Block, allowedLogs: Set<org.bukkit.Material>, maxBlocks: Int, timeoutNanos: Long): List<Block>
}
