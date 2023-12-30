package com.github.starter.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.flow
import org.springframework.stereotype.Repository
import com.github.pokemon.model.Pokemon

@Repository
class EntriesRepository(private val client: DatabaseClient) {

    fun listEntries(): Flow<Pokemon> {
        return client.sql("select * from pokedex.entries")
            .fetch()
            .flow().map { row -> Pokemon.fromMap(row) }
    }
}