package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.ChartOfAccountDao
import com.osnordev.abaco.data.local.ChartOfAccountEntity
import com.osnordev.abaco.domain.model.AccountType
import javax.inject.Inject

/**
 * Inicializa el plan de cuentas estándar TCP cubano para un cliente nuevo.
 * Se llama justo después de crear un cliente para que tenga su propio catálogo.
 */
class InitClientChartOfAccountsUseCase @Inject constructor(
    private val dao: ChartOfAccountDao
) {
    suspend operator fun invoke(clientId: Long) {
        val accounts = buildStandardChart(clientId)
        dao.insertAll(accounts)
    }

    private fun buildStandardChart(clientId: Long): List<ChartOfAccountEntity> = listOf(
        // ── Cuentas principales ──────────────────────────────────────────
        ChartOfAccountEntity("100", "Efectivo en Caja",        AccountType.ASSET,     "DEBIT",  null,    true, clientId),
        ChartOfAccountEntity("110", "Efectivo en Banco",       AccountType.ASSET,     "DEBIT",  null,    true, clientId),
        ChartOfAccountEntity("183", "Inventarios",             AccountType.ASSET,     "DEBIT",  null,    true, clientId),
        ChartOfAccountEntity("240", "Activos Fijos",           AccountType.ASSET,     "DEBIT",  null,    true, clientId),
        ChartOfAccountEntity("410", "Cuentas por Pagar",       AccountType.LIABILITY, "CREDIT", null,    true, clientId),
        ChartOfAccountEntity("600", "Patrimonio TCP",          AccountType.EQUITY,    "CREDIT", null,    true, clientId),
        ChartOfAccountEntity("800", "Gastos de Operación",     AccountType.EXPENSE,   "DEBIT",  null,    true, clientId),
        ChartOfAccountEntity("810", "Impuestos y Tasas",       AccountType.EXPENSE,   "DEBIT",  null,    true, clientId),
        ChartOfAccountEntity("900", "Ingresos por Ventas",     AccountType.INCOME,    "CREDIT", null,    true, clientId),
        ChartOfAccountEntity("920", "Ingresos Financieros",    AccountType.INCOME,    "CREDIT", null,    true, clientId),
        // ── Subcuentas ───────────────────────────────────────────────────
        ChartOfAccountEntity("11020",    "Bonificación caja extra",       AccountType.ASSET,   "DEBIT",  "110", true, clientId),
        ChartOfAccountEntity("80011000", "Materias primas",               AccountType.EXPENSE, "DEBIT",  "800", true, clientId),
        ChartOfAccountEntity("80050000", "Remuneración trabajadores",     AccountType.EXPENSE, "DEBIT",  "800", true, clientId),
        ChartOfAccountEntity("80080000", "Arrendamiento de espacios",     AccountType.EXPENSE, "DEBIT",  "800", true, clientId),
        ChartOfAccountEntity("80050100", "Servicio de Contabilidad",      AccountType.EXPENSE, "DEBIT",  "800", true, clientId),
        ChartOfAccountEntity("81010",    "Impuesto por ventas",           AccountType.EXPENSE, "DEBIT",  "810", true, clientId),
        ChartOfAccountEntity("81030",    "Impuesto fuerza trabajo",       AccountType.EXPENSE, "DEBIT",  "810", true, clientId),
        ChartOfAccountEntity("81040",    "Impuesto ingresos personales",  AccountType.EXPENSE, "DEBIT",  "810", true, clientId),
    )
}
