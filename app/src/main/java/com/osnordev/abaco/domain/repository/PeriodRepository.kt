package com.osnordev.abaco.domain.repository

/**
 * Contrato para consultar el estado de los períodos contables.
 * Permite a los UseCases verificar si un período está cerrado sin depender
 * directamente del ViewModel.
 */
interface PeriodRepository {
    /** Devuelve true si el período (año/mes) está cerrado para el cliente activo. */
    suspend fun isPeriodClosed(year: Int, month: Int): Boolean
}
