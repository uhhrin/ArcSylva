package org.thatnubarin.arcSylva

import org.bukkit.plugin.java.JavaPlugin
import org.thatnubarin.arcSylva.confirmation.ConfirmationManager
import org.thatnubarin.arcSylva.config.YamlConfigManager
import org.thatnubarin.arcSylva.core.breaking.BatchTreeBreaker
import org.thatnubarin.arcSylva.core.tree.FloodTreeScanner
import org.thatnubarin.arcSylva.core.tree.NaturalTreeDetector
import org.thatnubarin.arcSylva.core.validation.NaturalBlockValidator
import org.thatnubarin.arcSylva.core.validation.PermissionValidator
import org.thatnubarin.arcSylva.core.validation.SneakValidator
import org.thatnubarin.arcSylva.core.validation.ToolValidator
import org.thatnubarin.arcSylva.core.validation.ValidationChain
import org.thatnubarin.arcSylva.data.repository.PDCBlockRepository
import org.thatnubarin.arcSylva.listener.BlockPlaceListener
import org.thatnubarin.arcSylva.listener.TreeBreakListener

class ArcSylva : JavaPlugin() {
    private lateinit var configManager: YamlConfigManager

    override fun onEnable() {
        configManager = YamlConfigManager(this)
        val settings = configManager.settings()

        val repository = PDCBlockRepository(this)
        val confirmationManager = ConfirmationManager(settings.confirmTimeoutSeconds)
        val detector = NaturalTreeDetector(settings, repository, FloodTreeScanner())
        val breaker = BatchTreeBreaker(this, settings, repository)
        val validators = listOf(
            PermissionValidator(settings),
            ToolValidator(settings),
            SneakValidator(settings),
            NaturalBlockValidator(repository)
        )
        val chain = ValidationChain(validators)

        server.pluginManager.registerEvents(TreeBreakListener(detector, breaker, chain, confirmationManager, settings), this)
        server.pluginManager.registerEvents(BlockPlaceListener(repository), this)

        logger.info("ArcSylva enabled with log-only tree felling")
    }

    override fun onDisable() {
        logger.info("ArcSylva disabled")
    }
}
