package com.example.slotanalyzer.di

import com.example.slotanalyzer.data.repository.AppSettingRepositoryImpl
import com.example.slotanalyzer.data.repository.MachineRepositoryImpl
import com.example.slotanalyzer.data.repository.MasterSyncRepositoryImpl
import com.example.slotanalyzer.data.repository.PlaySessionRepositoryImpl
import com.example.slotanalyzer.domain.repository.AppSettingRepository
import com.example.slotanalyzer.domain.repository.MachineRepository
import com.example.slotanalyzer.domain.repository.MasterSyncRepository
import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMachineRepository(
        impl: MachineRepositoryImpl
    ): MachineRepository

    @Binds
    @Singleton
    abstract fun bindPlaySessionRepository(
        impl: PlaySessionRepositoryImpl
    ): PlaySessionRepository

    @Binds
    @Singleton
    abstract fun bindMasterSyncRepository(
        impl: MasterSyncRepositoryImpl
    ): MasterSyncRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingRepository(
        impl: AppSettingRepositoryImpl
    ): AppSettingRepository
}
