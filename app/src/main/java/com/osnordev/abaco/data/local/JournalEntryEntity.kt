package com.osnordev.abaco.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.osnordev.abaco.domain.model.AccountType
import com.osnordev.abaco.domain.model.SyncStatus
import java.time.LocalDate

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING.name
)

@Entity(
    tableName = "journal_lines",
    foreignKeys = [ForeignKey(
        entity = JournalEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("entryId")]
)
data class JournalLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val accountName: String,
    val accountType: AccountType,
    val debit: Double = 0.0,
    val credit: Double = 0.0
)

/** Convenience class for Room's @Relation query */
data class JournalEntryWithLines(
    @Embedded val entry: JournalEntryEntity,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val lines: List<JournalLineEntity>
)
