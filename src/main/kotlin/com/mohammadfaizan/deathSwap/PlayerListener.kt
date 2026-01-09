package com.mohammadfaizan.deathSwap

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        if (GameManager.state == GameManager.GameState.RUNNING &&
            GameManager.players.containsKey(player.uniqueId)
        ) {
            GameManager.handlePlayerDisconnect(player.uniqueId)
        }
    }
}