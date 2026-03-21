package com.example.slotanalyzer.data.database.entity

import androidx.room.*

@Entity(
    tableName = "machine_counter_definitions",
    foreignKeys = [
        ForeignKey(
            entity = MachineEntity::class,
            parentColumns = ["id"],
            childColumns = ["machine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["machine_id"]), Index(value = ["machine_id", "key"], unique = true)]
)
data class MachineCounterDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "machine_id") val machineId: String,
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "input_type") val inputType: String,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "is_default_visible") val isDefaultVisible: Boolean = true,
    @ColumnInfo(name = "supports_minus") val supportsMinus: Boolean = false,
    @ColumnInfo(name = "notes") val notes: String? = null
)
