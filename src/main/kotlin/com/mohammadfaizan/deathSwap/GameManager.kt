package com.mohammadfaizan.deathSwap

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.scheduler.BukkitTask
import java.util.*

object GameManager {

    enum class GameState {
        WAITING,
        RUNNING,
        ENDED
    }

    data class PlayerData(
        val uuid: UUID,
        var isAlive: Boolean,
    )

    var state = GameState.WAITING
    val players = mutableMapOf<UUID, PlayerData>()
    var round = 1

    private var currentTimer: BukkitTask? = null
    private var showBossBar = false
    private var bossBar: BossBar? = null
    private var roundTimeTotal: Int = 300 // Example: 5 minutes per round

    private var halfTimeAnnouncement = false

    private var halfTimeAnnounced = false
    private var lastSecondAnnounced = -1

    private val config = DeathSwap.instance.config

    fun startGame() {
        val minPlayers = 2
        if (Bukkit.getOnlinePlayers().size < minPlayers) {
            Bukkit.broadcastMessage("§cNot enough players to start the game. Minimum required: $minPlayers")
            state = GameState.ENDED
            return
        }

        state = GameState.RUNNING
        round = 1
        players.clear()

        Bukkit.broadcastMessage("§a\\uD83D\\uDFE2 Death Swap started!")

        for (player in Bukkit.getOnlinePlayers()) {
            players[player.uniqueId] = PlayerData(
                player.uniqueId, isAlive = true
            )
            player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f)
        }

        startTimer()
        // startBossBar()
    }

    fun startTimer() {
        currentTimer?.cancel()
        removeBossBar()
        halfTimeAnnounced = false
        lastSecondAnnounced = -1

        roundTimeTotal = config.getInt("round-time", 300)
        var timeLeft = roundTimeTotal

        showBossBar = config.getBoolean("allow-bossbar", false)
        if (showBossBar) {
            bossBar = Bukkit.createBossBar("Death Swap - Time Left: ${formatTime(timeLeft)}", BarColor.GREEN, BarStyle.SOLID)

            players.keys.forEach { uuid ->
                Bukkit.getPlayer(uuid)?.let { player ->
                    bossBar?.addPlayer(player)
                }
            }

            bossBar?.progress = 1.0
        }

        currentTimer = Bukkit.getScheduler().runTaskTimer(DeathSwap.instance, Runnable {
            if (state != GameState.RUNNING) {
                currentTimer?.cancel()
                removeBossBar()
                return@Runnable
            }
            checkActivePlayers()

            // half time announcement
            halfTimeAnnouncement = config.getBoolean("halfway-announcement", false)
            if (halfTimeAnnouncement) {
                if (!halfTimeAnnounced && timeLeft <= roundTimeTotal / 2) {
                    Bukkit.broadcastMessage("§e\\u23F1 Half time! §6\${formatTime(timeLeft)} §eremaining.")
                    // PLay a sound to all players
                    players.keys.forEach { uuid ->
                        Bukkit.getPlayer(uuid)?.playSound(
                            Bukkit.getPlayer(uuid)?.location ?: return@forEach,
                            Sound.BLOCK_NOTE_BLOCK_PLING,
                            1.0f,
                            1.0f
                        )
                    }
                    halfTimeAnnounced = true
                }
            }

            if (timeLeft in 1..10 && timeLeft != lastSecondAnnounced) {
                lastSecondAnnounced = timeLeft

                players.values.forEach { data ->
                    val player = Bukkit.getPlayer(data.uuid) ?: return@forEach
                    val pitch = 0.5f + (11 - timeLeft) * 0.1f
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch)
                }
            }else if (timeLeft > 10 && lastSecondAnnounced in 1..10) {
                lastSecondAnnounced = -1
            }

            if (timeLeft <= 0) {
                currentTimer?.cancel()
                currentTimer = null
                removeBossBar()
                swapPlayers()
                if (state == GameState.RUNNING) {
                    round++
                    startTimer()
                }
                return@Runnable
            }

            val progress = timeLeft.toDouble() / roundTimeTotal.toDouble()
            if (showBossBar) {
                bossBar?.setTitle("Death Swap - Time Left: ${formatTime(timeLeft)}")
                bossBar?.progress = progress.coerceIn(0.0, 1.0)

                when {
                    timeLeft <= 30 -> bossBar?.color = BarColor.RED
                    timeLeft <= 60 -> bossBar?.color = BarColor.YELLOW
                    else -> bossBar?.color = BarColor.GREEN
                }
            }

            timeLeft--
        }, 0L, 20L)
    }

    fun endGame() {
        if (state != GameState.RUNNING) return

        state = GameState.ENDED
        currentTimer?.cancel()
        currentTimer = null
        removeBossBar()

        Bukkit.broadcastMessage("§c\\u26D4 Death Swap has been ended by an op.")

        players.clear()
        round = 1
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", minutes, sec)
    }

    private fun removeBossBar() {
        bossBar?.removeAll()
        bossBar = null
    }

    fun removeBossBarFromPlayer(playerUuid: UUID) {
        bossBar?.let { bar ->
            Bukkit.getPlayer(playerUuid)?.let { player ->
                bar.removePlayer(player)
            }
        }
    }

    private fun updateBossBarPlayers() {
        bossBar?.let { bar ->
            bar.players.forEach { player ->
                if (!players.containsKey(player.uniqueId)) {
                    bar.removePlayer(player)
                }
            }
            players.keys.forEach { uuid ->
                Bukkit.getPlayer(uuid)?.let { player ->
                    if (!bar.players.contains(player)) {
                        bar.addPlayer(player)
                    }
                }
            }
        }
    }

    private fun checkActivePlayers() {
        val offlinePlayers = players.keys.filter { Bukkit.getPlayer(it) == null }
        offlinePlayers.forEach { player ->
            players.remove(player)
        }
        updateBossBarPlayers()

        if (players.isEmpty() && state == GameState.RUNNING) {
            Bukkit.broadcastMessage("§cAll players have left the game. Ending the game.")
            state = GameState.ENDED
            currentTimer?.cancel()
            currentTimer = null
            removeBossBar()
        }
    }

    fun quitGame(playerUuid: UUID) {
        if (state != GameState.RUNNING) return

        val player = Bukkit.getPlayer(playerUuid) ?: return
        val playerData = players.remove(playerUuid) ?: return

        bossBar?.removePlayer(player)

        if (players.isEmpty()) {
            Bukkit.broadcastMessage("§cGame ended - no players remaining.")
            state = GameState.ENDED
            currentTimer?.cancel()
            currentTimer = null
            removeBossBar()
            return
        }

        if (players.size == 1) {
            val remainingPlayer = Bukkit.getPlayer(players.values.first().uuid)
            remainingPlayer?.sendMessage("§aYou are the last player standing!")
            Bukkit.broadcastMessage("§a${remainingPlayer?.name} is the last player standing!")
            state = GameState.ENDED
            currentTimer?.cancel()
            currentTimer = null
            removeBossBar()
            return
        }

        // Multiple players still in game - continue
        Bukkit.broadcastMessage("§e${player.name} has quit the game. ${players.size} players remaining.")
    }

    fun handlePlayerDisconnect(playerUuid: UUID) {
        if (state != GameState.RUNNING) return

        val playerData = players.remove(playerUuid) ?: return
        val playerName = Bukkit.getOfflinePlayer(playerUuid).name ?: "A player"

        updateBossBarPlayers()

        Bukkit.broadcastMessage("§c\u26A0 $playerName has disconnected and is removed from the game.")

        if (players.isEmpty()) {
            Bukkit.broadcastMessage("§cAll players have left the game. Ending the game.")
            state = GameState.ENDED
            currentTimer?.cancel()
            currentTimer = null
            removeBossBar()
            return
        }

        // Last player standing
        if (players.size == 1) {
            val remainingPlayer = Bukkit.getPlayer(players.values.first().uuid)
            remainingPlayer?.sendMessage("§aYou are the last player standing!")
            Bukkit.broadcastMessage("§a${remainingPlayer?.name} is the last player standing thus a WINNER!")
            state = GameState.ENDED
            currentTimer?.cancel()
            currentTimer = null
            removeBossBar()
            return
        }
    }

    fun cleanup() {
        currentTimer?.cancel()
        currentTimer = null
        state = GameState.ENDED
        round = 1
        players.clear()
        removeBossBar()
    }

    private fun swapPlayers() {
        val alivePlayers = players.values.filter { it.isAlive }
            .mapNotNull { Bukkit.getPlayer(it.uuid) }
            .toMutableList()

        if (alivePlayers.size < 2) {
            Bukkit.broadcastMessage("§cNot enough players to swap.")
            if (alivePlayers.size < 2) {
                val winner = alivePlayers.first()
                Bukkit.broadcastMessage("§a§l${winner.name} is the last player standing!")
                cleanup()
            }
            return
        }
        // Shuffle swap list
        alivePlayers.shuffle()
        val locations = alivePlayers.map { it.location.clone() }

        // Swap locations in pairs
        for (i in alivePlayers.indices step 2) {
            if (i + 1 < alivePlayers.size) {
                val playerA = alivePlayers[i]
                val playerB = alivePlayers[i + 1]

                val locA = locations[i]
                val locB = locations[i + 1]

                playerA.teleport(locB)
                playerB.teleport(locA)

                playerA.playSound(playerA.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
                playerB.playSound(playerB.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)

                if (alivePlayers.size > 2) {
                    playerA.sendMessage("§aYou have been swapped with §6${playerB.name}§a!")
                    playerB.sendMessage("§aYou have been swapped with §6${playerA.name}§a!")
                }
            }
        }
        Bukkit.broadcastMessage("§a\\u21C4 Players have been swapped!")
    }
}