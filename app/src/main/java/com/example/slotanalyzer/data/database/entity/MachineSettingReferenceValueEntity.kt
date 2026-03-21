package com.example.slotanalyzer.data.database.entity

import androidx.room.*

@Entity(
    tableName = "machine_setting_reference_values",
    foreignKeys = [
        ForeignKey(
            entity = MachineEntity::class,
            parentColumns = ["id"],
            childColumns = ["machine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["machine_id"]), Index(value = ["machine_id", "counter_key", "setting_no"], unique = true)]
)
data class MachineSettingReferenceValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "machine_id") val machineId: String,
    @ColumnInfo(name = "counter_key") val counterKey: String,
    @ColumnInfo(name = "setting_no") val settingNo: Int,
    @ColumnInfo(name = "denominator_value") val denominatorValue: Double? = null,
    @ColumnInfo(name = "numerator_value") val numeratorValue: Double? = null,
    @ColumnInfo(name = "weight") val weight: Double = 1.0,
    @ColumnInfo(name = "min_sample_size") val minSampleSize: Int? = null,
    @ColumnInfo(name = "note") val note: String? = null
)
