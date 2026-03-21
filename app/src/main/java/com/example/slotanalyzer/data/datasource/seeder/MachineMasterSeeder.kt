package com.example.slotanalyzer.data.datasource.seeder

interface MachineMasterSeeder {
    suspend fun seedIfNeeded(): SeedResult
    suspend fun forceReseed(): SeedResult
}
