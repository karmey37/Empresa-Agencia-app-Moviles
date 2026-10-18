# Agencia Oportunidades

Aplicación móvil Android para la **gestión de oportunidades y activaciones corporativas** de una agencia de marketing directo y beneficios corporativos. Permite al equipo comercial registrar, priorizar y dar seguimiento a eventos, ferias y actividades de organizaciones (empresas, municipalidades, instituciones) donde la agencia puede participar.

Proyecto semestral de **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC).

> Todos los datos de la aplicación (usuarios, organizaciones, contactos, eventos y propuestas) son **ficticios**. No se usan datos reales de personas ni de empresas.

## Integrantes

| Integrantes        | 
|--------------------|
| Matias Valdebenito | 
| Fernando Sepulveda | 
| Leandro Varela     | 

## Tecnologías

- Kotlin y Jetpack Compose
- Material Design 3
- Arquitectura MVVM (UI, ViewModel, Repository, fuente de datos)
- Room (SQLite) para persistencia local, con KSP
- StateFlow y corrutinas para el estado de las pantallas
- Navigation Compose
- Coil para mostrar imágenes
- Git y GitHub para el trabajo colaborativo

## Roles y cuentas de prueba

La app tiene dos roles. Cada uno ve pantallas y acciones distintas.

| Rol | Correo | Contraseña |
|---|---|---|
| Ejecutivo Comercial | ejecutivo1@agencia.cl | Ejec1234 |
| Ejecutivo Comercial | ejecutivo2@agencia.cl | Ejec1234 |
| Ejecutivo Comercial | ejecutivo3@agencia.cl | Ejec1234 |
| Supervisor Comercial | supervisor@agencia.cl | Super1234 |

- **Ejecutivo:** ve solo las oportunidades que tiene asignadas, las crea y edita, registra seguimientos y cambia estados. No puede confirmar ni descartar una oportunidad de forma definitiva.
- **Supervisor:** ve todas las oportunidades, asigna y reasigna responsables, y valida o descarta oportunidades.

## Funcionalidades

Estado de avance (se actualiza durante el desarrollo):

- [x] Modelo de datos relacional con Room (10 entidades)
- [x] DAOs, base de datos y repositorio
- [x] Datos de prueba ficticios que se cargan en la primera ejecución
- [x] Inicio y cierre de sesión con cuentas locales
- [x] Dashboard según el rol
- [x] Listado de oportunidades con búsqueda y filtros por estado y prioridad
- [x] Detalle de oportunidad
- [x] Formulario para crear y editar oportunidades
- [x] Registro y consulta de organizaciones y contactos
- [x] Seguimientos con historial y registro de nuevas gestiones
- [x] Panel de supervisor con asignación de responsables
- [x] Cambio de estado respetando permisos y transiciones
- [x] Alertas: eventos próximos, seguimientos pendientes y gestiones atrasadas
- [x] Cámara para fotografiar eventos, con vista previa y asociación a la oportunidad
- [x] Integración con el calendario del dispositivo
- [ ] Tema Material 3 centralizado, colores semánticos y animaciones

## Arquitectura

El proyecto sigue MVVM, separado en capas:

```
cl.duoc.agenciaoportunidades
├── model/     Entidades Room, enums y clases de resumen para los listados
├── data/      DAOs, AppDatabase, AgenciaRepository y datos iniciales
├── ui/        Pantallas (Composables), ViewModels y UiState por módulo
│   └── theme/ Colores, tipografía y tema Material 3
└── util/      Validaciones y utilidades (separadas de los Composables)
```

Flujo de datos: `Pantalla (Compose) → ViewModel (StateFlow / UiState) → Repository → DAO → Room (SQLite)`

## Modelo de datos

Modelo relacional normalizado (tercera forma normal). Las comunas viven en una tabla propia para que la región dependa de la comuna y no se repita en cada registro.

| Tabla | Descripción | Relaciones |
|---|---|---|
| `usuarios` | Cuentas ficticias con perfil Ejecutivo o Supervisor | |
| `comunas` | Comuna y región | |
| `organizaciones` | Empresas, municipalidades e instituciones | → `comunas` |
| `contactos` | Contactos corporativos ficticios | → `organizaciones` |
| `eventos` | Ferias, jornadas y actividades | → `organizaciones`, `comunas` |
| `oportunidades` | Oportunidad comercial sobre un evento | → `eventos`, `usuarios` (responsable) |
| `seguimientos` | Acciones de gestión y próximo seguimiento | → `oportunidades`, `usuarios`, `contactos` |
| `historial_estados` | Cambios de estado de cada oportunidad | → `oportunidades` |
| `resultados_activacion` | Resultado de una activación realizada | → `oportunidades` |
| `evidencias` | Fotografías asociadas a una oportunidad | → `oportunidades` |

## Cómo ejecutar el proyecto

1. Clonar el repositorio:
```
   git clone https://github.com/karmey37/Empresa-Agencia-app-Moviles.git
```
2. Abrir la carpeta del proyecto con **Android Studio**.
3. Esperar a que termine la sincronización de Gradle (**Sync**).
4. Seleccionar un emulador (por ejemplo Pixel 8) o un teléfono con depuración USB.
5. Presionar **Run** (▶).
6. Iniciar sesión con alguna de las cuentas de prueba indicadas arriba.

En la primera ejecución la app crea la base de datos y carga los datos ficticios automáticamente.

## Flujo de trabajo en equipo

- Cada integrante trabaja en su propia rama y sube cambios mediante commits técnicos.
- Antes de empezar a trabajar: `git pull origin main`.
- No se modifica la carpeta `model` sin avisar al resto del grupo, porque cambia la base de datos.

## Datos ficticios

Los nombres, correos, teléfonos, organizaciones, costos y resultados de activaciones son inventados con fines académicos. La app no se conecta a servicios externos ni envía mensajes reales.