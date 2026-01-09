package com.mohammadfaizan.deathSwap

import org.bukkit.plugin.java.JavaPlugin

class DeathSwap : JavaPlugin() {

    companion object {
        lateinit var instance: DeathSwap
    }

    override fun onEnable() {
        // Plugin startup logic
        instance = this
        saveDefaultConfig()

        server.pluginManager.registerEvents(DeathListener(), this)
        server.pluginManager.registerEvents(PlayerListener(), this)
        getCommand("deathswap")?.setExecutor(DeathSwapCommand())

        logger.info("DeathSwap has been enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        GameManager.cleanup()
        logger.info("DeathSwap has been disabled!")
    }
}
