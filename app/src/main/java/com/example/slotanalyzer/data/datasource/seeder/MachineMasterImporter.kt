package com.example.slotanalyzer.data.datasource.seeder

import com.example.slotanalyzer.data.datasource.model.MachineMasterJson

interface MachineMasterImporter {
    suspend fun importMachine(master: MachineMasterJson, now: Long)
}
