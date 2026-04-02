package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.JournalEntryEntity
import com.osnordev.abaco.data.local.JournalLineEntity
import com.osnordev.abaco.domain.model.AccountType
import com.osnordev.abaco.domain.model.PayrollCalculation
import com.osnordev.abaco.domain.validation.ValidationResult
import java.time.LocalDate
import javax.inject.Inject

/**
 * Genera y guarda automáticamente el asiento contable de una nómina.
 * Implementa la partida doble: Suma Débitos = Suma Créditos.
 *
 * Estructura del asiento:
 * DÉBITOS  (Gastos de la empresa = totalCompanyCost)
 *   Gasto de Sueldos              = grossSalary
 *   Gasto SS Patronal             = cssEmployer
 *   Gasto Vacaciones              = holidayProvision
 *   Gasto Subsidio                = subsidyProvision
 *   Gasto SS Especial             = specialSS
 *
 * CRÉDITOS (Obligaciones y pagos = totalCompanyCost)
 *   Sueldos por Pagar (Neto)      = netSalary
 *   Acreedores SS Empleado        = cssEmployee
 *   Acreedores IIP Retenido       = iipRetained
 *   Acreedores SS Patronal        = cssEmployer
 *   Acreedores Vacaciones         = holidayProvision
 *   Acreedores Subsidio           = subsidyProvision
 *   Acreedores SS Especial        = specialSS
 */
class CreateSalaryJournalEntryUseCase @Inject constructor(
    private val insertJournalEntryUseCase: InsertJournalEntryUseCase
) {
    suspend operator fun invoke(payroll: PayrollCalculation): ValidationResult {
        val date = LocalDate.now()
        val description = "Nómina: ${payroll.employeeName} (${payroll.ci}) - ${date.month} ${date.year}"

        val entry = JournalEntryEntity(
            date = date,
            description = description
        )

        val lines = mutableListOf<JournalLineEntity>()

        // ── DÉBITOS (Gastos para la empresa) ─────────────────────────────
        lines.add(line("Gasto de Sueldos", AccountType.EXPENSE, debit = payroll.grossSalary))
        lines.add(line("Gasto de SS para utilización de la fuerza de trabajo", AccountType.EXPENSE, debit = payroll.cssEmployer))
        lines.add(line("Gasto de Vacaciones", AccountType.EXPENSE, debit = payroll.holidayProvision))
        lines.add(line("Gasto de Subsidio", AccountType.EXPENSE, debit = payroll.subsidyProvision))
        lines.add(line("Gasto de SS Especial", AccountType.EXPENSE, debit = payroll.specialSS))

        // ── CRÉDITOS (Obligaciones y pagos) ──────────────────────────────
        // Salario neto que se le paga al trabajador
        lines.add(line("Sueldos por Pagar (Neto)", AccountType.LIABILITY, credit = payroll.netSalary))
        // Retenciones al empleado (SS + IIP) que la empresa debe entregar al Estado
        lines.add(line("Acreedores - SS Empleado", AccountType.LIABILITY, credit = payroll.cssEmployee))
        lines.add(line("Acreedores - IIP Retenido (ONAT)", AccountType.LIABILITY, credit = payroll.iipRetained))
        // Aportes patronales que la empresa debe pagar al Estado
        lines.add(line("Acreedores - SS fuerza de trabajo", AccountType.LIABILITY, credit = payroll.cssEmployer))
        lines.add(line("Acreedores - Vacaciones", AccountType.LIABILITY, credit = payroll.holidayProvision))
        lines.add(line("Acreedores - Subsidio", AccountType.LIABILITY, credit = payroll.subsidyProvision))
        lines.add(line("Acreedores - SS Especial", AccountType.LIABILITY, credit = payroll.specialSS))

        return insertJournalEntryUseCase(entry, lines)
    }

    private fun line(
        name: String,
        type: AccountType,
        debit: Double = 0.0,
        credit: Double = 0.0
    ) = JournalLineEntity(
        entryId = 0,
        accountName = name,
        accountType = type,
        debit = debit,
        credit = credit
    )
}
