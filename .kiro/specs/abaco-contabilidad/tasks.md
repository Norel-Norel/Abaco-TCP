# Plan de Implementación — Ábaco Contabilidad

- [x] 1. Configurar estructura del proyecto y dependencias
  - Agregar dependencias en `libs.versions.toml` y `build.gradle.kts`: Room, Hilt, Compose Navigation, Kotlinx Serialization, Kotest, MPAndroidChart o Vico para gráficos
  - Configurar Hilt en el módulo de la app (`@HiltAndroidApp`)
  - Habilitar plugins de Kotlin Serialization y KSP
  - _Requisitos: 7.1, 4.6, 6.5_

- [ ] 2. Implementar modelos de datos y capa de dominio
  - [ ] 2.1 Crear modelos de dominio: `Transaction`, `TransactionType`, `IncomeCategory`, `ExpenseCategory`, `TaxConfig`, `TaxBracket`, `TaxResult`, `BracketDetail`, `AppModule`
    - Anotar con `@Serializable` donde corresponda
    - _Requisitos: 2.1, 4.3, 6.5, 7.4_
  - [ ]* 2.2 Escribir property test — Propiedad 1: Round-trip de serialización de transacciones
    - **Propiedad 1: Round-trip de serialización de transacciones**
    - **Valida: Requisito 7.4**
  - [ ]* 2.3 Escribir property test — Propiedad 2: Round-trip de configuración tributaria
    - **Propiedad 2: Round-trip de configuración tributaria**
    - **Valida: Requisitos 6.5, 4.6**

- [ ] 3. Implementar capa de datos (Room + DataStore)
  - [ ] 3.1 Crear entidad Room `TransactionEntity` y `TransactionDao` con queries por período (año/mes)
    - _Requisitos: 7.1, 7.2, 7.3_
  - [ ] 3.2 Crear `AppDatabase` con Room y configurar migraciones básicas
    - _Requisitos: 7.1_
  - [ ] 3.3 Implementar `TransactionRepositoryImpl` con `Flow` para observar cambios
    - _Requisitos: 2.3, 2.4, 7.2, 7.3_
  - [ ] 3.4 Implementar `TaxConfigRepository` usando DataStore para persistir `TaxConfig` serializado en JSON
    - _Requisitos: 6.3, 6.5_
  - [ ] 3.5 Implementar `ModuleRepository` usando DataStore para persistir estado de `AppModule`
    - _Requisitos: 8.1, 8.5_

- [ ] 4. Implementar lógica de cálculo tributario
  - [ ] 4.1 Crear `TaxCalculator` con funciones puras: `calculateCSS(grossIncome, cssRate)` y `calculateIIP(netIncome, brackets)`
    - Implementar escala progresiva cubana por defecto
    - _Requisitos: 4.1, 4.2, 4.3, 4.5_
  - [ ]* 4.2 Escribir property test — Propiedad 3: CSS siempre no negativa
    - **Propiedad 3: CSS siempre no negativa**
    - **Valida: Requisitos 4.1, 4.5**
  - [ ]* 4.3 Escribir property test — Propiedad 4: IIP con ingresos cero produce tributo cero
    - **Propiedad 4: IIP con ingresos cero produce tributo cero**
    - **Valida: Requisito 4.5**
  - [ ]* 4.4 Escribir property test — Propiedad 5: Escala progresiva del IIP es monótonamente creciente
    - **Propiedad 5: Escala progresiva del IIP es monótonamente creciente**
    - **Valida: Requisito 4.3**

- [ ] 5. Checkpoint — Asegurarse de que todos los tests pasan, consultar al usuario si surgen dudas.

- [ ] 6. Implementar casos de uso (Use Cases)
  - [ ] 6.1 Crear `GetTransactionsByPeriodUseCase`, `InsertTransactionUseCase`, `UpdateTransactionUseCase`, `DeleteTransactionUseCase`
    - _Requisitos: 2.3, 2.4, 7.3_
  - [ ] 6.2 Crear `GetTaxResultUseCase` que combine ingresos/gastos del período con `TaxCalculator`
    - _Requisitos: 4.1, 4.2, 4.4_
  - [ ] 6.3 Crear `ValidateTransactionUseCase` que rechace importes ≤ 0
    - _Requisitos: 2.2_
  - [ ]* 6.4 Escribir property test — Propiedad 6: Adición de transacción incrementa el total del período
    - **Propiedad 6: Adición de transacción incrementa el total del período**
    - **Valida: Requisitos 2.3, 2.4**
  - [ ]* 6.5 Escribir property test — Propiedad 7: Rechazo de transacciones con importe inválido
    - **Propiedad 7: Rechazo de transacciones con importe inválido**
    - **Valida: Requisito 2.2**

- [ ] 7. Implementar navegación y estructura de la app
  - [ ] 7.1 Configurar `NavHost` con `AnimatedNavHost` y rutas: `dashboard`, `transactions`, `transaction_form`, `taxes`, `settings`
    - Implementar transiciones `slideInHorizontally` / `slideOutHorizontally` con `spring` de baja amortiguación
    - _Requisitos: 1.1, 1.2, 1.5_
  - [ ] 7.2 Crear `BottomNavBar` con Material 3 que muestre/oculte destinos según módulos activos
    - _Requisitos: 1.1, 8.2_
  - [ ] 7.3 Crear `MainScaffold` que integre `BottomNavBar`, `TopAppBar` y el `NavHost`
    - _Requisitos: 1.3, 1.4_
  - [ ]* 7.4 Escribir property test — Propiedad 8: Módulo desactivado oculta sus rutas
    - **Propiedad 8: Módulo desactivado oculta sus rutas**
    - **Valida: Requisito 8.2**

- [ ] 8. Implementar pantalla de Transacciones
  - [ ] 8.1 Crear `TransactionViewModel` con `StateFlow` para lista de transacciones del período activo
    - _Requisitos: 2.3, 2.4, 7.2_
  - [ ] 8.2 Crear `TransactionListScreen` con listado agrupado, FAB para nueva transacción y soporte de edición/eliminación
    - _Requisitos: 2.7_
  - [ ] 8.3 Crear `TransactionFormScreen` con campos: tipo, importe, categoría, fecha, descripción; validación inline de importe
    - _Requisitos: 2.1, 2.2, 2.5, 2.6_

- [ ] 9. Implementar pantalla Dashboard
  - [ ] 9.1 Crear `DashboardViewModel` que calcule totales de ingresos, gastos y utilidad neta por período
    - _Requisitos: 3.1, 3.4_
  - [ ] 9.2 Crear `DashboardScreen` con tarjetas de resumen (ingresos, gastos, utilidad neta), selector de período y estado vacío
    - _Requisitos: 3.1, 3.5_
  - [ ] 9.3 Integrar gráfico de barras por categoría (ingresos vs gastos) usando Vico o MPAndroidChart
    - _Requisitos: 3.2_
  - [ ] 9.4 Integrar gráfico circular de distribución de gastos por categoría
    - _Requisitos: 3.3_

- [ ] 10. Implementar pantalla de Tributos
  - [ ] 10.1 Crear `TaxViewModel` que exponga `TaxResult` del período activo usando `GetTaxResultUseCase`
    - _Requisitos: 4.1, 4.2, 4.4_
  - [ ] 10.2 Crear `TaxScreen` con desglose de CSS e IIP: base de cálculo, tasa, importe por tramo
    - _Requisitos: 4.4, 4.5_

- [ ] 11. Implementar pantalla de Configuración
  - [ ] 11.1 Crear `SettingsViewModel` para gestionar `TaxConfig` y estado de módulos
    - _Requisitos: 6.1, 6.2, 6.3, 6.4, 8.1_
  - [ ] 11.2 Crear `SettingsScreen` con sección de módulos (switches) y sección de configuración tributaria (tasa CSS + tramos IIP editables)
    - _Requisitos: 6.1, 6.2, 6.4, 8.1, 8.2, 8.4_
  - [ ] 11.3 Implementar validación de tramos IIP consecutivos y tasa CSS en rango [0,1]
    - _Requisitos: 6.2, 6.4_
  - [ ]* 11.4 Escribir property test — Propiedad 9: Reactivación de módulo restaura datos
    - **Propiedad 9: Reactivación de módulo restaura datos**
    - **Valida: Requisito 8.3**

- [ ] 12. Aplicar tema Material 3 consistente
  - Definir `AppTheme` con paleta de colores, tipografía y formas de Material 3
  - Aplicar el tema en todas las pantallas
  - _Requisitos: 1.4_

- [ ] 13. Checkpoint final — Asegurarse de que todos los tests pasan, consultar al usuario si surgen dudas.
