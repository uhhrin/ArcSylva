package org.thatnubarin.arcSylva.confirmation

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.thatnubarin.arcSylva.api.model.TreeData
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple double-break confirmation tracker keyed by player + root block.
 */
class ConfirmationManager(private val timeoutSeconds: Int) {
    private val pending = ConcurrentHashMap<UUID, Pending>()

    /**
     * @return true if already confirmed (second break), false if we just recorded a pending confirmation.
     */
    fun checkOrPrompt(player: Player, tree: TreeData): Boolean {
        val now = System.currentTimeMillis()
        val existing = pending[player.uniqueId]
        if (existing != null) {
            if (now <= existing.expiresAt && sameRoot(existing.root, tree.root)) {
                pending.remove(player.uniqueId)
                return true
            }
            pending.remove(player.uniqueId)
        }
        pending[player.uniqueId] = Pending(tree.root, now + timeoutSeconds * 1000L)
        return false
    }

    private fun sameRoot(a: Block, b: Block): Boolean {
        return a.world.uid == b.world.uid && a.x == b.x && a.y == b.y && a.z == b.z
    }

    private data class Pending(val root: Block, val expiresAt: Long)
}
