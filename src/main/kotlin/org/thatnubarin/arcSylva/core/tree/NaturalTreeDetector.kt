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

        val base = findBaseLog(block)
        val verticalCount = countVerticalFromBase(base)
        if (verticalCount < settings.minVerticalLogs) return Optional.empty()

        val scanTimeoutNanos = settings.scanTimeoutMs.toLong() * 1_000_000
        val treeLogs = scanner.scanConnectedLogs(base, allowedLogs, settings.maxBlocks, scanTimeoutNanos)
        if (treeLogs.isEmpty()) return Optional.empty()

        val hasPlayerPlaced = repository.isPlayerPlaced(base) || treeLogs.any { repository.isPlayerPlaced(it) }
        val requiresConfirmation = hasPlayerPlaced && settings.confirmModified
        if (hasPlayerPlaced && !settings.confirmModified) {
            return Optional.empty()
        }

        val leaves = findLeavesAround(treeLogs, settings.minNaturalLeaves, scanTimeoutNanos)
        if (leaves.size < settings.minNaturalLeaves) return Optional.empty()

        val allBlocks = treeLogs + leaves
        if (allBlocks.size > settings.maxBlocks && !player.hasPermission(PermissionNode.BYPASS_LIMIT.node)) {
            return Optional.empty()
        }

        return Optional.of(TreeData(base, treeLogs, leaves, hasPlayerPlaced, requiresConfirmation))
    }

    private fun findLeavesAround(logs: List<Block>, needed: Int, timeoutNanos: Long): List<Block> {
        val radius = settings.leafRadius
        if (radius <= 0) return emptyList()

        val world = logs.firstOrNull()?.world ?: return emptyList()
        val leaves = mutableListOf<Block>()
        val seen = HashSet<Block>()
        val deadline = System.nanoTime() + timeoutNanos

        for (log in logs) {
            if (System.nanoTime() > deadline) break
            for (dx in -radius..radius) {
                for (dy in -radius..radius) {
                    for (dz in -radius..radius) {
                        if (System.nanoTime() > deadline) break
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

    private fun countVerticalFromBase(base: Block): Int {
        var count = 0
        var current: Block? = base
        while (current != null && count < settings.maxBlocks && allowedLogs.contains(current.type)) {
            count++
            current = current.getRelative(0, 1, 0)
        }
        return count
    }

    private fun findBaseLog(start: Block): Block {
        var current = start
        var steps = 0
        while (steps < settings.maxBlocks) {
            val below = current.getRelative(0, -1, 0)
            if (!allowedLogs.contains(below.type)) break
            current = below
            steps++
        }
        return current
    }
}
