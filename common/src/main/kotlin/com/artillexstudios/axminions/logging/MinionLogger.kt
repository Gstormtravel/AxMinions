package com.artillexstudios.axminions.logging

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.ArrayDeque
import java.util.concurrent.Executors

object MinionLogger {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "AxMinions-File-Logger").apply { isDaemon = true }
    }
    private val recentEntries = ArrayDeque<String>(500)
    private const val MAX_RECENT = 500

    private var currentDay: String = ""
    private var currentWriter: PrintWriter? = null

    fun log(minion: Minion, category: String, message: String) {
        val loc = minion.getLocation()
        val locStr = "${loc.world?.name ?: "world"}:${loc.blockX},${loc.blockY},${loc.blockZ}"
        val ownerName = minion.getOwner()?.name ?: minion.getOwnerUUID().toString()
        val firstOwner = minion.getFirstOwner().ifBlank { ownerName }
        val history = minion.getOwnershipHistoryString().ifBlank { firstOwner }

        val header = "[PrimeiroDono=$firstOwner | Historico=$history | DonoAtual=$ownerName | Minion=${minion.getType().getName()} Lv${minion.getLevel()} | Loc=$locStr]"
        writeFormatted(header, category, message)
    }

    fun logSystem(
        firstOwner: String,
        history: String,
        currentOwner: String,
        minionLabel: String,
        loc: Location?,
        category: String,
        message: String
    ) {
        val locStr = if (loc != null) {
            "${loc.world?.name ?: "world"}:${loc.blockX},${loc.blockY},${loc.blockZ}"
        } else {
            "global"
        }
        val safeFirst = firstOwner.ifBlank { currentOwner }
        val safeHistory = history.ifBlank { safeFirst }
        val header = "[PrimeiroDono=$safeFirst | Historico=$safeHistory | DonoAtual=$currentOwner | Minion=$minionLabel | Loc=$locStr]"
        writeFormatted(header, category, message)
    }

    private fun writeFormatted(header: String, category: String, message: String) {
        val time = LocalTime.now().format(timeFormatter)
        val day = LocalDate.now().format(dateFormatter)
        val line = "[$time] $header [$category] $message"

        synchronized(recentEntries) {
            if (recentEntries.size >= MAX_RECENT) {
                recentEntries.removeFirst()
            }
            recentEntries.addLast(line)
        }

        if (com.artillexstudios.axminions.api.config.Config.DEBUG()) {
            Bukkit.getLogger().info("[AxMinions-Log] $line")
        }

        executor.execute {
            writeToFile(day, line)
        }
    }

    @Synchronized
    private fun writeToFile(day: String, line: String) {
        try {
            if (currentWriter == null || currentDay != day) {
                currentWriter?.close()
                val logsDir = File(AxMinionsPlugin.INSTANCE.dataFolder, "logs")
                if (!logsDir.exists()) {
                    logsDir.mkdirs()
                }
                val file = File(logsDir, "$day.log")
                currentWriter = PrintWriter(FileWriter(file, true), true)
                currentDay = day
            }
            currentWriter?.println(line)
        } catch (t: Throwable) {
            Bukkit.getLogger().warning("[AxMinions] Falha ao gravar log em arquivo: ${t.message}")
        }
    }

    fun getRecentLogs(limit: Int = 20, filter: String = ""): List<String> {
        val trimmedFilter = filter.trim()
        val snapshot: List<String> = synchronized(recentEntries) {
            recentEntries.toList()
        }
        val filtered = if (trimmedFilter.isEmpty()) {
            snapshot
        } else {
            snapshot.filter { it.contains(trimmedFilter, ignoreCase = true) }
        }
        return filtered.takeLast(limit.coerceIn(1, MAX_RECENT))
    }

    fun getLatestLogFile(): File? {
        val logsDir = File(AxMinionsPlugin.INSTANCE.dataFolder, "logs")
        if (!logsDir.exists()) return null
        return logsDir.listFiles { f -> f.isFile && f.name.endsWith(".log") }
            ?.maxByOrNull { it.lastModified() }
    }

    @Synchronized
    fun shutdown() {
        try {
            currentWriter?.flush()
            currentWriter?.close()
            currentWriter = null
        } catch (_: Throwable) {
        }
        try {
            executor.shutdown()
        } catch (_: Throwable) {
        }
    }
}
