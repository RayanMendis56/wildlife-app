
package com.example.wildlife_app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.wildlife_app.data.local.entity.OutingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutingDao {

    @Upsert
    suspend fun upsert(outing: OutingEntity): Long

    @Delete
    suspend fun delete(outing: OutingEntity)

    @Query("SELECT * FROM outings ORDER BY date DESC")
    fun getAll(): Flow<List<OutingEntity>>

    @Query("SELECT * FROM outings WHERE id = :outingId")
    suspend fun getById(outingId: Long): OutingEntity?
}
