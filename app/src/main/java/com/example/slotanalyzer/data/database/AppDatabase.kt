package com.example.slotanalyzer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.slotanalyzer.data.database.dao.AppSettingDao
import com.example.slotanalyzer.data.database.dao.MachineDao
import com.example.slotanalyzer.data.database.dao.MasterMetadataDao
import com.example.slotanalyzer.data.database.dao.PlayHistoryDao
import com.example.slotanalyzer.data.database.dao.PlaySessionDao
import com.example.slotanalyzer.data.database.entity.AppSettingEntity
import com.example.slotanalyzer.data.database.entity.MachineCeilingRuleEntity
import com.example.slotanalyzer.data.database.entity.MachineCounterDefinitionEntity
import com.example.slotanalyzer.data.database.entity.MachineEntity
import com.example.slotanalyzer.data.database.entity.MachineSettingReferenceValueEntity
import com.example.slotanalyzer.data.database.entity.MasterMetadataEntity
import com.example.slotanalyzer.data.database.entity.PlayHistoryEntity
import com.example.slotanalyzer.data.database.entity.PlaySessionEntity
import com.example.slotanalyzer.data.database.entity.SessionCounterValueEntity

@Database(
    entities = [
        MachineEntity::class,
        MachineCounterDefinitionEntity::class,
        MachineSettingReferenceValueEntity::class,
        MachineCeilingRuleEntity::class,
        PlaySessionEntity::class,
        SessionCounterValueEntity::class,
        PlayHistoryEntity::class,
        AppSettingEntity::class,
        MasterMetadataEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun machineDao(): MachineDao
    abstract fun playSessionDao(): PlaySessionDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun masterMetadataDao(): MasterMetadataDao
}
