package org.thatnubarin.arcSylva.config

import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.atomic.AtomicReference

class YamlConfigManager(private val plugin: JavaPlugin) {
    private val cached = AtomicReference<Settings>()

    fun settings(): Settings {
        return cached.updateAndGet { existing -> existing ?: load() }
    }

    fun reload() {
        plugin.reloadConfig()
        cached.set(load())
    }

    private fun load(): Settings {
        plugin.saveDefaultConfig()
        val cfg = plugin.config
        return Settings.fromConfig(cfg)
    }
}
