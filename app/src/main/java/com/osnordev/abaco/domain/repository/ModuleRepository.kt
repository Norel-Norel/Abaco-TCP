package com.osnordev.abaco.domain.repository

import com.osnordev.abaco.domain.model.AppModule
import kotlinx.coroutines.flow.Flow

interface ModuleRepository {
    fun getModuleStates(): Flow<Map<AppModule, Boolean>>
    suspend fun setModuleEnabled(module: AppModule, enabled: Boolean)
}
