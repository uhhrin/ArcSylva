package org.thatnubarin.arcSylva.core.validation

import org.bukkit.entity.Player
import org.bukkit.block.Block
import org.thatnubarin.arcSylva.api.model.TreeData

/** Simple validation result model. */
data class ValidationResult(val valid: Boolean, val message: String? = null) {
    companion object {
        fun success() = ValidationResult(true, null)
        fun failure(message: String) = ValidationResult(false, message)
    }
}

fun interface TreeValidator {
    fun validate(player: Player, block: Block, tree: TreeData?): ValidationResult
}

class ValidationChain(private val validators: List<TreeValidator>) {
    fun validate(player: Player, block: Block, tree: TreeData?): ValidationResult {
        for (validator in validators) {
            val result = validator.validate(player, block, tree)
            if (!result.valid) return result
        }
        return ValidationResult.success()
    }
}
