package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.SalaryDetails
import com.osnordev.abaco.domain.model.TaxConfig
import javax.inject.Inject

/**
 * Caso de uso para calcular el salario neto y desglose de impuestos.
 */
class CalculateSalaryUseCase @Inject constructor() {

    /**
     * @param grossSalary El salario bruto mensual.
     * @param taxConfig Configuración de impuestos a aplicar.
     * @return [SalaryDetails] con el desglose calculado.
     */
    operator fun invoke(grossSalary: Double, taxConfig: TaxConfig): SalaryDetails {
        // 1. Calcular Seguridad Social (CSS)
        val socialSecurity = grossSalary * taxConfig.cssRate

        // 2. Calcular Base para el Impuesto (Normalmente Gross - CSS)
        val taxableIncome = grossSalary - socialSecurity

        // 3. Calcular Impuesto Sobre la Renta (IIP/IRPF) usando tramos
        var incomeTax = 0.0
        taxConfig.iipBrackets.forEach { bracket ->
            if (taxableIncome > bracket.from) {
                val upperLimit = bracket.to ?: Double.MAX_VALUE
                val taxableInThisBracket = minOf(taxableIncome, upperLimit) - bracket.from
                if (taxableInThisBracket > 0) {
                    incomeTax += taxableInThisBracket * bracket.rate
                }
            }
        }

        // 4. Calcular Salario Neto
        val netSalary = grossSalary - socialSecurity - incomeTax

        return SalaryDetails(
            grossSalary = grossSalary,
            socialSecurity = socialSecurity,
            incomeTax = incomeTax,
            netSalary = netSalary
        )
    }
}
