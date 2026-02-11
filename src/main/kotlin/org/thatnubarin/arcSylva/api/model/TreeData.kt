package org.thatnubarin.arcSylva.api.model

import org.bukkit.Location
import org.bukkit.block.Block

/**
 * Represents a detected tree to be felled.
 */
data class TreeData(
    val root: Block,
    val logBlocks: List<Block>,
    val leafBlocks: List<Block>,
    val hasPlayerPlaced: Boolean,
    val requiresConfirmation: Boolean
) {
    val blockCount: Int get() = logBlocks.size + leafBlocks.size
    val center: Location get() = root.location
}
