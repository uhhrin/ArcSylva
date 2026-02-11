package org.thatnubarin.arcSylva.core.tree

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.thatnubarin.arcSylva.api.model.TreeData
import org.thatnubarin.arcSylva.config.Settings
import org.thatnubarin.arcSylva.data.repository.BlockDataRepository
import org.thatnubarin.arcSylva.permission.PermissionNode
import org.thatnubarin.arcSylva.util.MaterialSets
import java.util.Optional

class NaturalTreeDetector(
    private val settings: Settings,
    private val repository: BlockDataRepository,
    private val scanner: TreeScanner
) : TreeDetector {
    private val allowedLogs: Set<Material> = MaterialSets.LOGS
        .filter { settings.allowedLogTypes.isEmpty() || settings.allowedLogTypes.contains(it.name) }
        .toSet()

    override fun detectTree(block: Block, player: Player): Optional<TreeData> {
        if (allowedLogs.isEmpty()) return Optional.empty()
        if (!allowedLogs.contains(block.type)) return Optional.empty()
        if (settings.enabledWorlds.isNotEmpty() && !settings.enabledWorlds.contains(block.world.name)) {
            return Optional.empty()
        }

        val verticalCount = countVerticalLogs(block)
        if (verticalCount < settings.minVerticalLogs) return Optional.empty()

        val treeLogs = scanner.scanConnectedLogs(block, allowedLogs, settings.maxBlocks)
        if (treeLogs.isEmpty()) return Optional.empty()

        val hasPlayerPlaced = treeLogs.any { repository.isPlayerPlaced(it) }
        val requiresConfirmation = hasPlayerPlaced && settings.confirmModified && !player.hasPermission(PermissionNode.FORCE.node)
        if (hasPlayerPlaced && !settings.confirmModified && !player.hasPermission(PermissionNode.FORCE.node)) {
            return Optional.empty()
        }

        val leaves = findLeavesAround(treeLogs, settings.minNaturalLeaves)
        if (leaves.size < settings.minNaturalLeaves) return Optional.empty()

        val allBlocks = treeLogs + leaves
        if (allBlocks.size > settings.maxBlocks && !player.hasPermission(PermissionNode.BYPASS_LIMIT.node)) {
            return Optional.empty()
        }

        return Optional.of(TreeData(block, treeLogs, leaves, hasPlayerPlaced, requiresConfirmation))
    }

    private fun findLeavesAround(logs: List<Block>, needed: Int): List<Block> {
        val radius = settings.leafRadius
        if (radius <= 0) return emptyList()

        val world = logs.firstOrNull()?.world ?: return emptyList()
        val leaves = mutableListOf<Block>()
        val seen = HashSet<Block>()

        for (log in logs) {
            for (dx in -radius..radius) {
                for (dy in -radius..radius) {
                    for (dz in -radius..radius) {
                        val bx = log.x + dx
                        val by = log.y + dy
                        val bz = log.z + dz
                        if (by < world.minHeight || by >= world.maxHeight) continue
                        val candidate = world.getBlockAt(bx, by, bz)
                        if (MaterialSets.LEAVES.contains(candidate.type) && seen.add(candidate)) {
                            leaves.add(candidate)
                            if (leaves.size >= needed) return leaves
                        }
                    }
                }
            }
        }
        return leaves
    }

    private fun countVerticalLogs(block: Block): Int {
        var count = 0
        var current: Block? = block
        while (current != null && count < settings.maxBlocks && allowedLogs.contains(current.type)) {
            count++
            current = current.getRelative(0, 1, 0)
        }
        return count
    }
}
