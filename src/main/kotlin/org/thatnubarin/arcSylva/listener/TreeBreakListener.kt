package org.thatnubarin.arcSylva.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.thatnubarin.arcSylva.confirmation.ConfirmationManager
import org.thatnubarin.arcSylva.config.Settings
import org.thatnubarin.arcSylva.core.breaking.TreeBreaker
import org.thatnubarin.arcSylva.core.tree.TreeDetector
import org.thatnubarin.arcSylva.core.validation.ValidationChain

class TreeBreakListener(
    private val detector: TreeDetector,
    private val breaker: TreeBreaker,
    private val validationChain: ValidationChain,
    private val confirmationManager: ConfirmationManager,
    private val settings: Settings
) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val player = event.player

        val detection = detector.detectTree(block, player)
        if (detection.isEmpty) return
        val tree = detection.get()

        val validation = validationChain.validate(player, block, tree)
        if (!validation.valid) {
            validation.message?.let { player.sendMessage(it) }
            return
        }

        if (tree.requiresConfirmation) {
            val confirmed = confirmationManager.requestOrConfirm(player, tree)
            if (!confirmed) {
                event.isCancelled = true
                player.sendMessage("This looks player-placed. Break again within ${settings.confirmTimeoutSeconds}s to confirm felling it.")
                return
            }
        }

        event.isCancelled = true
        val tool = player.inventory.itemInMainHand
        breaker.breakTree(tree, player, tool)
    }
}
