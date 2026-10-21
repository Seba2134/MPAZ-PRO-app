# MPAZ PRO - Aplicación Móvil Escuela Marcela Paz (2.º Básico)

## Descripción del Proyecto
MPAZ PRO es una aplicación móvil desarrollada en **Android Studio** con **Jetpack Compose** y **Room Database**. Diseñada para apoyar el proceso pedagógico de estudiantes de 2.º básico, la gestión docente y la administración por parte de la Jefatura UTP.

## Integrantes
- **[Sebastian Soto]** - Desarrollador Principal
- **[Martina Brandt]** - Desarrollador Principal

## Funcionalidades Principales
- **Persistencia de datos (Room):** Almacenamiento local seguro de unidades, preguntas, intentos e historial de notas.
- **Autenticación y Seguridad:** Cifrado de contraseñas con PBKDF2 + HMAC-SHA256 con sal aleatoria.
- **Gestión por Roles:**
    - **Jefatura UTP:** Control del estado del contenido (Borrador -> Autorizado -> Publicado).
    - **Docente:** Asignación de evaluaciones, configuración de intentos e informes por OA.
    - **Estudiante:** Realización de desafíos y prueba final con diseño accesible.
- **Recursos Nativos:** Notificaciones locales y reproducción de audio instruccional.

## Requisitos de Ejecución
1. Android Studio Ladybug / ME o superior.
2. JDK 11 o posterior.
3. Dispositivo / Emulador con Android 8.0 (API level 26) o superior.