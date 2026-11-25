# MPAZ PRO 2.º

App Android para la Escuela Escritora Marcela Paz (San Bernardo), pensada para 2.º básico. Jefatura UTP gestiona usuarios, unidades y contenidos; los docentes asignan unidades y revisan informes; los estudiantes estudian y rinden sus actividades desde el teléfono. Incluye un backend REST en Spring Boot que recibe una copia de los resultados. Proyecto de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes
- Sebastián Soto
- Martina Brandt

## Funcionalidades
- **Inicio de sesión por rol** (Jefatura UTP, Docente, Estudiante). Las contraseñas se guardan cifradas con PBKDF2 + HMAC-SHA256 y sal aleatoria.
- **Jefatura UTP**
  - Crear usuarios (estudiantes y docentes), con validaciones por campo.
  - Crear unidades y sus objetivos de aprendizaje (OA), y moverlas por los estados **Borrador → En revisión → Autorizado → Publicado → Deshabilitado**, con confirmación al publicar o deshabilitar y un **historial** de quién hizo cada cambio.
  - Crear desafíos y la prueba final de cada unidad, con preguntas de 2 a 4 alternativas. Solo se edita mientras la unidad está en Borrador.
  - Crear contenido para un objetivo de aprendizaje: texto, ejemplo, **foto con la cámara** o **audio grabado con el micrófono**.
  - Administrar cursos: crear y eliminar cursos (solo sin estudiantes), elegir al profesor jefe y al docente de cada asignatura.
- **Docente**
  - Asignar a su curso las unidades que UTP autorizó, y configurar su prueba final (instrucciones, intentos, fechas de apertura y cierre, mostrar respuestas).
  - Ver el **avance del curso** (quién rindió, intentos y mejor puntaje) y el **informe por OA** de cada estudiante: puntaje global, porcentaje y etiqueta por OA (Fortaleza / En desarrollo / Reforzar, según una regla fija), desglose de respuestas y una observación que el docente valida.
  - El profesor jefe ve todas las asignaturas de su curso; el de asignatura, solo las suyas.
  - Ver si el servidor está conectado y enviar los resultados pendientes.
- **Estudiante**
  - Abrir cada unidad y estudiar sus contenidos (texto, ejemplos, imágenes y audios), con su avance.
  - Resolver desafíos con retroalimentación inmediata y rendir la prueba final, una pregunta por pantalla, respetando intentos y fechas. Si sale a la mitad, retoma donde quedó.
- **Recursos nativos:** cámara y micrófono. Los permisos se piden solo al usarlos y se manejan los casos de rechazo, incluido el rechazo permanente (botón a Ajustes). Los archivos se guardan en el almacenamiento interno de la app.
- **Sin conexión primero:** Room es la fuente de verdad. Al terminar una actividad, la app envía una copia al servidor con Retrofit y JWT; si no hay conexión, queda pendiente y la app funciona igual.
- **Arquitectura:** MVVM, Jetpack Compose + Material 3 y persistencia local con Room. Las validaciones están separadas de la interfaz y tienen pruebas unitarias.

## Cómo ejecutar
1. Clonar el repositorio: `git clone https://github.com/Seba2134/MPAZ-PRO-app.git`
2. Abrirlo en Android Studio y esperar la sincronización de Gradle.
3. Ejecutar en un emulador o teléfono con Android 8.0 (API 26) o superior.
4. Iniciar sesión con un usuario de prueba:

| Rol | Usuario | Contraseña |
|---|---|---|
| Jefatura UTP | utp | utp123 |
| Profesora jefe (2.º A) | ana | ana123 |
| Docente de Matemática | carlos | carlos123 |
| Docente de Ciencias | daniela | daniela123 |
| Estudiantes (2.º A) | tomas, sofia, benjamin, isidora | 1234 |

Pruebas unitarias de la app: `./gradlew test`.

## Backend
API REST en `backend/` (Spring Boot, Spring Security, JPA y H2 en archivo). Recibe una copia de los resultados de la app y entrega el informe por OA. Conoce solo a los 8 usuarios de prueba.

### Cómo ejecutarlo
1. Necesita JDK 17 o superior (sirve el de Android Studio: `JAVA_HOME` = `C:\Program Files\Android\Android Studio\jbr`).
2. `cd backend` y luego `./mvnw spring-boot:run` (en Windows: `mvnw.cmd spring-boot:run`). Queda en `http://localhost:8080`.
3. Consola de la base de datos: `http://localhost:8080/h2-console` (URL `jdbc:h2:file:./data/mpazpro`, usuario `sa`, sin clave).
4. Pruebas: `./mvnw test`.

La app usa `http://10.0.2.2:8080/`, que es el computador visto desde el emulador. En un teléfono real se cambia `API_URL` en `app/build.gradle.kts` por la IP del PC y se agrega esa IP en `res/xml/network_security_config.xml`.

En Android 17 (API 37) o superior, conectarse a la red local requiere el permiso **Dispositivos cercanos / red local** (`ACCESS_LOCAL_NETWORK`): la app lo pide al abrirse. Si se rechaza, todo funciona igual y los resultados quedan pendientes.

### Cómo funciona el JWT
- Un **JWT** (JSON Web Token) es un texto firmado que el servidor entrega al iniciar sesión. Tiene 3 partes: `encabezado.contenido.firma`. El contenido lleva `sub` (el usuario), `rol` y `exp` (vence en 8 horas).
- La firma se calcula con una clave secreta del servidor (HS256): si alguien cambia una letra del token, la firma deja de coincidir y se rechaza.
- La app lo manda en cada petición como `Authorization: Bearer <token>`. Un filtro revisa la firma y el vencimiento: si está bien, deja pasar; si no, responde **401**.
- Es **stateless**: el servidor no guarda sesiones, todo va en el token. La app guarda el token solo en memoria.

### Endpoints
| Método y ruta | Quién puede | Respuestas |
|---|---|---|
| `GET /api/salud` | Cualquiera | 200 `{"estado":"ok"}` |
| `POST /api/auth/login` | Cualquiera | 200 con el token · 401 · 400 |
| `POST /api/intentos` | Estudiante (solo los suyos), Docente, UTP | 201 creado · 200 ya existía · 400 · 401 · 403 |
| `GET /api/intentos?estudiante=tomas` | Docente, UTP | 200 · 401 · 403 |
| `GET /api/intentos/{id}` | Docente, UTP | 200 · 404 |
| `GET /api/informes/estudiante/{usuario}?unidadTitulo=...` | Docente, UTP | 200 (mejor intento de la prueba final, por OA) · 403 al estudiante |

### Ejemplo con curl
```bash
# 1) Iniciar sesión y copiar el "token" de la respuesta
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombreUsuario":"ana","password":"ana123"}'

# 2) Usar el token
curl "http://localhost:8080/api/intentos?estudiante=tomas" \
  -H "Authorization: Bearer <token>"
```
En Windows (Git Bash), si el cuerpo lleva tildes, conviene guardarlo en un archivo y enviarlo con `--data-binary @intento.json`.
