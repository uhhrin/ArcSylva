package org.thatnubarin.arcSylva.confirmation

import org.bukkit.entity.Player
import org.thatnubarin.arcSylva.api.model.TreeData
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ConfirmationManager(private val timeoutSeconds: Int) {
    private val pending = ConcurrentHashMap<UUID, Pending>()

    /**
     * Returns true when confirmation is satisfied; false means a prompt was created and action should stop.
     */
    fun requestOrConfirm(player: Player, tree: TreeData): Boolean {
        val now = System.currentTimeMillis()
        val existing = pending[player.uniqueId]
        if (existing != null) {
            if (now <= existing.expiresAt && sameRoot(existing.tree, tree)) {
                pending.remove(player.uniqueId)
                return true
            }
            pending.remove(player.uniqueId)
        }
        pending[player.uniqueId] = Pending(tree, now + timeoutSeconds * 1000L)
        return false
    }

    fun clear(player: Player) {
        pending.remove(player.uniqueId)
    }

    private fun sameRoot(first: TreeData, second: TreeData): Boolean {
        val a = first.root
        val b = second.root
        val worldMatch = a.world.uid == b.world.uid
        return worldMatch && a.x == b.x && a.y == b.y && a.z == b.z
    }

    private data class Pending(val tree: TreeData, val expiresAt: Long)
}
