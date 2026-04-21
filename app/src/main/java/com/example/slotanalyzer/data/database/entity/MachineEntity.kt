package com.example.slotanalyzer.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "manufacturer") val manufacturer: String? = null,
    @ColumnInfo(name = "type") val type: String? = null,
    @ColumnInfo(name = "coin_unit") val coinUnit: Double? = null,
    @ColumnInfo(name = "release_date") val releaseDate: String? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "dmm_url") val dmmUrl: String? = null,
    @ColumnInfo(name = "ichigeki_url") val ichigekiUrl: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
