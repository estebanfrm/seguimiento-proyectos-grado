# Decisiones de arquitectura

Estas decisiones desarrollan la especificación de [PLAN.md](PLAN.md); el modelo de dominio se conserva en [sgpg.jdl](../sgpg.jdl).

## ADR 01 — Monolito JHipster y modelo JDL

**Decisión:** usar el monolito JHipster 9.4 con Angular, Spring Boot y PostgreSQL, manteniendo el JDL como fuente de verdad del modelo.

**Motivo:** autenticación JWT, DTOs, mapeadores, repositorios, migraciones y pruebas generadas permiten concentrar el trabajo en el seguimiento académico.

**Consecuencia:** se conserva la estructura generada y sus nombres de rutas y relaciones. Las regeneraciones requieren revisar los cambios de dominio para preservar las reglas añadidas.

## ADR 02 — Reglas y permisos en el servidor

**Decisión:** validar las mutaciones en los servicios y consultar la relación del usuario con el proyecto mediante `ProyectoAccessService`.

**Motivo:** una petición HTTP directa debe cumplir las mismas restricciones que la interfaz. Ocultar botones solo orienta al usuario.

**Consecuencia:** el servidor fuerza autor, fechas, versión y etapa; el cliente muestra los requisitos y errores devueltos por la API. Tras las mutaciones relevantes se recalcula el avance.

## ADR 03 — Estrategia por transición

**Decisión:** representar los requisitos de cada transición con una estrategia del workflow, conservando el orden fijo de etapas.

**Motivo:** propuesta, anteproyecto, desarrollo y sustentación tienen condiciones distintas. Separarlas permite probar cada conjunto sin acumular condiciones en los controladores.

**Consecuencia:** consultar requisitos y avanzar deben evaluar las mismas condiciones y códigos. La transición, la actualización del proyecto y su historial constituyen una operación transaccional.

## ADR 04 — 403 para permisos y 409 para reglas

**Decisión:** responder 403 ante falta de autorización y 409 ante una regla de negocio incumplida, con `ReglaNegocioException` y un `codigo` estable.

**Motivo:** permite distinguir a una persona sin permiso de una persona autorizada que necesita completar requisitos.

**Consecuencia:** el frontend interpreta códigos como `ENTREGA_EN_CURSO` o `FECHA_LIMITE_VENCIDA`; el texto puede mejorar sin romper el contrato. No se usa 409 para conceder información a usuarios sin acceso.

## ADR 05 — Avance calculado y persistido

**Decisión:** calcular `base + floor((baseSiguiente - base) * cumplidos / (totalRequisitos + 1))` en `AvanceService.recalcular`, con bases 0, 20, 40, 75 y 100. FINALIZADO siempre tiene 100%.

**Motivo:** el porcentaje representa evidencia académica verificable, no una estimación editable por el usuario.

**Consecuencia:** PUT/PATCH no modifican el avance directamente. El `+1` del denominador reserva un tramo para la transición, de modo que completar requisitos no equivale a haber cerrado la etapa. Una sustentación lista para cerrar, como P5, tiene 87% y sigue ACTIVA; solo FINALIZADO alcanza 100%.

## ADR 06 — Tiempo mediante Clock inyectable

**Decisión:** usar `Instant.now(clock)` con un bean `Clock` en UTC para fechas forzadas y vencimientos.

**Motivo:** un reloj fijo permite verificar el instante exacto del vencimiento sin depender del día de ejecución.

**Consecuencia:** el plazo vence cuando `now > fechaLimite`, no cuando son iguales. Los escenarios demo se referencian al 13 de noviembre de 2026 y usan límites de 2027, salvo P4. El coordinador debe ajustar el reloj de sus pruebas temporales.

## ADR 07 — Datos demo explícitos y aislados de faker

**Decisión:** cargar once usuarios y seis proyectos mediante un changelog PostgreSQL incluido con contexto `dev`; retirar `faker` de la configuración de desarrollo y conservar sus archivos.

**Motivo:** las relaciones aleatorias no demuestran reglas académicas. P1–P6 deben representar estados reproducibles, con entregas, evaluaciones e historial coherentes.

**Consecuencia:** se reservan IDs 1001–1011 para usuarios y 2001–7010 para entidades; se eleva `sequence_generator` sin reducirla, dejando margen para el pool de 50 de Hibernate. Se reutiliza el BCrypt del usuario base `user`, cuya contraseña es `user`, verificada en el test unitario. Los enlaces de documentos son ficticios.

P2 no tiene observaciones; P3 contiene una corrección histórica ya resuelta. P5 tiene evaluaciones anticipadas respecto de su fecha límite de sustentación: el contrato exige que esa fecha exista, no que haya transcurrido. P6 también terminó antes de su fecha límite. `estudiante1` participa en P1 y P6 para mostrar un proyecto activo y otro cerrado con cinco estudiantes.

La prueba `DatosDemoIT` ejecuta Liquibase con contexto `dev` en un esquema temporal del PostgreSQL de Testcontainers y lo elimina al terminar. Así se comprueba la migración real sin introducir datos demo en las tablas de los otros tests de integración.

## ADR-08: Texto largo como `text` plano en PostgreSQL

**Contexto:** con `@Lob String`, Hibernate 7 trata la columna como _large object_ en PostgreSQL y guarda un OID en lugar del texto. Funcionaba en los tests (Hibernate escribía y leía igual) pero fallaba al leer datos cargados por Liquibase o SQL; se detectó al arrancar la demo.

**Decisión:** los campos `TextBlob` usan `@JdbcTypeCode(SqlTypes.LONGVARCHAR)` sin `@Lob`, así se guardan como `text` legible por cualquier cliente SQL.

## ADR-09: PDF como `bytea` y bytes solo de escritura en JSON

**Decisión:** `Entrega.archivo` usa `@JdbcTypeCode(SqlTypes.VARBINARY)` sin `@Lob`. Liquibase 5.0.3 traduce el tipo `blob` del changelog existente a `OID` en PostgreSQL; `EntregaArchivoTest` lo comprueba sin base de datos. La migración nueva convierte la columna a `bytea` mediante `lo_get(archivo)` si todavía es `oid` y añade `archivo_nombre`. Conserva los bytes de los large objects y los nulos; si la columna ya es `bytea`, omite la conversión. No elimina large objects que puedan tener otras referencias. `EntregaArchivoIT` verifica la columna y la lectura después de limpiar el contexto JPA.

**Consecuencia:** se valida el tipo `application/pdf`, la firma inicial `%PDF-` y el tamaño binario configurable (10 MB por defecto). Las respuestas JSON contienen metadatos e indicador `tieneArchivo`; los bytes se aceptan solo al escribir y se obtienen por un endpoint binario protegido por la visibilidad del proyecto. Omitir los bytes al editar conserva el PDF existente. La firma es una comprobación de formato inicial, sin interpretar la estructura completa del documento.
