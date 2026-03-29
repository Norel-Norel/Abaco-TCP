package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.ContactEntity
import com.osnordev.abaco.data.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchContactsUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    operator fun invoke(query: String): Flow<List<ContactEntity>> =
        if (query.isBlank()) repository.getAll() else repository.search(query)
}
