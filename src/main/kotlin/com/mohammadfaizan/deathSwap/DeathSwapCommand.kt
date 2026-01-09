package com.mohammadfaizan.deathSwap

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.UUID

class DeathSwapCommand: CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (command.name.equals("deathswap", ignoreCase = true)) {

            if (args.isEmpty()) {
                sender.sendMessage("§cUsage: /deathswap [ start | stop | status | quit ]")
                return true
            }

            when (args[0].lowercase()) {
                "start" -> {
                    if (GameManager.state == GameManager.GameState.RUNNING) {
                        sender.sendMessage("§cA Death Swap game is already running.")
                    } else {
                        GameManager.startGame()
                    }
                    return true
                }

                "stop" -> {
                    if (GameManager.state != GameManager.GameState.RUNNING) {
                        sender.sendMessage("§cNo game is currently running.")
                    } else {
                        GameManager.endGame()
                    }
                    return true
                }

                "status" -> {
                    sender.sendMessage("§eDeath Swap Game Status: §a${GameManager.state}")
                    sender.sendMessage("§ePlayers: §a${GameManager.players.size}")
                    return true
                }

                "quit" -> {
                    if (sender is org.bukkit.entity.Player) {
                        val playerUUID: UUID = sender.uniqueId
                        if (GameManager.players.containsKey(playerUUID)) {
                            GameManager.quitGame(playerUUID)
                        } else {
                            sender.sendMessage("§cYou are not part of the Death Swap game.")
                        }
                    } else {
                        sender.sendMessage("§cOnly players can use this command.")
                    }
                }

                else -> {
                    sender.sendMessage("§cUnknown subcommand. Usage: /deathswap < start | stop | status | quit >")
                    return true
                }
            }
        }
        return false
    }
}