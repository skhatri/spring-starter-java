package com.github.starter.endpoint

import com.github.pokemon.model.Pokemon
import com.github.starter.repository.EntriesRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

@RestController
@RequestMapping("pokemon")
class EntriesEndpoint(private val entriesRepository: EntriesRepository) {

    @GetMapping("/list")
    fun listEntries(): Flow<Pokemon> = entriesRepository.listEntries()

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP", "server_time" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
    }

    @GetMapping("/check")
    fun detailedHealthCheck(): Map<String, String> {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        var reason = ""
        var status = "UP"
        try {
            val appData = System.getenv("APP_DATA_PATH")
            val f = File("$appData/test.txt")
            f.writeText("last check $now")
        } catch (e: Exception) {
            reason = "${e.message}"
            status = "DOWN"
        }
        return mapOf("status" to status, "server_time" to now, "reason" to reason)
    }

}
