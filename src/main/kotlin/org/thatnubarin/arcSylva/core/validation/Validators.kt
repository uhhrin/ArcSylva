package org.thatnubarin.arcSylva.core.validation

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.thatnubarin.arcSylva.config.Settings
import org.thatnubarin.arcSylva.data.repository.BlockDataRepository
import org.thatnubarin.arcSylva.permission.PermissionNode
import org.thatnubarin.arcSylva.util.MaterialSets

class PermissionValidator(private val settings: Settings) : TreeValidator {
    override fun validate(player: Player, block: Block, tree: org.thatnubarin.arcSylva.api.model.TreeData?): ValidationResult {
        return if (player.hasPermission(PermissionNode.USE.node)) ValidationResult.success()
        else ValidationResult.failure("You lack permission to fell trees.")
    }
}

class ToolValidator(private val settings: Settings) : TreeValidator {
    override fun validate(player: Player, block: Block, tree: org.thatnubarin.arcSylva.api.model.TreeData?): ValidationResult {
        if (!settings.requireAxe) return ValidationResult.success()
        val item: ItemStack? = player.inventory.itemInMainHand
        return if (item != null && MaterialSets.AXES.contains(item.type)) ValidationResult.success()
        else ValidationResult.failure("You need an axe to fell trees.")
    }
}

class SneakValidator(private val settings: Settings) : TreeValidator {
    override fun validate(player: Player, block: Block, tree: org.thatnubarin.arcSylva.api.model.TreeData?): ValidationResult {
        if (settings.sneakToDisable && player.isSneaking) {
            return ValidationResult.failure("Sneaking disables auto tree felling.")
        }
        return ValidationResult.success()
    }
}

class NaturalBlockValidator(private val repository: BlockDataRepository) : TreeValidator {
    override fun validate(player: Player, block: Block, tree: org.thatnubarin.arcSylva.api.model.TreeData?): ValidationResult {
        // All player-placed checks are handled in detection/confirmation to avoid double messages.
        return ValidationResult.success()
    }
}
