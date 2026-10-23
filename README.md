# MPAZ PRO 2.º

App Android para la Escuela Escritora Marcela Paz (San Bernardo), pensada para 2.º básico. Jefatura UTP gestiona usuarios y contenidos, y los docentes configuran las pruebas de sus asignaturas. Proyecto de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes
- Sebastián Soto
- Martina Brandt

## Funcionalidades
- *Inicio de sesión por rol* (Jefatura UTP, Docente, Estudiante). Las contraseñas se guardan cifradas con PBKDF2 + HMAC-SHA256 y sal aleatoria.
- Jefatura UTP
  - Crear usuarios (estudiantes y docentes), con validaciones por campo.
  - Crear contenido para un objetivo de aprendizaje: texto, ejemplo, **foto con la cámara** o **audio grabado con el micrófono**.
- Docente: ver sus pruebas y configurarlas (instrucciones, intentos, fechas de apertura y cierre, mostrar respuestas). El profesor jefe ve todas las asignaturas de su curso; el de asignatura, solo las suyas.
- Recursos nativos: cámara y micrófono. Los permisos se piden solo al usarlos y se manejan los casos de rechazo, incluido el rechazo permanente (botón a Ajustes). Los archivos se guardan en el almacenamiento interno de la app.
- Arquitectura: MVVM, Jetpack Compose + Material 3 y persistencia local con Room. Las validaciones están separadas de la interfaz.

## Cómo ejecutar
1. Clonar el repositorio: `git clone https://github.com/Seba2134/MPAZ-PRO-app.git`
2. Abrirlo en Android Studio y esperar la sincronización de Gradle.
3. Ejecutar en un emulador o teléfono con Android 8.0 (API 26) o superior.
4. Iniciar sesión con un usuario de prueba:

| Rol | Usuario | Contraseña |
|---|---|---|
| Jefatura UTP | utp | utp123 |
| Profesora jefe | ana | ana123 |
| Docente de Matemática | carlos | carlos123 |
| Estudiante | tomas | 1234 |
