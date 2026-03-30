package com.example.slotanalyzer.di

import android.content.Context
import androidx.room.Room
import com.example.slotanalyzer.data.database.AppDatabase
import com.example.slotanalyzer.data.database.dao.AppSettingDao
import com.example.slotanalyzer.data.database.dao.MachineCeilingRuleDao
import com.example.slotanalyzer.data.database.dao.MachineCounterDefinitionDao
import com.example.slotanalyzer.data.database.dao.MachineDao
import com.example.slotanalyzer.data.database.dao.MachineSettingReferenceValueDao
import com.example.slotanalyzer.data.database.dao.MasterMetadataDao
import com.example.slotanalyzer.data.database.dao.PlayHistoryDao
import com.example.slotanalyzer.data.database.dao.PlaySessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "slot_analyzer.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMachineDao(db: AppDatabase): MachineDao = db.machineDao()

    @Provides
    fun provideMachineCounterDefinitionDao(
        db: AppDatabase
    ): MachineCounterDefinitionDao = db.machineCounterDefinitionDao()

    @Provides
    fun provideMachineSettingReferenceValueDao(
        db: AppDatabase
    ): MachineSettingReferenceValueDao = db.machineSettingReferenceValueDao()

    @Provides
    fun provideMachineCeilingRuleDao(
        db: AppDatabase
    ): MachineCeilingRuleDao = db.machineCeilingRuleDao()

    @Provides
    fun providePlaySessionDao(db: AppDatabase): PlaySessionDao = db.playSessionDao()

    @Provides
    fun providePlayHistoryDao(db: AppDatabase): PlayHistoryDao = db.playHistoryDao()

    @Provides
    fun provideAppSettingDao(db: AppDatabase): AppSettingDao = db.appSettingDao()

    @Provides
    fun provideMasterMetadataDao(db: AppDatabase): MasterMetadataDao = db.masterMetadataDao()
}
