
package com.example.wildlife_app.data.repository

import com.example.wildlife_app.data.local.dao.OutingDao
import com.example.wildlife_app.data.local.entity.OutingEntity
import kotlinx.coroutines.flow.Flow

class OutingRepository(
    private val outingDao: OutingDao
) {

    fun observeAllOutings(): Flow<List<OutingEntity>> {
        return outingDao.getAll()
    }

    suspend fun getOutingById(outingId: Long): OutingEntity? {
        return outingDao.getById(outingId)
    }

    suspend fun saveOuting(outing: OutingEntity): Long {
        return outingDao.upsert(outing)
    }

    suspend fun deleteOuting(outing: OutingEntity) {
        outingDao.delete(outing)
    }
}
