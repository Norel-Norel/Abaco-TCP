package com.osnordev.abaco.domain.model

import kotlinx.serialization.Serializable

/**
 * Configuración de tributos y aportes según las regulaciones cubanas (ONAT y Código de Trabajo).
 */
@Serializable
data class TaxConfig(
    // --- TCP (Trabajador por Cuenta Propia) ---
    /** Contribución a la Seguridad Social del TCP - Habitualmente 20% */
    val cssRate: Double = 0.20,

    /** Escala Progresiva del Impuesto sobre los Ingresos Personales (IIP) */
    val iipBrackets: List<TaxBracket> = defaultIipBrackets(),

    // --- Retenciones al Empleado (Nómina) ---
    /** Contribución Especial a la Seguridad Social (Trabajador) - Habitualmente 5% */
    val cssEmployeeRate: Double = 0.05,

    // --- Aportes Patronales (Costo de la Empresa) ---
    /** Contribución a la Seguridad Social (Patronal) - Habitualmente 12.5% */
    val cssEmployerRate: Double = 0.125,

    /** Provisión para Vacaciones - Habitualmente 9.09% */
    val holidayRate: Double = 0.0909,

    /** Provisión para Subsidios - Habitualmente 1.5% */
    val subsidyRate: Double = 0.015,

    /** Contribución Especial a la Seguridad Social (Riesgos/Especial) - Habitualmente 5% */
    val specialSSRate: Double = 0.05
)

/**
 * Escala progresiva del IIP (ONAT) vigente.
 */
fun defaultIipBrackets(): List<TaxBracket> = listOf(
    TaxBracket(from = 0.0,      to = 3250.0,    rate = 0.00),
    TaxBracket(from = 3250.0,   to = 9510.0,    rate = 0.03),
    TaxBracket(from = 9510.0,   to = 15000.0,   rate = 0.05),
    TaxBracket(from = 15000.0,  to = 20000.0,   rate = 0.075),
    TaxBracket(from = 20000.0,  to = 25000.0,   rate = 0.10),
    TaxBracket(from = 25000.0,  to = 30000.0,   rate = 0.15),
    TaxBracket(from = 30000.0,  to = null,      rate = 0.20)
)
