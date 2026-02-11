package org.thatnubarin.arcSylva.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.thatnubarin.arcSylva.data.repository.BlockDataRepository
import org.thatnubarin.arcSylva.util.MaterialSets

class BlockPlaceListener(private val repository: BlockDataRepository) : Listener {
    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        if (MaterialSets.LOGS.contains(block.type)) {
            repository.markAsPlayerPlaced(block)
        }
    }
}
