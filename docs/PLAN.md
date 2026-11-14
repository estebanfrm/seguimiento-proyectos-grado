# SGPG — Plan de implementación (especificación para los workers)

Sistema de seguimiento de proyectos de grado. Stack: Java 17 + Spring Boot (JHipster 9.4.0, monolito, JWT)
+ PostgreSQL + Angular. Paquete raíz: `co.edu.sgpg`. Modelo de dominio: `sgpg.jdl` (fuente de verdad).

Lo que el proyecto debe demostrar: **flujos académicos**, **permisos diferenciados por rol** y
**reglas para avanzar entre etapas**.

---

## 1. Roles

| Authority          | Persona       | Resumen                                                                 |
|--------------------|---------------|-------------------------------------------------------------------------|
| `ROLE_ADMIN`       | Coordinador   | Todo. Asigna tutor/jurados, fija fechas límite, cancela proyectos.      |
| `ROLE_ESTUDIANTE`  | Estudiante    | Registra propuesta, sube entregas de SU proyecto en la etapa actual.    |
| `ROLE_TUTOR`       | Tutor         | Revisa entregas, añade/resuelve observaciones, solicita avance de etapa.|
| `ROLE_JURADO`      | Jurado        | Evalúa ANTEPROYECTO y SUSTENTACION de proyectos donde está asignado.    |
| `ROLE_USER`        | (todos)       | Autenticado.                                                            |

Constantes en `co.edu.sgpg.security.AuthoritiesConstants` (ya creadas en la base).
"Relación con el proyecto" = estudiante (`proyecto.estudiantes`), tutor (`proyecto.tutor`) o jurado
(`proyecto.jurados`). Se consulta SIEMPRE mediante `ProyectoAccessService` (ya creado en la base).

### Matriz de permisos

| Acción                                   | ADMIN | ESTUDIANTE              | TUTOR                 | JURADO                     |
|------------------------------------------|:-----:|-------------------------|-----------------------|----------------------------|
| Ver proyecto / entregas / evaluaciones   | todos | solo los suyos          | solo donde es tutor   | solo donde es jurado       |
| Crear proyecto (propuesta)               | sí    | sí (queda como estudiante) | no                 | no                         |
| Editar título/resumen                    | sí    | solo en PROPUESTA       | no                    | no                         |
| Asignar tutor / jurados / estudiantes    | sí    | no                      | no                    | no                         |
| Cambiar etapa/estado/avance por PUT/PATCH| **nunca** (solo vía workflow)                                                    ||||
| CRUD fechas límite                       | sí    | no (lectura)            | no (lectura)          | no (lectura)               |
| Crear entrega                            | no    | sí (reglas §3)          | no                    | no                         |
| Revisar entrega (cambiar estado)         | sí    | no                      | sí, si es su tutor    | no                         |
| Crear observación / resolverla           | sí    | no (lectura)            | sí, si es su tutor    | no                         |
| Crear evaluación                         | no    | no                      | no                    | sí, si asignado (reglas §3)|
| Avanzar etapa                            | sí    | no                      | sí, si es su tutor    | no                         |
| Cancelar proyecto                        | sí    | no                      | no                    | no                         |
| Historial de etapas                      | lectura (según visibilidad); nunca se escribe por REST                          ||||
| Borrar cualquier entidad                 | sí    | no                      | no                    | no                         |

Sin permiso → **403**. Regla de negocio violada → **409** con `ReglaNegocioException` (ver §5).

---

## 2. Etapas y reglas de transición

Orden fijo, sin saltos ni retrocesos: `PROPUESTA → ANTEPROYECTO → DESARROLLO → SUSTENTACION → FINALIZADO`.
`FINALIZADO` es terminal. Solo se avanza si `estado == ACTIVO`.

Requisitos (cada uno tiene un `codigo` estable que usa el frontend):

**PROPUESTA → ANTEPROYECTO**
- `TUTOR_ASIGNADO` — el proyecto tiene tutor.
- `ESTUDIANTES_ASIGNADOS` — al menos un estudiante.
- `ENTREGA_APROBADA` — existe una entrega de la etapa actual en estado `APROBADA`.
- `SIN_CORRECCIONES_PENDIENTES` — no hay observaciones `CORRECCION_REQUERIDA` con `resuelta=false` sobre entregas de la etapa actual.

**ANTEPROYECTO → DESARROLLO**
- `ENTREGA_APROBADA`
- `SIN_CORRECCIONES_PENDIENTES`
- `JURADOS_ASIGNADOS` — al menos 2 jurados.
- `EVALUACIONES_COMPLETAS` — todos los jurados asignados evaluaron la etapa actual.
- `EVALUACION_APROBATORIA` — ninguna evaluación con concepto `REPROBADO` y promedio ≥ 3.0.

**DESARROLLO → SUSTENTACION**
- `ENTREGA_APROBADA` (documento final)
- `SIN_CORRECCIONES_PENDIENTES`
- `SUSTENTACION_PROGRAMADA` — existe `FechaLimiteEtapa` para la etapa `SUSTENTACION`.

**SUSTENTACION → FINALIZADO**
- `EVALUACIONES_COMPLETAS`
- Resultado: si promedio ≥ 3.0 y ningún `REPROBADO` → `estado = APROBADO`; si no → `estado = REPROBADO`.
  En ambos casos `etapaActual = FINALIZADO`.

Al avanzar: se crea un `HistorialEtapa` (origen, destino, fecha, usuario, comentario), se actualiza
`fechaActualizacion` y se recalcula `porcentajeAvance`. Cancelar (ADMIN): `estado = CANCELADO`,
historial con origen=destino=etapa actual y el comentario (obligatorio).

### Porcentaje de avance
Base por etapa: PROPUESTA 0, ANTEPROYECTO 20, DESARROLLO 40, SUSTENTACION 75, FINALIZADO 100.
`avance = base + (baseSiguiente - base) * cumplidos / totalRequisitos` (redondeo hacia abajo).
FINALIZADO = 100. Se calcula con `AvanceService.recalcular(proyectoId)` (interfaz en la base) y se
persiste en `proyecto.porcentajeAvance`.

---

## 3. Reglas de entidades

**ProyectoGrado (creación)**: el servidor fuerza `etapaActual=PROPUESTA`, `estado=ACTIVO`,
`porcentajeAvance=0`, `fechaRegistro=now`. Si lo crea un estudiante, se añade como estudiante y
se ignoran tutor/jurados enviados. En update/patch se ignoran (o 409) cambios de `etapaActual`,
`estado`, `porcentajeAvance`, `fechaRegistro`.

**Entrega (creación)** — solo estudiante del proyecto:
- `proyecto.estado == ACTIVO` y `etapaActual != FINALIZADO` → si no, 409 `PROYECTO_NO_ACTIVO`.
- `etapa` se fuerza a `proyecto.etapaActual`.
- Si existe `FechaLimiteEtapa` de esa etapa y `now > fechaLimite` → 409 `FECHA_LIMITE_VENCIDA`.
- Si ya hay una entrega de la etapa en `ENVIADA` o `EN_REVISION` → 409 `ENTREGA_EN_CURSO`.
- Si ya hay una entrega `APROBADA` en la etapa → 409 `ETAPA_YA_APROBADA`.
- Forzados: `version = max(version de la etapa)+1`, `estado=ENVIADA`, `autor=usuario actual`, `fechaEntrega=now`.
- Edición por el estudiante solo mientras `ENVIADA`; nunca cambia estado/version/etapa.

**Revisión de entrega** `PUT /api/entregas/{id}/revision` `{ "estado": "EN_REVISION|CON_OBSERVACIONES|APROBADA|RECHAZADA" }`
— tutor del proyecto o admin. No se puede `APROBADA` si la entrega tiene observaciones
`CORRECCION_REQUERIDA` sin resolver → 409 `CORRECCIONES_PENDIENTES`. Solo se revisan entregas de la
etapa actual del proyecto.

**Observación** — tutor del proyecto o admin. Forzados: `autor`, `fechaCreacion=now`, `resuelta=false`.
Si `tipo == CORRECCION_REQUERIDA` la entrega pasa a `CON_OBSERVACIONES`.
`PUT /api/observaciones/{id}/resolver` — tutor/admin marca `resuelta=true`.

**Evaluación** — solo jurado asignado:
- `etapa` se fuerza a `proyecto.etapaActual`; debe ser `ANTEPROYECTO` o `SUSTENTACION` → si no 409 `ETAPA_NO_EVALUABLE`.
- Una por jurado y etapa → 409 `EVALUACION_DUPLICADA`.
- Coherencia: `calificacion < 3.0` ⇔ `concepto == REPROBADO` → si no 409 `CONCEPTO_INCOHERENTE`.
- Forzados: `jurado = usuario actual`, `fechaEvaluacion = now`. No editable salvo admin.

**HistorialEtapa**: solo lectura por REST (POST/PUT/PATCH/DELETE → 403 para todos, incluido admin
si se prefiere quitar los endpoints; lo importante es que nadie lo escriba por REST).

Tras cualquier mutación que afecte requisitos (entrega creada/revisada, observación creada/resuelta,
evaluación creada, asignación de jurados/tutor) llamar `avanceService.recalcular(proyectoId)`.

---

## 4. Contrato de API nuevo (lo consume el frontend)

Las rutas CRUD generadas por JHipster se mantienen (`/api/proyecto-grados`, `/api/entregas`,
`/api/observacions`, `/api/evaluacions`, `/api/fecha-limite-etapas`, `/api/historial-etapas` —
verificar el nombre exacto en los `*Resource.java` generados) pero filtradas por permisos.

| Método | Ruta                                              | Quién                 | Respuesta |
|--------|---------------------------------------------------|-----------------------|-----------|
| GET    | `/api/proyecto-grados/{id}/workflow`              | quien puede ver       | `RequisitosTransicionDTO` |
| POST   | `/api/proyecto-grados/{id}/workflow/avanzar`      | tutor del proyecto, admin | `ProyectoGradoDTO` / 409 |
| POST   | `/api/proyecto-grados/{id}/workflow/cancelar`     | admin                 | `ProyectoGradoDTO` |
| GET    | `/api/proyecto-grados/{id}/seguimiento`           | quien puede ver       | `SeguimientoProyectoDTO` |
| PUT    | `/api/entregas/{id}/revision`                     | tutor, admin          | `EntregaDTO` |
| PUT    | `/api/observacions/{id}/resolver`                 | tutor, admin          | `ObservacionDTO` |
| GET    | `/api/usuarios/por-rol/{authority}`               | admin                 | `UsuarioResumenDTO[]` |

```jsonc
// RequisitosTransicionDTO
{ "proyectoId": 1, "etapaActual": "PROPUESTA", "etapaSiguiente": "ANTEPROYECTO", // null si FINALIZADO
  "puedeAvanzar": false,
  "requisitos": [ { "codigo": "TUTOR_ASIGNADO", "descripcion": "El proyecto tiene tutor asignado", "cumplido": true } ] }

// Body de avanzar / cancelar
{ "comentario": "texto opcional (obligatorio en cancelar)" }

// SeguimientoProyectoDTO
{ "proyecto": { /* ProyectoGradoDTO */ },
  "porcentajeAvance": 35,
  "etapas": [
    { "etapa": "PROPUESTA", "estado": "COMPLETADA|ACTUAL|PENDIENTE",
      "fechaLimite": "2026-12-01T23:59:00Z", "vencida": false, "diasRestantes": 18,
      "entregas": [ { "id": 3, "titulo": "...", "version": 2, "estado": "APROBADA",
                      "fechaEntrega": "...", "observacionesPendientes": 0 } ],
      "evaluaciones": [ { /* EvaluacionDTO */ } ] } ],
  "historial": [ { /* HistorialEtapaDTO */ } ],
  "requisitos": { /* RequisitosTransicionDTO */ } }

// UsuarioResumenDTO
{ "id": 5, "login": "tutor1", "firstName": "Ana", "lastName": "Pérez" }
```

---

## 5. Contratos compartidos ya creados en la base (NO cambiar firmas)

- `security/AuthoritiesConstants`: `ADMIN, USER, ANONYMOUS, ESTUDIANTE, TUTOR, JURADO`.
- Liquibase `authority.csv`: incluye `ROLE_ESTUDIANTE`, `ROLE_TUTOR`, `ROLE_JURADO`.
- `service/ReglaNegocioException(String codigo, String mensaje)` → `ExceptionTranslator` la mapea a
  409 ProblemDetail con propiedades `codigo` y `message = "error.reglaNegocio"`.
  Subclase opcional con lista `pendientes` para avanzar.
- `service/ProyectoAccessService` (+impl): `esAdmin()`, `esEstudianteDe(id)`, `esTutorDe(id)`,
  `esJuradoDe(id)`, `puedeVer(id)`, `verificarPuedeVer(id)` (lanza `AccessDeniedException` → 403),
  `loginActual()`.
- `service/AvanceService` (+impl simple) `int recalcular(Long proyectoId)`. El stream A reemplaza la impl.
- Métodos de consulta añadidos a los repositorios (ver los `*Repository.java`).

---

## 6. Reparto de trabajo (un worker Codex por stream, cada uno en su worktree/rama)

| Stream | Rama               | Dueño de                                                                                  |
|--------|--------------------|-------------------------------------------------------------------------------------------|
| A      | `feat/workflow`    | `service/workflow/**`, `web/rest/ProyectoWorkflowResource.java`, `AvanceServiceImpl`, DTOs de workflow/seguimiento, sus tests |
| B      | `feat/permisos`    | Resources/Services/QueryServices de entidades generadas, `UsuarioPorRolResource`, sus tests de integración |
| C      | `feat/frontend`    | `src/main/webapp/**` (Angular)                                                           |
| D      | `feat/datos`       | Liquibase datos demo, desactivar fake-data incoherente, `README.md`, `docs/**` (excepto este PLAN) |

Reglas para todos los workers:
1. No tocar archivos de otro stream. Si necesitas algo de otro stream, codifícalo contra el contrato §4/§5.
2. Si necesitas un método de repositorio nuevo, añádelo en el repositorio correspondiente (cambio mínimo, al final de la interfaz).
3. Código y mensajes en español para dominio; seguir el estilo JHipster existente. `./mvnw spotless:apply` si existe.
4. Tests: backend `./mvnw -ntp verify` (o al menos los tests tocados); frontend `npm run lint && npm test && npm run webapp:build`.
5. Commit en tu rama con mensajes claros. No hacer push ni merge.
6. Al terminar escribe un resumen: qué hiciste, archivos, tests ejecutados y resultado, pendientes.
