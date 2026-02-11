package org.thatnubarin.arcSylva.core.tree

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.thatnubarin.arcSylva.api.model.TreeData
import java.util.Optional

interface TreeDetector {
    fun detectTree(block: Block, player: Player): Optional<TreeData>
}
