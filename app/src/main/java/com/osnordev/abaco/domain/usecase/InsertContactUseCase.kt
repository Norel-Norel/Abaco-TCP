package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.ContactEntity
import com.osnordev.abaco.data.repository.ContactRepository
import javax.inject.Inject

class InsertContactUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(entity: ContactEntity): Long = repository.insert(entity)
}
