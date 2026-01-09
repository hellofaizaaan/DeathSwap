package com.mohammadfaizan.deathSwap

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathListener: Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player

        if (GameManager.state == GameManager.GameState.RUNNING && GameManager.players.containsKey(player.uniqueId)) {
            val playerData = GameManager.players[player.uniqueId]
            playerData?.isAlive = false

            GameManager.players.remove(player.uniqueId)
            GameManager.removeBossBarFromPlayer(player.uniqueId)
            val aliveCount = GameManager.players.values.count { it.isAlive }

            if (aliveCount == 0) {
                Bukkit.broadcastMessage("§cAll players have died! No winners this round.")
                GameManager.endGame()
                GameManager.cleanup()
            } else if (aliveCount == 1) {
                val winner = GameManager.players.values.firstOrNull { it.isAlive }
                winner?.let {
                    val winnerPlayer = Bukkit.getPlayer(it.uuid)
                    Bukkit.broadcastMessage("§a${winnerPlayer?.name} is the last player standing!")
                    GameManager.endGame()
                    GameManager.cleanup()
                }
            } else {
                Bukkit.broadcastMessage("§e${player.name} has died! §a$aliveCount players remain alive.")
            }
        }
    }
}