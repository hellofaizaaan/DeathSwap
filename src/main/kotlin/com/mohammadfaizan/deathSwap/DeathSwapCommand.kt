package com.mohammadfaizan.deathSwap

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.UUID

class DeathSwapCommand: CommandExecutor, TabCompleter {
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
                    if (sender is Player) {
                        val playerUUID: UUID = sender.uniqueId
                        if (GameManager.players.containsKey(playerUUID)) {
                            GameManager.quitGame(playerUUID)
                        } else {
                            sender.sendMessage("§cYou are not part of the Death Swap game.")
                        }
                    } else {
                        sender.sendMessage("§cOnly players can use this command.")
                    }
                    return true
                }

                "help" -> {
                    sender.sendMessage(" ")
                    sender.sendMessage("§e------ §6Death Swap Help §e--------")
                    sender.sendMessage("")
                    sender.sendMessage("§6/ds start §7- Start the Death Swap game")
                    sender.sendMessage("§6/ds quit §7- Quit the current game")
                    sender.sendMessage("§6/ds about §7- Information about the plugin")
                    sender.sendMessage("")
                    sender.sendMessage("§e-----------------------------------")
                    return true
                }

                "about" -> {
                    sender.sendMessage(" ")
                    sender.sendMessage("§e------ §6About Death Swap §e---------")
                    sender.sendMessage("")
                    sender.sendMessage("§eDeath Swap Plugin §61.0.00 §eby §9§nMohammad Faizan§e")
                    sender.sendMessage("§eWebsite: §9§nmohammadfaizan.com§e")
                    sender.sendMessage("§e-----------------------------------")
                    return true
                }

                else -> {
                    sender.sendMessage("§cUnknown subcommand. Usage: /deathswap < start | stop | status | quit >")
                    return true
                }
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (command.name.equals("deathswap", ignoreCase = true)) {
            if (args.size == 1) {
                val subcommands = listOf("start", "stop", "status", "quit", "help", "about")
                val input = args[0].lowercase()

                // Filter suggestions based on what user has typed
                return subcommands.filter { it.startsWith(input) }
            }
        }
        return emptyList()
    }
}