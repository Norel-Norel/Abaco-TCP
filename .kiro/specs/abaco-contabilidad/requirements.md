# Documento de Requisitos — Ábaco Contabilidad

## Introducción

Ábaco es una aplicación móvil Android de contabilidad simplificada dirigida a trabajadores por cuenta propia (TCP) y emprendedores cubanos. Permite registrar ingresos y gastos, visualizar el estado financiero mensual mediante gráficos, y calcular automáticamente los tributos correspondientes a la Oficina Nacional de Administración Tributaria (ONAT) según la legislación cubana vigente. La interfaz sigue los principios de Material 3 con una navegación fluida inspirada en iOS.

## Glosario

- **TCP**: Trabajador por Cuenta Propia. Persona natural autorizada a ejercer actividades económicas de forma independiente en Cuba.
- **ONAT**: Oficina Nacional de Administración Tributaria. Entidad cubana responsable de la recaudación de impuestos.
- **Ingreso**: Entrada de dinero derivada de la actividad económica del TCP.
- **Gasto**: Salida de dinero asociada a la actividad económica del TCP.
- **Utilidad Neta**: Diferencia entre los ingresos totales y los gastos deducibles en un período fiscal.
- **Impuesto sobre Ingresos Personales (IIP)**: Tributo aplicado a la utilidad neta del TCP según escala progresiva cubana.
- **Contribución a la Seguridad Social (CSS)**: Aporte obligatorio del TCP al sistema de seguridad social cubano, calculado sobre los ingresos brutos.
- **Período Fiscal**: Mes calendario utilizado como unidad de reporte y cálculo tributario.
- **Categoría**: Clasificación temática de un ingreso o gasto (ej. Ventas, Servicios, Materias Primas, Transporte).
- **Sistema**: La aplicación móvil Ábaco.
- **Usuario**: El TCP o emprendedor que utiliza la aplicación.
- **Dashboard**: Pantalla principal que muestra el resumen financiero mensual con gráficos.
- **Navegación por pestañas**: Barra de navegación inferior con transiciones animadas fluidas.

---

## Requisitos

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
3. THE Sistema SHALL mostrar un gráfico circular (pie chart) con la distribución porcentual de los gastos por categoría.
4. WHEN el usuario selecciona un período fiscal diferente, THE Sistema SHALL actualizar todos los gráficos y totales para reflejar el período seleccionado.
5. WHEN no existen transacciones en el período fiscal activo, THE Sistema SHALL mostrar un estado vacío con un mensaje orientativo al usuario.

---

### Requisito 4 — Cálculo de tributos ONAT

**User Story:** Como usuario, quiero que la aplicación calcule automáticamente mis obligaciones tributarias con la ONAT, para cumplir con la legislación cubana sin errores.

#### Criterios de Aceptación

1. WHEN el usuario accede a la pantalla de Tributos, THE Sistema SHALL calcular y mostrar la Contribución a la Seguridad Social (CSS) aplicando el 20% sobre los ingresos brutos del período fiscal.
2. WHEN el usuario accede a la pantalla de Tributos, THE Sistema SHALL calcular el Impuesto sobre Ingresos Personales (IIP) aplicando la escala progresiva cubana vigente sobre la utilidad neta anual estimada.
3. THE Sistema SHALL aplicar la siguiente escala progresiva para el IIP sobre la utilidad neta anual: hasta 10 000 CUP — exento (0%); de 10 001 a 20 000 CUP — 15%; de 20 001 a 30 000 CUP — 20%; de 30 001 a 50 000 CUP — 30%; más de 50 000 CUP — 50%. El usuario podrá actualizar estas tasas desde la configuración de la aplicación.
4. WHEN el usuario accede a la pantalla de Tributos, THE Sistema SHALL mostrar el desglose detallado de cada tributo con su base de cálculo, tasa aplicada e importe resultante.
5. WHEN los ingresos brutos del período son iguales a cero, THE Sistema SHALL mostrar los tributos calculados como cero sin generar errores.
6. THE Sistema SHALL serializar y deserializar los datos de tributos calculados usando JSON para persistencia local.

---

### Requisito 6 — Configuración tributaria

**User Story:** Como usuario, quiero poder ajustar las tasas y tramos tributarios desde la app, para adaptarme a cambios en la legislación cubana sin necesidad de actualizar la aplicación.

#### Criterios de Aceptación

1. THE Sistema SHALL proveer una pantalla de configuración accesible desde el menú principal.
2. WHEN el usuario modifica un tramo o tasa del IIP, THE Sistema SHALL validar que los tramos sean consecutivos y las tasas estén entre 0% y 100%.
3. WHEN el usuario guarda la configuración tributaria, THE Sistema SHALL persistir los nuevos valores y recalcular los tributos del período activo de forma inmediata.
4. WHEN el usuario modifica la tasa de CSS, THE Sistema SHALL validar que el valor esté entre 0% y 100% antes de persistirlo.
5. THE Sistema SHALL serializar y deserializar la configuración tributaria usando JSON para su persistencia local.

---

### Requisito 7 — Persistencia de datos

**User Story:** Como usuario, quiero que mis datos se guarden localmente en el dispositivo, para acceder a ellos sin conexión a internet.

#### Criterios de Aceptación

1. THE Sistema SHALL almacenar todas las transacciones en una base de datos local SQLite mediante Room.
2. WHEN la aplicación se cierra y se vuelve a abrir, THE Sistema SHALL restaurar todas las transacciones y el estado de la aplicación sin pérdida de datos.
3. WHEN el usuario elimina una transacción, THE Sistema SHALL eliminarla de la base de datos local y actualizar los totales del período afectado de forma inmediata.
4. THE Sistema SHALL serializar y deserializar las transacciones usando JSON para operaciones de exportación e importación.

---

### Requisito 8 — Módulos activables

**User Story:** Como usuario, quiero activar o desactivar módulos de funcionalidad según mis necesidades, para mantener la app simple y relevante a mi tipo de negocio.

#### Criterios de Aceptación

1. THE Sistema SHALL proveer una lista de módulos opcionales que el usuario puede activar o desactivar desde la pantalla de configuración.
2. WHEN el usuario desactiva un módulo, THE Sistema SHALL ocultar las pantallas, opciones de menú y funcionalidades asociadas a ese módulo sin eliminar los datos existentes.
3. WHEN el usuario reactiva un módulo previamente desactivado, THE Sistema SHALL restaurar todas las pantallas y datos asociados a ese módulo de forma inmediata.
4. THE Sistema SHALL incluir los siguientes módulos en la versión inicial: Tributos ONAT, Gráficos del Dashboard, Configuración tributaria.
5. THE Sistema SHALL persistir el estado de activación de cada módulo en almacenamiento local.
