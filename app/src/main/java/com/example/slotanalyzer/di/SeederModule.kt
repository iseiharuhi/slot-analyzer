package com.example.slotanalyzer.di

import com.example.slotanalyzer.data.datasource.seeder.*
import com.example.slotanalyzer.data.datasource.source.LocalAssetMachineMasterSource
import com.example.slotanalyzer.data.datasource.source.MachineMasterSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SeederModule {

    @Binds
    @Singleton
    abstract fun bindMachineMasterSource(impl: LocalAssetMachineMasterSource): MachineMasterSource

    @Binds
    @Singleton
    abstract fun bindMachineMasterImporter(impl: MachineMasterImporterImpl): MachineMasterImporter

    @Binds
    @Singleton
    abstract fun bindMachineMasterSeeder(impl: MachineMasterSeederImpl): MachineMasterSeeder
}
