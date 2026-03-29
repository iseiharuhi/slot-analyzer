package com.example.slotanalyzer.di

import android.content.Context
import androidx.room.Room
import com.example.slotanalyzer.data.database.AppDatabase
import com.example.slotanalyzer.data.database.dao.MachineCeilingRuleDao
import com.example.slotanalyzer.data.database.dao.MachineCounterDefinitionDao
import com.example.slotanalyzer.data.database.dao.MachineDao
import com.example.slotanalyzer.data.database.dao.MachineSettingReferenceValueDao
import com.example.slotanalyzer.data.database.dao.MasterMetadataDao
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
    fun provideMachineDao(db: AppDatabase): MachineDao {
        return db.machineDao()
    }

    @Provides
    fun provideCounterDao(db: AppDatabase): MachineCounterDefinitionDao {
        return db.machineCounterDefinitionDao()
    }

    @Provides
    fun provideReferenceDao(db: AppDatabase): MachineSettingReferenceValueDao {
        return db.machineSettingReferenceValueDao()
    }

    @Provides
    fun provideCeilingDao(db: AppDatabase): MachineCeilingRuleDao {
        return db.machineCeilingRuleDao()
    }

    @Provides
    fun provideMasterMetadataDao(db: AppDatabase): MasterMetadataDao {
        return db.masterMetadataDao()
    }

    @Provides
    fun providePlaySessionDao(db: AppDatabase): PlaySessionDao {
        return db.playSessionDao()
    }
}