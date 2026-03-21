package com.example.slotanalyzer.di

import com.example.slotanalyzer.data.repository.MachineInferenceSpecRepositoryImpl
import com.example.slotanalyzer.data.repository.MachineRepositoryImpl
import com.example.slotanalyzer.data.repository.PlayHistoryRepositoryImpl
import com.example.slotanalyzer.data.repository.PlaySessionRepositoryImpl
import com.example.slotanalyzer.domain.repository.MachineInferenceSpecRepository
import com.example.slotanalyzer.domain.repository.MachineRepository
import com.example.slotanalyzer.domain.repository.PlayHistoryRepository
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
    abstract fun bindPlayHistoryRepository(
        impl: PlayHistoryRepositoryImpl
    ): PlayHistoryRepository

    @Binds
    @Singleton
    abstract fun bindMachineInferenceSpecRepository(
        impl: MachineInferenceSpecRepositoryImpl
    ): MachineInferenceSpecRepository
}