package org.thatnubarin.arcSylva.util

import org.bukkit.Material

/**
 * Cached material sets for fast membership checks.
 */
object MaterialSets {
    val LOGS: Set<Material> = setOf(
        Material.OAK_LOG,
        Material.BIRCH_LOG,
        Material.SPRUCE_LOG,
        Material.JUNGLE_LOG,
        Material.ACACIA_LOG,
        Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG,
        Material.CHERRY_LOG,
        Material.CRIMSON_STEM,
        Material.WARPED_STEM,
        Material.STRIPPED_OAK_LOG,
        Material.STRIPPED_BIRCH_LOG,
        Material.STRIPPED_SPRUCE_LOG,
        Material.STRIPPED_JUNGLE_LOG,
        Material.STRIPPED_ACACIA_LOG,
        Material.STRIPPED_DARK_OAK_LOG,
        Material.STRIPPED_MANGROVE_LOG,
        Material.STRIPPED_CHERRY_LOG,
        Material.STRIPPED_CRIMSON_STEM,
        Material.STRIPPED_WARPED_STEM
    )

    val LEAVES: Set<Material> = setOf(
        Material.OAK_LEAVES,
        Material.BIRCH_LEAVES,
        Material.SPRUCE_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.ACACIA_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.MANGROVE_LEAVES,
        Material.CHERRY_LEAVES,
        Material.NETHER_WART_BLOCK,
        Material.WARPED_WART_BLOCK
    )

    val AXES: Set<Material> = setOf(
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.COPPER_AXE,
        Material.IRON_AXE,
        Material.GOLDEN_AXE,
        Material.DIAMOND_AXE,
        Material.NETHERITE_AXE
    )
}
