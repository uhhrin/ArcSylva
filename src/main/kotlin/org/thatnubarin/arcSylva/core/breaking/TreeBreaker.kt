package org.thatnubarin.arcSylva.core.breaking

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.thatnubarin.arcSylva.api.model.TreeData
import java.util.concurrent.CompletableFuture

interface TreeBreaker {
    fun breakTree(tree: TreeData, player: Player, tool: ItemStack): CompletableFuture<BreakResult>
}

data class BreakResult(val brokenBlocks: Int)
