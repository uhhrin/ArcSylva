package org.thatnubarin.arcSylva.config

import org.bukkit.configuration.file.FileConfiguration

/**
 * Immutable snapshot of plugin settings.
 */
data class Settings(
    val requireAxe: Boolean,
    val sneakToDisable: Boolean,
    val confirmModified: Boolean,
    val confirmTimeoutSeconds: Int,
    val maxBlocks: Int,
    val blocksPerTick: Int,
    val scanTimeoutMs: Int,
    val leafRadius: Int,
    val minVerticalLogs: Int,
    val minNaturalLeaves: Int,
    val enabledWorlds: Set<String>,
    val allowedLogTypes: Set<String>,
    val playSound: Boolean
) {
    companion object {
        fun fromConfig(config: FileConfiguration): Settings = Settings(
            requireAxe = config.getBoolean("require-axe", true),
            sneakToDisable = config.getBoolean("sneak-to-disable", true),
            confirmModified = config.getBoolean("confirm-modified-trees", true),
            confirmTimeoutSeconds = config.getInt("confirmations.timeout", 8),
            maxBlocks = config.getInt("limits.max-blocks-per-tree", 64),
            blocksPerTick = config.getInt("limits.blocks-per-tick", 12),
            scanTimeoutMs = config.getInt("limits.scan-timeout-ms", 100),
            leafRadius = config.getInt("detection.leaf-search-radius", 4),
            minVerticalLogs = config.getInt("detection.min-vertical-logs", 2),
            minNaturalLeaves = config.getInt("detection.min-natural-leaves", 1),
            enabledWorlds = config.getStringList("enabled-worlds").toSet(),
            allowedLogTypes = config.getStringList("detection.allowed-log-types").toSet(),
            playSound = config.getBoolean("features.play-sound", true)
        )
    }
}
