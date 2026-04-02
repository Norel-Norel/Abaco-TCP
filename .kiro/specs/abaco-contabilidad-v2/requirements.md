# Documento de Requisitos — Ábaco Contabilidad v2

## Introducción

Este documento consolida todos los requisitos de Ábaco Contabilidad, incluyendo los de la v1 (ya implementados) y los nuevos de la v2. La v2 extiende la app con:
1. **Partida doble**: registro contable formal con cuentas de débito y crédito.
2. **Balance General**: estado financiero estructurado, exportable a PDF.
3. **Notificaciones de vencimientos**: pagos con fecha límite y notificaciones push via AlarmManager.
4. **Sincronización con Supabase**: sincronización bidireccional offline-first con resolución de conflictos.
5. **Mejoras de UI**: iconos Material Extended, drawer lateral derecho para configuración, tema claro/oscuro, botón de gráficos en barra inferior.
6. **Reportes avanzados**: exportación CSV/Excel, flujo de caja, proyección tributaria, comparativa entre períodos.
7. **Presupuestos por categoría**: límites mensuales con alertas de consumo.
8. **Clientes y proveedores**: catálogo básico de contactos asociados a transacciones.
9. **Facturas y recibos**: adjuntar foto de comprobante a una transacción.
10. **Seguridad**: PIN de acceso y cifrado de base de datos local con SQLCipher.
11. **Búsqueda y filtros avanzados**: filtrar transacciones por categoría, rango de fechas e importe.
12. **Transacciones recurrentes**: programar repetición automática de transacciones periódicas.
13. **Widget de Android**: acceso rápido desde la pantalla de inicio para registrar transacciones.
14. **Onboarding**: pantallas de bienvenida para usuarios nuevos.
15. **Multimoneda**: soporte para CUP, MLC y USD con tipo de cambio configurable.
16. **Código QR de cobro**: generar QR con número de cuenta y teléfono del TCP para facilitar pagos.
17. **Módulo de Inventario**: gestión de productos con control de stock, movimientos (entrada/salida/ajuste) y alertas de stock mínimo.
18. **Nómina**: cálculo de salario neto, retenciones del empleado y aportes patronales según normas cubanas, con generación automática de asiento contable.

## Glosario

- **TCP**: Trabajador por Cuenta Propia. Persona natural autorizada a ejercer actividades económicas de forma independiente en Cuba.
- **ONAT**: Oficina Nacional de Administración Tributaria.
- **Ingreso**: Entrada de dinero derivada de la actividad económica del TCP.
- **Gasto**: Salida de dinero asociada a la actividad económica del TCP.
- **Utilidad Neta**: Diferencia entre los ingresos totales y los gastos deducibles en un período fiscal.
- **IIP**: Impuesto sobre Ingresos Personales. Tributo aplicado a la utilidad neta según escala progresiva cubana.
- **CSS**: Contribución a la Seguridad Social. Aporte obligatorio calculado sobre los ingresos brutos.
- **Período Fiscal**: Mes calendario utilizado como unidad de reporte y cálculo tributario.
- **Partida Doble**: Principio contable donde cada transacción afecta al menos dos cuentas: un débito y un crédito de igual importe.
- **Cuenta Contable**: Clasificación que agrupa movimientos del mismo tipo (ej. Caja, Banco, Cuentas por Pagar).
- **Débito**: Cargo a una cuenta. Aumenta activos y gastos; disminuye pasivos y patrimonio.
- **Crédito**: Abono a una cuenta. Aumenta pasivos y patrimonio; disminuye activos y gastos.
- **Asiento Contable**: Registro de una transacción en partida doble, compuesto por líneas de débito y crédito que suman igual.
- **Balance General**: Estado financiero que muestra Activos, Pasivos y Patrimonio en una fecha determinada.
- **Balance de Comprobación**: Listado de todas las cuentas con sus saldos deudores y acreedores.
- **Activo**: Recursos controlados por el TCP (Caja, Banco, Inventario, Cuentas por Cobrar).
- **Pasivo**: Obligaciones del TCP (Cuentas por Pagar, Préstamos).
- **Patrimonio**: Diferencia entre Activos y Pasivos (Capital, Utilidad del período).
- **Vencimiento**: Fecha límite en que un pago o cobro debe realizarse.
- **AlarmManager**: API de Android para programar alarmas exactas que disparan notificaciones.
- **Supabase**: Plataforma backend-as-a-service con base de datos PostgreSQL y API REST.
- **Sincronización Diferida**: Estrategia que acumula cambios locales y los envía al servidor cuando hay conexión disponible.
- **Conflicto de Sincronización**: Situación donde el mismo registro fue modificado localmente y en el servidor desde la última sincronización.
- **Drawer**: Panel lateral deslizable que contiene opciones de configuración de la aplicación.
- **Dashboard**: Pantalla principal que muestra el resumen financiero mensual con gráficos.
- **Sistema**: La aplicación móvil Ábaco.
- **Usuario**: El TCP o emprendedor que utiliza la aplicación.
- **TaxConfig**: Modelo de configuración tributaria con dos grupos de tasas: `cssRate` (CSS del TCP autónomo, 20% por defecto) y `cssEmployeeRate`/`cssEmployerRate` (retenciones de nómina, 5% y 12.5% respectivamente).

---

## Requisitos — v1 (ya implementados)

### Requisito 1 — Diseño visual y navegación

**User Story:** Como usuario, quiero una interfaz con Material 3 y navegación fluida tipo iOS, para que la experiencia de uso sea intuitiva y agradable.

#### Criterios de Aceptación

1. THE Sistema SHALL implementar una barra de navegación inferior con al menos 3 destinos principales: Dashboard, Transacciones y Tributos.
2. WHEN el usuario selecciona una pestaña de navegación, THE Sistema SHALL mostrar la pantalla correspondiente con una animación de transición deslizante horizontal.
3. WHILE el usuario navega entre pantallas, THE Sistema SHALL mantener el estado de cada pantalla sin reinicializarla.
4. THE Sistema SHALL aplicar el sistema de colores, tipografía y formas de Material 3 de forma consistente en todas las pantallas.
5. WHEN el usuario realiza un gesto de deslizamiento horizontal, THE Sistema SHALL navegar entre pantallas adyacentes de forma fluida.

---

### Requisito 2 — Registro de transacciones

**User Story:** Como usuario, quiero registrar mis ingresos y gastos con categoría, fecha e importe, para llevar un control detallado de mi actividad económica.

#### Criterios de Aceptación

1. WHEN el usuario abre la pantalla de nueva transacción, THE Sistema SHALL presentar un formulario con campos: tipo (ingreso/gasto), importe, categoría, fecha y descripción opcional.
2. WHEN el usuario intenta guardar una transacción con importe vacío o igual a cero, THE Sistema SHALL rechazar la operación y mostrar un mensaje de error descriptivo.
3. WHEN el usuario guarda una transacción válida, THE Sistema SHALL persistir la transacción en almacenamiento local y reflejarla inmediatamente en el listado.
4. WHEN el usuario guarda una transacción válida, THE Sistema SHALL actualizar los totales del período fiscal correspondiente de forma inmediata.
5. THE Sistema SHALL ofrecer las siguientes categorías de ingresos: Ventas, Servicios, Arrendamiento, Otros.
6. THE Sistema SHALL ofrecer las siguientes categorías de gastos: Materias primas, Transporte, Servicios públicos, Otros.
7. WHEN el usuario selecciona una transacción existente, THE Sistema SHALL permitir editarla o eliminarla.

---

### Requisito 3 — Dashboard de resumen financiero

**User Story:** Como usuario, quiero ver un resumen visual de mis ingresos y gastos mensuales en gráficos, para entender rápidamente mi situación financiera.

#### Criterios de Aceptación

1. WHEN el usuario abre el Dashboard, THE Sistema SHALL mostrar el total de ingresos, total de gastos y utilidad neta del período fiscal activo.
2. THE Sistema SHALL mostrar un gráfico de barras comparando ingresos y gastos por categoría en el período fiscal activo.
3. THE Sistema SHALL mostrar un gráfico circular con la distribución porcentual de los gastos por categoría.
4. WHEN el usuario selecciona un período fiscal diferente, THE Sistema SHALL actualizar todos los gráficos y totales para reflejar el período seleccionado.
5. WHEN no existen transacciones en el período fiscal activo, THE Sistema SHALL mostrar un estado vacío con un mensaje orientativo al usuario.

---

### Requisito 4 — Cálculo de tributos ONAT

**User Story:** Como usuario, quiero que la aplicación calcule automáticamente mis obligaciones tributarias con la ONAT, para cumplir con la legislación cubana sin errores.

#### Criterios de Aceptación

1. WHEN el usuario accede a la pantalla de Tributos, THE Sistema SHALL calcular y mostrar la CSS aplicando el 20% sobre los ingresos brutos del período fiscal.
2. WHEN el usuario accede a la pantalla de Tributos, THE Sistema SHALL calcular el IIP aplicando la escala progresiva cubana vigente sobre la utilidad neta anual estimada.
3. THE Sistema SHALL aplicar la siguiente escala progresiva para el IIP: hasta 10 000 CUP — exento; de 10 001 a 20 000 CUP — 15%; de 20 001 a 30 000 CUP — 20%; de 30 001 a 50 000 CUP — 30%; más de 50 000 CUP — 50%.
4. WHEN el usuario accede a la pantalla de Tributos, THE Sistema SHALL mostrar el desglose detallado de cada tributo con su base de cálculo, tasa aplicada e importe resultante.
5. WHEN los ingresos brutos del período son iguales a cero, THE Sistema SHALL mostrar los tributos calculados como cero sin generar errores.

---

### Requisito 5 — Configuración tributaria

**User Story:** Como usuario, quiero poder ajustar las tasas y tramos tributarios desde la app, para adaptarme a cambios en la legislación cubana.

#### Criterios de Aceptación

1. THE Sistema SHALL proveer una pantalla de configuración accesible desde el menú principal.
2. WHEN el usuario modifica un tramo o tasa del IIP, THE Sistema SHALL validar que los tramos sean consecutivos y las tasas estén entre 0% y 100%.
3. WHEN el usuario guarda la configuración tributaria, THE Sistema SHALL persistir los nuevos valores y recalcular los tributos del período activo de forma inmediata.
4. WHEN el usuario modifica la tasa de CSS, THE Sistema SHALL validar que el valor esté entre 0% y 100% antes de persistirlo.
5. THE Sistema SHALL serializar y deserializar la configuración tributaria usando JSON para su persistencia local.

---

### Requisito 6 — Persistencia de datos

**User Story:** Como usuario, quiero que mis datos se guarden localmente en el dispositivo, para acceder a ellos sin conexión a internet.

#### Criterios de Aceptación

1. THE Sistema SHALL almacenar todas las transacciones en una base de datos local SQLite mediante Room.
2. WHEN la aplicación se cierra y se vuelve a abrir, THE Sistema SHALL restaurar todas las transacciones y el estado de la aplicación sin pérdida de datos.
3. WHEN el usuario elimina una transacción, THE Sistema SHALL eliminarla de la base de datos local y actualizar los totales del período afectado de forma inmediata.
4. THE Sistema SHALL serializar y deserializar las transacciones usando JSON para operaciones de exportación e importación.

---

### Requisito 7 — Módulos activables

**User Story:** Como usuario, quiero activar o desactivar módulos de funcionalidad según mis necesidades, para mantener la app simple y relevante a mi tipo de negocio.

#### Criterios de Aceptación

1. THE Sistema SHALL proveer una lista de módulos opcionales que el usuario puede activar o desactivar desde la pantalla de configuración.
2. WHEN el usuario desactiva un módulo, THE Sistema SHALL ocultar las pantallas, opciones de menú y funcionalidades asociadas a ese módulo sin eliminar los datos existentes.
3. WHEN el usuario reactiva un módulo previamente desactivado, THE Sistema SHALL restaurar todas las pantallas y datos asociados a ese módulo de forma inmediata.
4. THE Sistema SHALL incluir los siguientes módulos en la versión inicial: Tributos ONAT, Gráficos del Dashboard, Configuración tributaria.
5. THE Sistema SHALL persistir el estado de activación de cada módulo en almacenamiento local.

---

## Requisitos — v2 (nuevos)

### Requisito 8 — Iconos Material Extended

**User Story:** Como usuario, quiero una interfaz con iconos consistentes y expresivos en todas las pantallas, para identificar rápidamente las funciones de la app.

#### Criterios de Aceptación

1. THE Sistema SHALL usar la librería Material Icons Extended para todos los iconos de la interfaz.
2. THE Sistema SHALL usar iconos semánticamente apropiados en la barra de navegación inferior, botones de acción, formularios y tarjetas de resumen.
3. WHEN se muestran tipos de transacción, THE Sistema SHALL distinguir visualmente ingresos y gastos mediante iconos de color diferenciado.

---

### Requisito 9 — Registro en Partida Doble

**User Story:** Como usuario, quiero registrar mis transacciones usando el principio de partida doble, para llevar una contabilidad formal con cuentas de débito y crédito.

#### Criterios de Aceptación

1. WHEN el usuario crea un asiento contable, THE Sistema SHALL requerir al menos una línea de débito y una línea de crédito, y validar que la suma de débitos sea igual a la suma de créditos antes de persistir.
2. WHEN el usuario guarda un asiento válido, THE Sistema SHALL persistir el asiento con todas sus líneas en almacenamiento local y reflejarlo en el listado de asientos de forma inmediata.
3. THE Sistema SHALL proveer las siguientes cuentas contables predeterminadas: Activo (Caja, Banco, Cuentas por Cobrar, Inventario), Pasivo (Cuentas por Pagar, Préstamos), Patrimonio (Capital, Utilidad del Período), Ingresos (Ventas, Servicios), Gastos (Materias Primas, Transporte, Servicios Públicos, Otros).
4. WHEN el usuario intenta guardar un asiento con débitos distintos a créditos, THE Sistema SHALL rechazar la operación y mostrar el importe de la diferencia no cuadrada.
5. THE Sistema SHALL serializar y deserializar los asientos contables usando JSON para persistencia local y sincronización.

---

### Requisito 10 — Balance General y Balance de Comprobación

**User Story:** Como usuario, quiero ver un Balance General generado automáticamente y poder exportar el Balance de Comprobación en PDF, para conocer y compartir mi situación financiera.

#### Criterios de Aceptación

1. WHEN el usuario accede a la pantalla de Balance General, THE Sistema SHALL calcular y mostrar el total de Activos, Pasivos y Patrimonio acumulados hasta la fecha de corte seleccionada.
2. THE Sistema SHALL verificar que Activos = Pasivos + Patrimonio y mostrar un indicador visual de si el balance cuadra.
3. WHEN el usuario selecciona una fecha de corte, THE Sistema SHALL recalcular el Balance General considerando solo los asientos con fecha anterior o igual a la fecha seleccionada.
4. THE Sistema SHALL mostrar el desglose por cuenta dentro de cada sección (Activos, Pasivos, Patrimonio).
5. WHEN el usuario solicita exportar el Balance de Comprobación, THE Sistema SHALL generar un archivo PDF con el listado de cuentas, saldos deudores, saldos acreedores y totales, y ofrecerlo para compartir o guardar.
6. WHEN no existen asientos registrados, THE Sistema SHALL mostrar el Balance General con todos los totales en cero y un mensaje orientativo.

---

### Requisito 11 — Notificaciones de Vencimientos

**User Story:** Como usuario, quiero registrar pagos con fecha de vencimiento y recibir notificaciones push cuando se acerquen o venzan, para no perder obligaciones de pago.

#### Criterios de Aceptación

1. WHEN el usuario registra un pago pendiente, THE Sistema SHALL permitir asociarle una fecha de vencimiento, un importe y una descripción.
2. WHEN se registra un pago con fecha de vencimiento futura, THE Sistema SHALL programar una notificación push mediante AlarmManager para 1 día antes del vencimiento y para el día del vencimiento.
3. WHEN el usuario marca un pago como pagado, THE Sistema SHALL cancelar las notificaciones pendientes asociadas a ese pago y actualizar su estado en almacenamiento local de forma inmediata.
4. WHEN la aplicación se reinicia, THE Sistema SHALL reprogramar todas las notificaciones de pagos pendientes cuya fecha de vencimiento sea posterior a la fecha actual.
5. WHEN una notificación es disparada, THE Sistema SHALL mostrar el nombre del pago, el importe y la fecha de vencimiento en el contenido de la notificación.
6. THE Sistema SHALL persistir los pagos pendientes en base de datos local con su estado (pendiente/pagado) y fecha de vencimiento.

---

### Requisito 12 — Sincronización con Supabase

**User Story:** Como usuario, quiero que mis datos se sincronicen con Supabase de forma bidireccional y con soporte offline, para acceder a ellos desde múltiples dispositivos y no perder información sin conexión.

#### Criterios de Aceptación

1. WHILE el dispositivo tiene conexión a internet, THE Sistema SHALL sincronizar automáticamente las transacciones, asientos y pagos pendientes con Supabase en segundo plano.
2. WHEN el dispositivo no tiene conexión, THE Sistema SHALL guardar todos los cambios localmente y marcarlos como pendientes de sincronización.
3. WHEN el dispositivo recupera la conexión, THE Sistema SHALL sincronizar los cambios pendientes con Supabase de forma diferida sin interrumpir la experiencia del usuario.
4. WHEN existe un conflicto entre un registro local y uno remoto, THE Sistema SHALL aplicar la estrategia "el más reciente gana" basada en el campo `updatedAt` y notificar al usuario si el conflicto no puede resolverse automáticamente.
5. THE Sistema SHALL sincronizar de forma diferida, agrupando cambios en lotes para reducir el consumo de batería y datos móviles.
6. WHEN la sincronización falla por error de red, THE Sistema SHALL reintentar con retroceso exponencial hasta un máximo de 3 intentos antes de marcar el lote como fallido.

---

### Requisito 13 — Mejoras de UI

**User Story:** Como usuario, quiero una interfaz mejorada con acceso rápido a gráficos, configuración en un panel lateral y soporte de tema claro/oscuro, para una experiencia más cómoda y personalizada.

#### Criterios de Aceptación

1. THE Sistema SHALL mostrar un botón de gráficos en la barra de navegación inferior que lleve directamente a la pantalla de gráficos del Dashboard.
2. THE Sistema SHALL implementar un drawer lateral deslizable desde el lado derecho de la pantalla que contenga las opciones de configuración de la aplicación.
3. WHEN el usuario abre el drawer de configuración, THE Sistema SHALL mostrar un interruptor para alternar entre tema claro y tema oscuro.
4. WHEN el usuario cambia el tema, THE Sistema SHALL aplicar el nuevo tema de forma inmediata en toda la aplicación y persistir la preferencia en almacenamiento local.
5. WHILE el drawer de configuración está abierto, THE Sistema SHALL mantener el contenido principal visible pero atenuado en el fondo.

---

### Requisito 14 — Reportes avanzados y exportación

**User Story:** Como usuario, quiero exportar mis datos en múltiples formatos y ver reportes de flujo de caja y proyecciones tributarias, para analizar mi negocio y compartir información con terceros.

#### Criterios de Aceptación

1. WHEN el usuario solicita exportar transacciones, THE Sistema SHALL generar un archivo CSV con todas las transacciones del período seleccionado y ofrecerlo para compartir o guardar.
2. WHEN el usuario solicita exportar en formato Excel, THE Sistema SHALL generar un archivo XLSX con las transacciones organizadas por categoría y período.
3. WHEN el usuario accede al reporte de flujo de caja, THE Sistema SHALL mostrar los ingresos y gastos agrupados por mes en un gráfico de líneas con tendencia de los últimos 12 meses.
4. WHEN el usuario accede a la proyección tributaria, THE Sistema SHALL calcular y mostrar la estimación de CSS e IIP anuales basada en el promedio mensual de ingresos del período actual.
5. WHEN el usuario accede a la comparativa de períodos, THE Sistema SHALL mostrar la variación porcentual de ingresos, gastos y utilidad neta entre el período actual y el período anterior.

---

### Requisito 15 — Presupuestos por categoría

**User Story:** Como usuario, quiero definir presupuestos mensuales por categoría de gasto y recibir alertas cuando me acerque al límite, para controlar mejor mis gastos.

#### Criterios de Aceptación

1. WHEN el usuario define un presupuesto para una categoría, THE Sistema SHALL persistir el importe límite mensual asociado a esa categoría.
2. WHEN el total de gastos de una categoría supera el 80% del presupuesto definido, THE Sistema SHALL mostrar una alerta visual en el Dashboard indicando la categoría y el porcentaje consumido.
3. WHEN el total de gastos de una categoría supera el 100% del presupuesto, THE Sistema SHALL mostrar el indicador de esa categoría en color de error y notificar al usuario.
4. WHEN el usuario accede al Dashboard, THE Sistema SHALL mostrar el progreso de cada presupuesto activo como una barra de progreso con el importe gastado y el límite.
5. WHEN comienza un nuevo mes, THE Sistema SHALL reiniciar el contador de gastos de cada presupuesto manteniendo los límites configurados.

---

### Requisito 16 — Clientes y Proveedores

**User Story:** Como usuario, quiero mantener un catálogo de clientes y proveedores y asociarlos a mis transacciones, para saber con quién hago negocios y cuánto les debo o me deben.

#### Criterios de Aceptación

1. WHEN el usuario crea un contacto, THE Sistema SHALL persistir nombre, teléfono, tipo (cliente/proveedor) y notas opcionales.
2. WHEN el usuario registra una transacción, THE Sistema SHALL permitir asociarla opcionalmente a un contacto del catálogo.
3. WHEN el usuario consulta un contacto, THE Sistema SHALL mostrar el historial de transacciones asociadas y el saldo neto acumulado con ese contacto.
4. WHEN el usuario elimina un contacto, THE Sistema SHALL desvincular las transacciones asociadas sin eliminarlas.
5. THE Sistema SHALL permitir buscar contactos por nombre o teléfono en el catálogo.

---

### Requisito 17 — Facturas y Recibos

**User Story:** Como usuario, quiero adjuntar fotos de comprobantes a mis transacciones, para tener respaldo documental de mis operaciones.

#### Criterios de Aceptación

1. WHEN el usuario edita o crea una transacción, THE Sistema SHALL permitir adjuntar una imagen desde la cámara o la galería del dispositivo.
2. WHEN el usuario adjunta una imagen, THE Sistema SHALL comprimir la imagen a un tamaño máximo de 500 KB antes de persistirla localmente.
3. WHEN el usuario visualiza una transacción con comprobante adjunto, THE Sistema SHALL mostrar una miniatura de la imagen y permitir verla en pantalla completa.
4. WHEN el usuario elimina el comprobante de una transacción, THE Sistema SHALL eliminar el archivo de imagen del almacenamiento local.
5. IF el dispositivo no tiene cámara disponible, THEN THE Sistema SHALL ofrecer únicamente la opción de seleccionar desde la galería.

---

### Requisito 18 — Seguridad: PIN de acceso y cifrado

**User Story:** Como usuario, quiero proteger la app con un PIN y tener mis datos cifrados, para que nadie pueda acceder a mi información financiera sin autorización.

#### Criterios de Aceptación

1. WHEN el usuario activa el PIN de acceso por primera vez, THE Sistema SHALL solicitar que ingrese y confirme un PIN de 4 a 6 dígitos.
2. WHEN el PIN está activado y el usuario abre la app, THE Sistema SHALL mostrar la pantalla de ingreso de PIN antes de permitir el acceso.
3. WHEN el usuario ingresa un PIN incorrecto 5 veces consecutivas, THE Sistema SHALL bloquear el acceso durante 30 segundos antes de permitir un nuevo intento.
4. THE Sistema SHALL cifrar la base de datos local usando SQLCipher con una clave derivada del PIN del usuario.
5. WHEN el usuario desactiva el PIN, THE Sistema SHALL descifrar la base de datos y eliminar la clave de cifrado del almacenamiento seguro.

---

### Requisito 19 — Búsqueda y filtros avanzados

**User Story:** Como usuario, quiero buscar y filtrar mis transacciones por múltiples criterios, para encontrar rápidamente cualquier operación.

#### Criterios de Aceptación

1. WHEN el usuario activa la búsqueda en el listado de transacciones, THE Sistema SHALL filtrar en tiempo real por descripción o categoría mientras el usuario escribe.
2. WHEN el usuario aplica un filtro por rango de fechas, THE Sistema SHALL mostrar únicamente las transacciones cuya fecha esté dentro del rango seleccionado.
3. WHEN el usuario aplica un filtro por importe, THE Sistema SHALL permitir definir un importe mínimo, máximo o ambos, y mostrar solo las transacciones que cumplan el criterio.
4. WHEN el usuario aplica un filtro por categoría, THE Sistema SHALL mostrar únicamente las transacciones de la categoría seleccionada.
5. WHEN el usuario combina múltiples filtros, THE Sistema SHALL aplicar todos los criterios simultáneamente con lógica AND.
6. WHEN el usuario limpia los filtros, THE Sistema SHALL restaurar el listado completo del período activo.

---

### Requisito 20 — Transacciones recurrentes

**User Story:** Como usuario, quiero programar transacciones que se repitan automáticamente, para no tener que registrar manualmente gastos o ingresos periódicos.

#### Criterios de Aceptación

1. WHEN el usuario crea una transacción recurrente, THE Sistema SHALL permitir seleccionar la frecuencia: diaria, semanal, quincenal o mensual, y la fecha de inicio.
2. WHEN llega la fecha programada de una transacción recurrente, THE Sistema SHALL crear automáticamente la transacción en la base de datos local con los mismos datos de la plantilla.
3. WHEN el usuario edita una transacción recurrente, THE Sistema SHALL preguntar si el cambio aplica solo a la ocurrencia actual o a todas las futuras.
4. WHEN el usuario elimina una transacción recurrente, THE Sistema SHALL preguntar si elimina solo la ocurrencia actual o cancela todas las futuras.
5. THE Sistema SHALL mostrar un indicador visual en el listado para distinguir las transacciones generadas automáticamente de las manuales.

---

### Requisito 21 — Widget de Android

**User Story:** Como usuario, quiero un widget en la pantalla de inicio para registrar transacciones rápidamente sin abrir la app completa.

#### Criterios de Aceptación

1. THE Sistema SHALL proveer un widget de Android que muestre el saldo del período actual (ingresos, gastos, utilidad neta).
2. WHEN el usuario pulsa el botón de acción rápida del widget, THE Sistema SHALL abrir directamente el formulario de nueva transacción.
3. WHEN se registra una transacción desde el widget, THE Sistema SHALL actualizar los totales del widget de forma inmediata.
4. THE Sistema SHALL actualizar los datos del widget cada vez que la app registre o modifique una transacción.

---

### Requisito 22 — Onboarding

**User Story:** Como usuario nuevo, quiero ver una guía de bienvenida que me explique las funciones principales, para empezar a usar la app sin confusión.

#### Criterios de Aceptación

1. WHEN el usuario abre la app por primera vez, THE Sistema SHALL mostrar una secuencia de 3 a 5 pantallas de onboarding con ilustraciones y descripciones de las funciones principales.
2. WHEN el usuario completa o salta el onboarding, THE Sistema SHALL marcar el onboarding como completado en almacenamiento local y no volver a mostrarlo.
3. THE Sistema SHALL incluir en el onboarding las siguientes secciones: registro de transacciones, cálculo de tributos ONAT, y configuración de la app.
4. WHEN el usuario está en el onboarding, THE Sistema SHALL permitir saltar la guía en cualquier momento mediante un botón visible.

---

### Requisito 23 — Soporte multimoneda

**User Story:** Como usuario, quiero registrar transacciones en CUP, MLC o USD con tipo de cambio configurable, para reflejar la realidad económica cubana con múltiples monedas.

#### Criterios de Aceptación

1. WHEN el usuario registra una transacción, THE Sistema SHALL permitir seleccionar la moneda: CUP, MLC o USD.
2. THE Sistema SHALL permitir al usuario configurar el tipo de cambio de MLC y USD respecto al CUP desde la pantalla de configuración.
3. WHEN el Sistema calcula totales del período, THE Sistema SHALL convertir todas las transacciones a CUP usando el tipo de cambio configurado y mostrar el total en CUP.
4. WHEN el usuario visualiza una transacción, THE Sistema SHALL mostrar el importe en la moneda original y su equivalente en CUP.
5. WHEN el tipo de cambio es actualizado, THE Sistema SHALL recalcular y actualizar todos los totales del período activo de forma inmediata.

---

### Requisito 24 — Código QR de cobro

**User Story:** Como usuario, quiero generar un código QR con mi número de cuenta y teléfono, para que mis clientes puedan escanear y realizar pagos fácilmente.

#### Criterios de Aceptación

1. WHEN el usuario accede a la sección de QR de cobro, THE Sistema SHALL mostrar un formulario para ingresar número de cuenta bancaria, número de teléfono y nombre del titular.
2. WHEN el usuario genera el QR, THE Sistema SHALL crear un código QR que codifique el número de cuenta, teléfono y nombre del titular en formato de texto estructurado.
3. THE Sistema SHALL mostrar el código QR generado en pantalla completa con el nombre del titular visible debajo.
4. WHEN el usuario pulsa compartir, THE Sistema SHALL exportar el código QR como imagen PNG y ofrecerlo para compartir por cualquier app del dispositivo.
5. THE Sistema SHALL persistir los datos de cobro del usuario para no tener que ingresarlos cada vez.

---

### Requisito 25 — Validaciones del QR de cobro

**User Story:** Como usuario, quiero que el sistema valide los datos del QR antes de generarlo, para evitar códigos con información incorrecta.

#### Criterios de Aceptación

1. WHEN el usuario ingresa el número de cuenta bancaria, THE Sistema SHALL validar que tenga exactamente 16 dígitos numéricos antes de generar el QR.
2. WHEN el usuario ingresa el número de teléfono, THE Sistema SHALL validar que comience con el prefijo `+53` antes de generar el QR.
3. WHEN alguna validación falla, THE Sistema SHALL mostrar un mensaje de error inline en el campo correspondiente sin generar el QR.

---

### Requisito 26 — Módulo de Inventario

**User Story:** Como usuario, quiero gestionar el inventario de mis productos con control de stock y movimientos, para saber cuánto tengo disponible y recibir alertas cuando el stock sea bajo.

#### Criterios de Aceptación

1. WHEN el usuario crea un producto, THE Sistema SHALL persistir nombre, descripción, categoría, unidad de medida, cantidad inicial, stock mínimo, precio de costo y precio de venta en CUP.
2. WHEN el usuario registra un movimiento de inventario, THE Sistema SHALL actualizar la cantidad del producto según el tipo: entrada (suma), salida (resta) o ajuste (reemplaza).
3. WHEN la cantidad de un producto es menor o igual a su stock mínimo configurado, THE Sistema SHALL mostrar una alerta visual en la lista de inventario.
4. THE Sistema SHALL mostrar una pestaña de "Stock bajo" que filtre únicamente los productos con cantidad ≤ stock mínimo.
5. THE Sistema SHALL permitir buscar productos por nombre o categoría en tiempo real.
6. WHEN el usuario elimina un producto, THE Sistema SHALL eliminar también sus movimientos asociados.

---

### Requisito 27 — Nómina de empleados

**User Story:** Como usuario, quiero calcular el salario neto de mis empleados con todas las retenciones y aportes patronales según la normativa cubana, para gestionar correctamente mi nómina.

#### Criterios de Aceptación

1. WHEN el usuario ingresa el salario devengado de un empleado, THE Sistema SHALL calcular la retención de Seguridad Social del trabajador (5%), el IIP según escala ONAT y el salario neto a cobrar.
2. WHEN el usuario calcula la nómina, THE Sistema SHALL calcular los aportes patronales: SS Patronal (12.5%), provisión de vacaciones (9.09%), provisión de subsidio (1.5%) y SS Especial (5%).
3. WHEN el usuario genera el asiento contable de la nómina, THE Sistema SHALL crear un asiento de partida doble cuadrado donde los débitos (gastos de la empresa) sean iguales a los créditos (obligaciones y pagos).
4. THE Sistema SHALL mostrar el costo total para la empresa como suma del salario devengado más todos los aportes patronales.
5. THE Sistema SHALL usar `cssEmployeeRate` de `TaxConfig` para la retención del trabajador y `cssEmployerRate` para el aporte patronal, manteniendo `cssRate` para el cálculo de tributos del TCP autónomo.
