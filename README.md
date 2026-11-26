# ORDINATIO — Personal Organizer

Aplicación de escritorio para la gestión de tareas, eventos, recordatorios y categorías, desarrollada en Java Swing.

## Descripción

ORDINATIO es un proyecto académico individual orientado a la organización personal. Permite crear un perfil local y gestionar actividades desde una interfaz gráfica de escritorio.

## Funcionalidades implementadas

- **Inicio:** acceso a las secciones principales de la aplicación.
- **Tareas:** registro y consulta de tareas.
- **Recordatorios:** gestión de recordatorios y consulta de notificaciones programadas.
- **Eventos:** registro y consulta de eventos.
- **Categorías:** organización de elementos mediante categorías.
- **Persistencia local:** almacenamiento y recuperación de información mediante serialización de objetos Java en `dia.dat`.

> **Alcance actual:** las notificaciones se gestionan dentro de la aplicación; esta versión no implementa avisos automáticos del sistema operativo. El acceso a perfiles se realiza mediante correo electrónico y no constituye autenticación protegida por contraseña. La serialización Java debe utilizarse únicamente con archivos de datos de confianza.

## Tecnologías

- Java 21 (configuración objetivo del proyecto)
- Java Swing
- Apache NetBeans y su GUI Builder
- Apache Ant
- Git y GitHub

## Arquitectura y estructura

El código está organizado por responsabilidades en los paquetes `modelo`, `vista`, `controlador` y `persistencia`. Los recursos gráficos se encuentran en `src/recursos`. La navegación entre vistas utiliza `CardLayout`.

```text
ORDINATIO/
├── nbproject/
├── src/
│   ├── controlador/
│   ├── modelo/
│   ├── persistencia/
│   ├── recursos/
│   └── vista/
├── .gitattributes
├── .gitignore
├── build.xml
└── manifest.mf
```

## Requisitos y ejecución

1. Instalar un JDK 21 o compatible.
2. Abrir el proyecto desde Apache NetBeans.
3. Verificar que la plataforma Java configurada corresponda al JDK instalado.
4. Ejecutar la clase principal `vista.VentanaPrincipal` desde el IDE.

También puede construirse con Apache Ant desde la raíz del proyecto mediante `ant clean jar`, siempre que Ant y el JDK estén correctamente configurados.

## Estado del proyecto

Proyecto académico en etapa de documentación y preparación para su publicación como portafolio. No se presenta como producto comercial ni como aplicación de producción.

## Mejoras futuras

- Incorporar autenticación segura si el proyecto evoluciona hacia múltiples usuarios.
- Evaluar una alternativa de persistencia basada en base de datos.
- Implementar avisos del sistema operativo.
- Añadir pruebas automatizadas y ampliar la validación de datos.

---

**ORDINATIO — Personal Organizer**
