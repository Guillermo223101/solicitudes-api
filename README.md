# solicitudes-api — API REST de gestión de solicitudes

**Backend Spring Boot + MongoDB Atlas | Desarrollo de Aplicaciones Móviles (DSY1105)**

API REST académica para registrar usuarios y administrar solicitudes de soporte técnico. Proporciona autenticación mediante JWT y permisos según los perfiles **CLIENTE**, **ADMIN** y **TECNICO**. Es el servicio remoto consumido por la aplicación [EvaluacionDesarrolloAppMovil](https://github.com/Guillermo223101/EvaluacionDesarrolloAppMovil).

## Funcionalidades implementadas

- Registro de clientes e inicio de sesión con token JWT.
- Contraseñas almacenadas como hash; las respuestas públicas de usuario no incluyen la clave ni su hash.
- Gestión de solicitudes propias: crear, listar, actualizar y eliminar mediante rutas de CLIENTE.
- Categorías **Hardware**, **Software** y **Redes**.
- Evidencias fotográficas opcionales en **JPEG/PNG** convertidas a **Base64**; comprobaciones de formato y tamaño antes del guardado.
- Supervisión de solicitudes, cambio de prioridad y asignación de técnicos por ADMIN.
- Consulta de solicitudes asignadas y actualización de estado por TECNICO.
- Registro y consulta de comentarios de avance de los técnicos, con fecha generada en el servidor.
- Persistencia de usuarios, solicitudes y avances en colecciones MongoDB.
- Control de acceso por roles y comprobación de pertenencia/asignación en los servicios.

## Tecnologías

- Java **21**.
- Spring Boot **4.1.3**.
- Spring Web MVC, Spring Data MongoDB y Jakarta Validation.
- Spring Security y OAuth2 Resource Server para procesar JWT.
- Maven Wrapper (`mvnw.cmd` / `mvnw`).
- MongoDB Atlas como base de datos remota.
- JUnit y Mockito para pruebas automatizadas.

## Organización del proyecto

```text
src/
├── main/
│   ├── java/com/gestionsolicitudes/api/
│   │   ├── config/         # Seguridad, codificador y validador JWT
│   │   ├── controller/     # Usuarios, solicitudes y avances por rol
│   │   ├── dto/            # Solicitudes de entrada y respuestas
│   │   ├── model/          # Usuario, Solicitud, AvanceTecnico, RolUsuario
│   │   ├── repository/     # Interfaces de Spring Data MongoDB
│   │   └── service/        # Reglas de negocio y autorizaciones
│   └── resources/application.properties
└── test/java/com/gestionsolicitudes/api/
    └── service/            # Pruebas de seguridad, usuarios y solicitudes
```

### Colecciones MongoDB

| Colección | Información |
| --- | --- |
| `usuarios` | Identidad, correo, rol y hash de contraseña. |
| `solicitudes` | Título, descripción, estado, prioridad, categoría, correos relacionados y evidencia Base64 opcional. |
| `avances_tecnicos` | Identificador de la solicitud, técnico, comentario y fecha/hora. |

Las fotografías se guardan actualmente como texto Base64 en el documento de la solicitud. Esta implementación es adecuada para las pruebas académicas con imágenes pequeñas, pero no está diseñada como servicio de almacenamiento de archivos de producción.

## Requisitos y configuración local

Se necesita:

1. **JDK 21** instalado y disponible para Maven.
2. Acceso a una instancia de **MongoDB Atlas** con un usuario de base de datos autorizado.
3. Dos variables de entorno en la terminal donde se ejecutará Spring Boot: `MONGODB_URI` y `JWT_SECRET`.

`src/main/resources/application.properties` contiene:

```properties
spring.application.name=solicitudes-api
spring.mongodb.uri=${MONGODB_URI}
```

El código JWT obtiene el secreto desde `JWT_SECRET`. Debe ser una cadena **Base64 que represente al menos 32 bytes**.

### Configuración segura en PowerShell (Windows)

En una terminal nueva, desde la carpeta raíz del backend:

```powershell
# Introduce la URI completa de MongoDB Atlas de forma privada.
# La URI debe seleccionar la base de datos gestion_solicitudes.
$uriSegura = Read-Host "URI privada de MongoDB Atlas" -AsSecureString
$env:MONGODB_URI = [System.Net.NetworkCredential]::new("", $uriSegura).Password
Remove-Variable uriSegura

# Generar un secreto JWT de desarrollo con 32 bytes aleatorios
$env:JWT_SECRET = [Convert]::ToBase64String(
    [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
)

# Comprobar solamente que existen las variables, sin imprimir secretos
Write-Host "MongoDB configurado:" (-not [string]::IsNullOrWhiteSpace($env:MONGODB_URI))
Write-Host "JWT configurado:" (-not [string]::IsNullOrWhiteSpace($env:JWT_SECRET))

# Iniciar Spring Boot
.\mvnw.cmd spring-boot:run
```

La URI debe estar formada conforme a MongoDB Atlas y apuntar a `gestion_solicitudes`. Si la contraseña contiene caracteres reservados de URL, debe codificarse correctamente antes de formar la URI.

> **Advertencia:** generar un secreto JWT diferente al reiniciar el servidor invalida los tokens emitidos anteriormente. Después de reiniciar, vuelve a iniciar sesión en Android. En entornos no académicos, el secreto debe gestionarse con un almacenamiento de secretos estable y seguro.

No pegar valores reales de `MONGODB_URI`, contraseñas o `JWT_SECRET` en el README, capturas, commits ni incidencias públicas.

### Compilar y ejecutar pruebas

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd clean test
```

**Última verificación de la rama `main` (25 de noviembre de 2026): 41 pruebas ejecutadas, 0 fallos, 0 errores y `BUILD SUCCESS`.** Las pruebas de carga de contexto necesitan la configuración de entorno correspondiente.

Para iniciar el servicio:

```powershell
.\mvnw.cmd spring-boot:run
```

La API funciona localmente en **`http://localhost:8080`** con la configuración actual. Para el emulador Android, el proyecto móvil usa **`http://10.0.2.2:8080/`**.

## Endpoints principales

Las rutas que requieren autenticación deben recibir `Authorization: Bearer <TOKEN_JWT>`.

| Método | Ruta | Acceso | Función |
| --- | --- | --- | --- |
| `POST` | `/api/usuarios/registro` | Público | Registrar una cuenta CLIENTE. |
| `POST` | `/api/usuarios/login` | Público | Autenticar y obtener JWT. |
| `GET` | `/api/usuarios/me` | Autenticado | Consultar identidad y autoridades. |
| `GET` | `/api/solicitudes/mis` | CLIENTE | Listar solicitudes propias. |
| `POST` | `/api/solicitudes/mis` | CLIENTE | Crear solicitud propia. |
| `PUT` | `/api/solicitudes/mis/{id}` | CLIENTE | Actualizar contenido y categoría. |
| `DELETE` | `/api/solicitudes/mis/{id}` | CLIENTE | Eliminar una solicitud propia. |
| `GET` | `/api/solicitudes/admin` | ADMIN | Supervisar solicitudes. |
| `PATCH` | `/api/solicitudes/admin/{id}/prioridad` | ADMIN | Cambiar prioridad. |
| `PATCH` | `/api/solicitudes/admin/{id}/tecnico` | ADMIN | Asignar un técnico válido. |
| `GET` | `/api/solicitudes/tecnico` | TECNICO | Consultar solicitudes asignadas. |
| `PATCH` | `/api/solicitudes/tecnico/{id}/estado` | TECNICO | Actualizar estado de solicitud asignada. |
| `POST` | `/api/solicitudes/tecnico/{id}/avances` | TECNICO | Registrar avance. |
| `GET` | `/api/solicitudes/tecnico/{id}/avances` | TECNICO | Obtener historial de avances. |

Las rutas CRUD genéricas antiguas `/api/solicitudes` y `/api/solicitudes/{id}` se **deniegan** en la configuración de seguridad actual, aunque su controlador histórico siga en el repositorio. Utilizar los endpoints por perfil de la tabla.

### Ejemplos de cuerpos JSON

**Registro de usuario (CLIENTE):**

```json
{
  "nombre": "Cliente de prueba",
  "correo": "cliente.demo@example.com",
  "clave": "UnaClaveDePruebaSegura",
  "direccion": "Santiago"
}
```

**Crear una solicitud autenticada:**

```json
{
  "titulo": "No funciona la impresora",
  "descripcion": "El equipo no reconoce la impresora de red",
  "categoria": "Redes",
  "evidenciaBase64": null
}
```

**Cambiar prioridad (ADMIN):**

```json
{ "prioridad": "ALTA" }
```

**Asignar técnico (ADMIN):**

```json
{ "correoTecnico": "tecnico.demo@example.com" }
```

El técnico debe existir y tener rol `TECNICO` en la base de datos. El registro público no permite autoconcederse permisos administrativos ni técnicos.

**Registrar avance (TECNICO):**

```json
{ "comentario": "Se revisó el equipo y se comprobó la conexión" }
```

La identidad del técnico y la fecha de registro las determina el servidor, no el cuerpo JSON enviado por Android.

## Seguridad

- Las cuentas registradas públicamente reciben el rol `CLIENTE`.
- JWT firmado mediante HS256; permisos validados por Spring Security.
- Comprobación adicional en servicios para impedir operaciones sobre solicitudes ajenas o no asignadas.
- Contraseñas tratadas mediante un codificador de hash, no devueltas en las respuestas de registro/login.
- Se aplica validación de entrada con DTO y Jakarta Validation.
- Para las evidencias se valida Base64, firma JPEG/PNG, posibilidad de decodificar la imagen y tamaño máximo del archivo decodificado de **1 MiB**.
- La implementación actual es un prototipo de laboratorio: no debe exponerse directamente en Internet sin HTTPS, gestión de secretos, monitoreo y endurecimiento adicional.

## Pruebas y evidencia de funcionamiento

La suite de backend cubre lógica de usuarios, JWT, restricciones de seguridad, solicitudes, prioridades, asignaciones, estados y avances de técnicos. La última ejecución desde `main` terminó con **41 pruebas exitosas**.

También se verificó manualmente la creación de solicitudes con categoría y fotografía desde Android, su almacenamiento en MongoDB Atlas y la lectura desde los tres perfiles.

> La cantidad de pruebas aprobadas no equivale automáticamente a un porcentaje de cobertura de código. No se declara cobertura del 80 % sin un informe de medición.

## Repositorios y colaboración

- **Backend:** [solicitudes-api](https://github.com/Guillermo223101/solicitudes-api)
- **Aplicación Android:** [EvaluacionDesarrolloAppMovil](https://github.com/Guillermo223101/EvaluacionDesarrolloAppMovil)
- **Integración backend aprobada y fusionada:** [Pull Request #1](https://github.com/Guillermo223101/solicitudes-api/pull/1)
- **Integración Android aprobada y fusionada:** [Pull Request #1](https://github.com/Guillermo223101/EvaluacionDesarrolloAppMovil/pull/1)

**Equipo:** Guillermo Miranda y Matías (`Matito15`). Desarrollo colaborativo mediante ramas, revisión de cambios y aprobación de Pull Requests.

**Alcance:** servicio para la evaluación académica de desarrollo móvil. Las tareas de generación y firma de APK reservadas para el examen no forman parte de este backend.
