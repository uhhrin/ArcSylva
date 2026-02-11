package org.thatnubarin.arcSylva.core.breaking

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.thatnubarin.arcSylva.api.model.TreeData
import org.thatnubarin.arcSylva.config.Settings
import java.util.concurrent.CompletableFuture

class BatchTreeBreaker(
    private val plugin: JavaPlugin,
    private val settings: Settings
) : TreeBreaker {
    override fun breakTree(tree: TreeData, player: Player, tool: ItemStack): CompletableFuture<BreakResult> {
        val future = CompletableFuture<BreakResult>()
        val blocks = tree.logBlocks
            .sortedWith(compareByDescending<Block> { it.y }.thenBy { it.x }.thenBy { it.z })
        object : BukkitRunnable() {
            var index = 0
            override fun run() {
                if (!player.isOnline || player.world != tree.root.world) {
                    cancel(); future.completeExceptionally(IllegalStateException("Player not present")); return
                }
                val perTick = settings.blocksPerTick
                var processed = 0
                while (index < blocks.size && processed < perTick) {
                    val block = blocks[index]
                    if (block.type.isAir) {
                        index++; processed++; continue
                    }
                    val breakEvent = org.bukkit.event.block.BlockBreakEvent(block, player)
                    Bukkit.getPluginManager().callEvent(breakEvent)
                    if (!breakEvent.isCancelled) {
                        block.breakNaturally(tool, true)
                    }
                    index++; processed++
                }
                if (index >= blocks.size) {
                    if (settings.playSound) {
                        player.world.playSound(tree.root.location, Sound.BLOCK_WOOD_BREAK, 0.8f, 1.0f)
                    }
                    cancel(); future.complete(BreakResult(blocks.size))
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
        return future
    }
}
