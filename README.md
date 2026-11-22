# Agencia Oportunidades

Aplicación móvil Android para la **gestión de oportunidades y activaciones corporativas** de una agencia de marketing directo y beneficios corporativos. Permite al equipo comercial registrar, priorizar y dar seguimiento a eventos, ferias y actividades de organizaciones (empresas, municipalidades, instituciones) donde la agencia puede participar.

Proyecto semestral de **DSY1105 – Desarrollo de Aplicaciones Móviles** (Duoc UC). Esta entrega corresponde a la Evaluación Parcial 4: conexión con un microservicio propio en Spring Boot, consumo de una API externa, pruebas unitarias y APK firmado.

> Todos los datos de la aplicación (usuarios, organizaciones, contactos, eventos y propuestas) son **ficticios**. No se usan datos reales de personas ni de empresas.

## Repositorios

| Parte | Repositorio |
|---|---|
| App móvil (este repositorio) | https://github.com/karmey37/Empresa-Agencia-app-Moviles |
| Microservicio (Spring Boot) | https://github.com/karmey37/Backend-Agencia-App-Movil |

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
- Retrofit 3 + OkHttp + Gson para consumir el microservicio y la API externa
- Coil para mostrar imágenes
- Pruebas: JUnit 4, `kotlinx-coroutines-test`, MockWebServer y cobertura con Kover
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
- [x] Tema Material 3 centralizado, colores semánticos y animaciones
- [x] **Directorio en servidor:** CRUD completo de organizaciones contra el microservicio Spring Boot (listar, buscar, crear, editar y eliminar)
- [x] **Feriados de Chile:** consulta de una API externa, con navegación por año y destacado del próximo feriado
- [x] Pruebas unitarias con cobertura mínima del 80 % sobre la lógica medida
- [x] APK de release firmado y versionado

Las pantallas nuevas se abren desde la barra superior de **Organizaciones**: el ícono de nube abre el Directorio en servidor y el ícono de calendario abre los Feriados.

## Arquitectura

El proyecto sigue MVVM, separado en capas:

```
cl.duoc.agenciaoportunidades
├── model/       Entidades Room, enums y clases de resumen para los listados
├── data/        DAOs, AppDatabase, AgenciaRepository y datos iniciales (almacenamiento local)
│   ├── remote/  Microservicio propio: AgenciaApiService, ApiClient, OrganizacionDto y
│   │            OrganizacionesRemoteRepository
│   └── externa/ API externa de feriados: FeriadosApiService, FeriadosClient, FeriadoDto y
│                FeriadosRepository
├── ui/          Pantallas (Composables), ViewModels y UiState por módulo
│   ├── directorio/  Directorio en servidor (CRUD contra el microservicio)
│   ├── feriados/    Feriados de Chile (API externa)
│   └── theme/       Colores, tipografía y tema Material 3
└── util/        Validaciones y utilidades (separadas de los Composables)
```

Flujos de datos:

- Local: `Pantalla (Compose) → ViewModel (StateFlow / UiState) → AgenciaRepository → DAO → Room (SQLite)`
- Microservicio: `Pantalla → DirectorioViewModel → OrganizacionesRemoteRepository → AgenciaApiService (Retrofit) → API Spring Boot → MySQL`
- API externa: `Pantalla → FeriadosViewModel → FeriadosRepository → FeriadosApiService (Retrofit) → date.nager.at`

Los repositorios remotos reciben la interfaz de Retrofit por constructor, y devuelven `Result`. Eso permite reemplazar la red por una API falsa en las pruebas, y los errores de conexión se traducen a mensajes para el usuario.

## Endpoints utilizados

### Microservicio propio (Spring Boot)

Repositorio: https://github.com/karmey37/Backend-Agencia-App-Movil

URL base: `http://10.0.2.2:8080/` desde el emulador de Android (`10.0.2.2` es el PC anfitrión). En el PC se prueba con `http://localhost:8080/`.

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/organizaciones` | Lista todas las organizaciones | 200 |
| GET | `/organizaciones/{id}` | Obtiene una organización | 200 / 404 |
| POST | `/organizaciones` | Crea una organización | 201 |
| PUT | `/organizaciones/{id}` | Actualiza una organización | 200 / 404 |
| DELETE | `/organizaciones/{id}` | Elimina una organización | 204 / 404 |

Cuerpo JSON de ejemplo (POST y PUT):

```json
{
  "nombre": "Centro Comercial Plaza Sur",
  "tipo": "CENTRO_COMERCIAL",
  "rubro": "Comercio",
  "comuna": "San Bernardo",
  "sitioWeb": "https://www.ejemplo-demo.cl",
  "publicoAsociado": "Visitantes y locatarios",
  "observaciones": ""
}
```

El campo `tipo` usa los valores del enum `TipoOrganizacion` de la app (`EMPRESA`, `MUNICIPALIDAD`, `CENTRO_COMERCIAL`, etc.).

### API externa: feriados (Nager.Date)

API pública, gratuita y sin clave: https://date.nager.at

| Método | URL | Descripción |
|---|---|---|
| GET | `https://date.nager.at/api/v3/PublicHolidays/{año}/CL` | Feriados públicos de Chile del año indicado |

### Diferencias entre la API externa y el microservicio propio

| | API externa (Nager.Date) | Microservicio propio |
|---|---|---|
| Quién la controla | Un tercero | El equipo |
| Operaciones | Solo lectura (GET) | CRUD completo |
| Protocolo | HTTPS | HTTP local (solo hacia `10.0.2.2`) |
| Datos | Públicos, sin base propia | Persisten en MySQL |
| Cliente Retrofit | `FeriadosClient` | `ApiClient` |

Son dos clientes Retrofit independientes: un fallo de la API externa no afecta al CRUD del microservicio ni a los datos locales de Room.

## Cómo ejecutar el proyecto

### 1. Levantar el microservicio

1. Iniciar **MySQL** (por ejemplo con XAMPP). Usuario `root` sin contraseña, puerto 3306.
2. Clonar y abrir `Backend-Agencia-App-Movil` con IntelliJ IDEA (JDK 21).
3. Ejecutar `AgenciaApiApplication` y esperar el mensaje `Started AgenciaApiApplication`.
4. Comprobar en el navegador: `http://localhost:8080/organizaciones` debe mostrar 3 organizaciones.

La base `bd_agencia`, la tabla y los datos de ejemplo se crean solos en la primera ejecución. Los detalles y los problemas frecuentes están en el README del backend.

### 2. Ejecutar la app

1. Clonar el repositorio:
```
   git clone https://github.com/karmey37/Empresa-Agencia-app-Moviles.git
```
2. Abrir la carpeta del proyecto con **Android Studio**.
3. Esperar a que termine la sincronización de Gradle (**Sync**).
4. Seleccionar un **emulador** (por ejemplo Pixel 8) y presionar **Run** (▶).
5. Iniciar sesión con alguna de las cuentas de prueba indicadas arriba.
6. En **Organizaciones**, tocar el ícono de nube (Directorio en servidor) o el de calendario (Feriados).

En la primera ejecución la app crea la base de datos local y carga los datos ficticios automáticamente. Para los Feriados se necesita conexión a internet.

> **Dispositivo físico:** la dirección `10.0.2.2` solo existe en el emulador. Con un teléfono conectado por USB hay que crear el puente `adb reverse tcp:8080 tcp:8080` y apuntar `ApiClient` a `http://localhost:8080/` (agregando `localhost` a `res/xml/network_security_config.xml`).

## Pruebas unitarias y cobertura

Herramientas: JUnit 4, `kotlinx-coroutines-test`, MockWebServer y **Kover**. El proyecto tiene más de 150 pruebas unitarias que se ejecutan en la JVM, sin emulador.

```
.\gradlew.bat :app:testDebugUnitTest        # ejecuta las pruebas
.\gradlew.bat :app:koverHtmlReportDebug     # informe HTML en app/build/reports/kover/htmlDebug/index.html
.\gradlew.bat :app:koverVerifyDebug         # falla si la cobertura baja del 80 %
```

### Qué se prueba y cómo

| Lógica probada | Pruebas | Técnica |
|---|---|---|
| `Validaciones`, `ReglasNegocio`, `Alertas` | Casos válidos, inválidos y límites | Pruebas directas (patrón Arrange–Act–Assert) |
| `OrganizacionesRemoteRepository` y `FeriadosRepository` | Método HTTP, ruta, cuerpo y códigos 200, 201, 204, 404 y 500 | **MockWebServer** (servidor HTTP falso) |
| `DirectorioViewModel` y `FeriadosViewModel` | Carga, búsqueda, crear, editar, borrar y errores | **API falsa** inyectada por constructor + `Dispatchers.setMain` y `advanceUntilIdle` |
| Errores de red y cancelación de corrutinas | `IOException`, `HttpException` y `CancellationException` | API falsa que lanza la excepción |

Lo que se mockea es **la red**: el ViewModel y el repository bajo prueba son los reales.

### Qué se mide con Kover y por qué

La regla `minBound(80)` está definida en `app/build.gradle.kts` y se aplica a la lógica que se puede probar en la JVM:

- `util`: `Validaciones`, `ReglasNegocio` y `Alertas`
- Microservicio: `OrganizacionesRemoteRepository`, `DirectorioViewModel` y `DirectorioValidador`
- API externa: `FeriadosRepository`, `FeriadosViewModel` y `FeriadosLogica`

Quedan **fuera** de la medición, de forma justificada:

- Pantallas Compose, tema y navegación: es presentación visual, no lógica de negocio.
- Los demás ViewModels y `AgenciaRepository`: dependen de `Application` y de Room (`AndroidViewModel`), por lo que requieren pruebas instrumentadas en emulador. Es una mejora pendiente para el EFT.

## APK firmado

| Dato | Valor |
|---|---|
| Archivo | `release/AgenciaOportunidades-1.1.0.apk` |
| Llave | `agencia-release.jks` (llave **académica**, en la raíz del repositorio) |
| `applicationId` | `cl.duoc.agenciaoportunidades` |
| `versionCode` / `versionName` | 2 / 1.1.0 |
| Firma | Esquema v2 (suficiente para `minSdk 24`) |

La firma se configura en `app/build.gradle.kts`, que lee las credenciales desde `keystore.properties`. Ese archivo **no se sube a Git** (está en `.gitignore`); en su lugar se incluye `keystore.properties.example`.

### Cómo se generó, paso a paso

1. Crear la llave con `keytool` (ajusta los datos y usa tus contraseñas):
```
keytool -genkeypair -v -keystore agencia-release.jks -alias agencia -keyalg RSA -keysize 2048 -validity 10000
```
2. Copiar `keystore.properties.example` como `keystore.properties` y completar `storePassword` y `keyPassword`.
3. Subir `versionCode` y `versionName` en `defaultConfig`, sin cambiar `applicationId`.
4. Generar el APK de release:
```
.\gradlew.bat :app:assembleRelease
```
5. El resultado queda en `app/build/outputs/apk/release/`; se copia a `release/` con el nombre versionado.
6. Verificar la firma con `apksigner` (incluido en las Build Tools del SDK de Android):
```
apksigner verify --verbose --print-certs release/AgenciaOportunidades-1.1.0.apk
```
7. Instalar y probar en un emulador o dispositivo.

Para publicar una actualización sin perder datos hay que **subir `versionCode`**, mantener el **mismo `applicationId`** y firmar con la **misma llave**; si cualquiera de las dos últimas cambia, Android rechaza la actualización o la instala como otra aplicación.

## Evidencias

> Agrega las capturas en la carpeta `docs/capturas/` con estos nombres.

![APK firmado y llave](docs/capturas/apk-firmado.png)
![Informe de cobertura Kover](docs/capturas/kover.png)
![Directorio en servidor](docs/capturas/directorio.png)
![Feriados de Chile](docs/capturas/feriados.png)

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

El microservicio tiene su propia tabla `organizaciones` en MySQL, independiente de la de Room: el Directorio en servidor y las organizaciones locales son fuentes de datos separadas.

## Flujo de trabajo en equipo

- Cada integrante trabaja en su propia rama y sube cambios mediante commits técnicos.
- Antes de empezar a trabajar: `git pull origin main`.
- No se modifica la carpeta `model` sin avisar al resto del grupo, porque cambia la base de datos.

## Datos ficticios

Todos los nombres, correos, teléfonos y organizaciones son inventados con fines académicos.
