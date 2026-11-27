# MPAZ PRO 2.º

App Android para la Escuela Escritora Marcela Paz (San Bernardo), pensada para 2.º básico. Jefatura UTP gestiona usuarios, unidades y contenidos; los docentes asignan unidades y revisan informes; los estudiantes estudian y rinden sus actividades desde el teléfono. Se integra con dos microservicios en Spring Boot (usuarios y resultados) y con la API pública de Wikimedia Commons. Proyecto de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes
- Sebastián Soto
- Martina Brandt

## Funcionalidades
- **Inicio de sesión por rol** (Jefatura UTP, Docente, Estudiante). Las contraseñas se guardan cifradas con PBKDF2 + HMAC-SHA256 y sal aleatoria.
- **Jefatura UTP**
  - Crear usuarios (estudiantes y docentes), con validaciones por campo.
  - Crear unidades y sus objetivos de aprendizaje (OA), y moverlas por los estados **Borrador → En revisión → Autorizado → Publicado → Deshabilitado**, con confirmación al publicar o deshabilitar y un **historial** de quién hizo cada cambio.
  - Crear desafíos y la prueba final de cada unidad, con preguntas de 2 a 4 alternativas. Solo se edita mientras la unidad está en Borrador.
  - Crear contenido para un objetivo de aprendizaje: texto, ejemplo, **foto con la cámara**, **audio grabado con el micrófono** o una **imagen libre de Wikimedia Commons**, que completa sola el autor y la licencia.
  - Administrar cursos: crear y eliminar cursos (solo sin estudiantes), elegir al profesor jefe y al docente de cada asignatura.
- **Docente**
  - Asignar a su curso las unidades que UTP autorizó, y configurar su prueba final (instrucciones, intentos, fechas de apertura y cierre, mostrar respuestas).
  - Ver el **avance del curso** (quién rindió, intentos y mejor puntaje) y el **informe por OA** de cada estudiante: puntaje global, porcentaje y etiqueta por OA (Fortaleza / En desarrollo / Reforzar, según una regla fija), desglose de respuestas y una observación que el docente valida.
  - La observación se guarda en el dispositivo y también en el servidor (envío y actualización). La tarjeta **«En el servidor»** muestra lo que el servidor recibió (intentos y observación) y permite traer al teléfono una observación escrita en otro equipo.
  - El profesor jefe ve todas las asignaturas de su curso; el de asignatura, solo las suyas.
  - Ver si cada microservicio está conectado y enviar los resultados pendientes.
- **Estudiante**
  - Abrir cada unidad y estudiar sus contenidos (texto, ejemplos, imágenes y audios), con su avance. Las imágenes libres muestran su atribución.
  - Resolver desafíos con retroalimentación inmediata y rendir la prueba final, una pregunta por pantalla, respetando intentos y fechas. Si sale a la mitad, retoma donde quedó.
- **Recursos nativos:** cámara y micrófono. Los permisos se piden solo al usarlos y se manejan los casos de rechazo, incluido el rechazo permanente (botón a Ajustes). Los archivos se guardan en el almacenamiento interno de la app.
- **Sin conexión primero:** Room es la fuente de verdad. Al terminar una actividad, la app envía una copia a ms-resultados con Retrofit y JWT; si no hay conexión, queda pendiente y la app funciona igual.
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

## Backend (microservicios)
Dos proyectos Spring Boot independientes en `backend/`, cada uno con su propia base de datos H2 en archivo, sus endpoints y su propio `mvnw`. Ninguno llama al otro: comparten solo la clave con que se firman los JWT.

| Microservicio | Puerto | Qué hace | Base de datos |
|---|---|---|---|
| `ms-usuarios` | 8081 | Inicio de sesión y emisión de los JWT. Tiene los 8 usuarios de prueba (claves con BCrypt). | `jdbc:h2:file:./data/usuarios` |
| `ms-resultados` | 8082 | Recibe los intentos, entrega el informe por OA y guarda las observaciones del docente. Solo **valida** los JWT: no tiene tabla de usuarios. | `jdbc:h2:file:./data/resultados` |

### Cómo ejecutarlos
Necesitan JDK 17 o superior (sirve el de Android Studio: `JAVA_HOME` = `C:\Program Files\Android\Android Studio\jbr`). Se abren **dos terminales**:

```powershell
# Terminal 1
cd backend\ms-usuarios
.\mvnw.cmd spring-boot:run

# Terminal 2
cd backend\ms-resultados
.\mvnw.cmd spring-boot:run
```

- Pruebas: `.\mvnw.cmd test` dentro de cada carpeta.
- Consola de la base de datos: `http://localhost:8081/h2-console` y `http://localhost:8082/h2-console` (usuario `sa`, sin clave).
- La app usa `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/` (10.0.2.2 es el computador visto desde el emulador). En un teléfono real se cambian `URL_USUARIOS` y `URL_RESULTADOS` en `app/build.gradle.kts` por la IP del PC y se agrega esa IP en `res/xml/network_security_config.xml`.
- En Android 17 (API 37) o superior, conectarse a la red local requiere el permiso **Dispositivos cercanos / red local** (`ACCESS_LOCAL_NETWORK`): la app lo pide al abrirse. Si se rechaza, todo funciona igual y los resultados quedan pendientes.

### Cómo funciona el JWT
- Un **JWT** (JSON Web Token) es un texto firmado que **ms-usuarios** entrega al iniciar sesión. Tiene 3 partes: `encabezado.contenido.firma`. El contenido lleva `sub` (el usuario), `rol` y `exp` (vence en 8 horas).
- La firma se calcula con una clave secreta (HS256). **ms-resultados** conoce la misma clave y comprueba la firma: si coincide, confía en el usuario y el rol del token sin preguntarle a ms-usuarios. Por eso sigue funcionando aunque ms-usuarios esté apagado. Si alguien cambia una letra del token, la firma deja de coincidir y se rechaza con **401**.
- La app lo manda en cada petición como `Authorization: Bearer <token>`. Es **stateless**: ningún servidor guarda sesiones. La app guarda el token solo en memoria.

### Endpoints
| Servicio | Método y ruta | Quién puede | Respuestas |
|---|---|---|---|
| ms-usuarios | `GET /api/salud` | Cualquiera | 200 `{"estado":"ok"}` |
| ms-usuarios | `POST /api/auth/login` | Cualquiera | 200 con el token · 401 · 400 |
| ms-usuarios | `GET /api/usuarios/yo` | Cualquier usuario con token | 200 (nombreUsuario, nombre, rol) · 401 |
| ms-resultados | `GET /api/salud` | Cualquiera | 200 `{"estado":"ok"}` |
| ms-resultados | `POST /api/intentos` | Estudiante (solo los suyos), Docente, UTP | 201 creado · 200 ya existía · 400 · 401 · 403 |
| ms-resultados | `GET /api/intentos?estudiante=tomas` | Docente, UTP | 200 · 401 · 403 |
| ms-resultados | `GET /api/intentos/{id}` | Docente, UTP | 200 · 404 |
| ms-resultados | `GET /api/informes/estudiante/{usuario}?unidadTitulo=...` | Docente, UTP | 200 (mejor intento de la prueba final, por OA) · 403 al estudiante |
| ms-resultados | `GET /api/observaciones?estudiante=...&unidadTitulo=...` | Docente, UTP | 200 · 404 si no existe |
| ms-resultados | `POST /api/observaciones` | Docente, UTP | 201 · 409 si ya existe (con su id) · 400 |
| ms-resultados | `PUT /api/observaciones/{id}` | Docente, UTP | 200 · 404 · 400 (si cambia el texto, se pierde la validación) |

### Ejemplo con curl
```bash
# 1) Iniciar sesión en ms-usuarios (8081) y copiar el "token" de la respuesta
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombreUsuario":"ana","password":"ana123"}'

# 2) Usar ese token en ms-resultados (8082)
curl "http://localhost:8082/api/intentos?estudiante=tomas" \
  -H "Authorization: Bearer <token>"
```
En Windows (Git Bash), si el cuerpo lleva tildes, conviene guardarlo en un archivo y enviarlo con `--data-binary @archivo.json`.

## API externa: Wikimedia Commons
Al crear un contenido de tipo Imagen, Jefatura UTP puede **buscar una imagen libre** en [Wikimedia Commons](https://commons.wikimedia.org/) con la API pública de MediaWiki (`https://commons.wikimedia.org/w/api.php`). Es gratuita y **no necesita clave**.

- Una sola búsqueda trae la miniatura, el autor y la licencia de cada imagen. Solo se muestran imágenes JPG, PNG o SVG con una licencia reconocible (Creative Commons o dominio público).
- Al elegir una, se descarga y se usa igual que una foto de la cámara. El autor queda como «Autor · Licencia · Wikimedia Commons», porque las licencias CC exigen citarlos, y el estudiante ve esa atribución bajo la imagen.
- Usa su propio cliente HTTP, con un User-Agent que identifica a la app (lo pide la política de Wikimedia), y **nunca** envía el token de nuestros microservicios.
- Sin internet, la app lo avisa y se puede seguir usando la cámara.
