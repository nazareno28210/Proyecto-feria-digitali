# Feria Digital

Sistema web para la gestion y visualizacion de ferias locales, desarrollado como proyecto del CENT 35.

## Descripcion

Feria Digital es una plataforma que conecta a feriantes con usuarios, permitiendo la gestion de ferias, stands y productos. Los administradores pueden crear y gestionar ferias, mientras que los feriantes pueden registrar sus productos y participar en eventos.

---

## Tecnologias Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security** - Autenticacion y autorizacion
- **Spring Data JPA** - Persistencia de datos
- **Thymeleaf** - Motor de plantillas
- **MySQL 8** - Base de datos relacional

### Frontend
- **HTML5 / CSS3 / JavaScript**
- **Leaflet.js** - Mapas interactivos
- **Toastify** - Notificaciones
- **Bootstrap Icons / Font Awesome** - Iconografia

### Servicios Externos
- **Cloudinary** - Almacenamiento de imagenes
- **Gmail SMTP** - Envio de correos electronicos

---

## Arquitectura del Sistema

```
src/main/java/com/mansilla_nazareno/feriadigital/feriadigital/
├── configurations/     # Configuraciones de seguridad y beans
├── controllers/        # Controladores REST
│   ├── Admin/          # Endpoints de administrador
│   ├── Feriante/       # Endpoints de feriante
│   └── UsuarioComun/   # Endpoints de usuario
├── dto/                # Data Transfer Objects
├── models/             # Entidades JPA
│   ├── Admin/          # Feria, Stand, Participacion
│   ├── Feriante/       # Producto, Categoria, ImagenProducto
│   └── UsuarioComun/   # Usuario, Resena, Solicitud
├── repositories/       # Interfaces JPA Repository
└── services/           # Logica de negocio

src/main/resources/static/web/
├── css/                # Hojas de estilo
├── js/                 # Scripts JavaScript
├── admin/              # Paginas del administrador
├── feriante/           # Paginas del feriante
└── *.html              # Paginas publicas
```

---

## Requerimientos Funcionales

### RF01 - Gestion de Usuarios
- Registro de usuarios con verificacion por email
- Inicio de sesion con email y contrasena
- Recuperacion de contrasena via email
- Tres tipos de usuario: NORMAL, FERIANTE, ADMINISTRADOR

### RF02 - Gestion de Ferias
- Crear, editar y eliminar ferias (soft delete)
- Asignar ubicacion geografica (latitud/longitud)
- Visualizar ferias en mapa interactivo
- Estados de feria: activa, finalizada, cancelada

### RF03 - Gestion de Stands
- Asignar stands a feriantes en una feria
- Gestionar participaciones (PENDIENTE, APROBADA, RECHAZADA)

### RF04 - Gestion de Productos
- CRUD de productos por feriante
- Subir multiples imagenes por producto (max 6)
- Categorizar productos
- Tipos de venta: unidad, peso, medida

### RF05 - Solicitudes de Feriante
- Usuarios normales pueden solicitar ser feriantes
- Administradores aprueban/rechazan solicitudes
- Notificacion por email del resultado

### RF06 - Resenas
- Usuarios pueden valorar ferias
- Sistema de puntuacion

---

## Requerimientos No Funcionales

### RNF01 - Seguridad
- Contrasenas hasheadas con BCrypt
- Sesiones manejadas con Spring Security
- Proteccion CSRF deshabilitada para API REST
- Rutas protegidas segun rol de usuario

### RNF02 - Rendimiento
- Carga de imagenes limitada a 10MB por archivo
- Maximo 50MB por request (multiples imagenes)
- Borrado logico para mantener integridad referencial

### RNF03 - Usabilidad
- Interfaz responsiva (mobile-first)
- Notificaciones visuales con Toastify
- Validaciones en cliente y servidor

### RNF04 - Disponibilidad
- Base de datos MySQL con conexion configurable
- Imagenes almacenadas en Cloudinary (CDN)

### RNF05 - Mantenibilidad
- Arquitectura MVC
- Separacion de responsabilidades por capas
- Codigo organizado por dominio (Admin, Feriante, Usuario)

---

## Instalacion y Ejecucion

### Prerrequisitos

- **Java 17** o superior
- **MySQL 8.0** o superior
- **Gradle 8.x** (incluido via wrapper)

### Configuracion de Base de Datos

1. Crear la base de datos:
```sql
CREATE DATABASE feriadigital;
```

2. Configurar credenciales en `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/feriadigital?useSSL=false&serverTimezone=UTC
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contrasena
```

### Variables de Entorno (Opcionales)

Para mayor seguridad, usar variables de entorno en lugar de valores hardcodeados:

```properties
# Cloudinary
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}

# Email
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

### Ejecucion

```bash
# Clonar repositorio
git clone https://github.com/nazareno28210/Proyecto-feria-digitali.git
cd Proyecto-feria-digitali

# Ejecutar con Gradle
./gradlew bootRun
```

La aplicacion estara disponible en: `http://localhost:8080`

### Ejecucion con Docker (Opcional)

```bash
# Construir imagen
docker build -t feria-digital .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=3306 \
  -e DB_NAME=feriadigital \
  -e DB_USER=root \
  -e DB_PASS=password \
  feria-digital
```

---

## Endpoints Principales

### Autenticacion
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/login` | Iniciar sesion |
| POST | `/api/logout` | Cerrar sesion |
| POST | `/api/usuarios/registrarse` | Registrar usuario |
| GET | `/api/usuarios/current` | Usuario actual |

### Ferias
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/ferias` | Listar ferias |
| GET | `/api/ferias/{id}` | Detalle de feria |
| POST | `/api/ferias` | Crear feria (ADMIN) |
| PUT | `/api/ferias/{id}` | Editar feria (ADMIN) |
| DELETE | `/api/ferias/{id}` | Eliminar feria (ADMIN) |

### Productos
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/productos` | Listar productos |
| POST | `/api/productos` | Crear producto (FERIANTE) |
| PUT | `/api/productos/{id}` | Editar producto (FERIANTE) |
| DELETE | `/api/productos/{id}` | Eliminar producto (FERIANTE) |

### Usuarios
| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| GET | `/api/usuarios` | Listar usuarios (ADMIN) |
| PUT | `/api/usuarios/editar` | Editar perfil |
| POST | `/auth/forgot-password` | Solicitar reset de contrasena |
| POST | `/auth/reset-password` | Cambiar contrasena |

---

## Modelo de Datos

### Entidades Principales

```
Usuario
├── id (int)
├── nombre (string)
├── apellido (string)
├── email (string, unique)
├── contrasena (string, bcrypt)
├── tipoUsuario (NORMAL | FERIANTE | ADMINISTRADOR)
├── estadoUsuario (ACTIVO | INACTIVO | SUSPENDIDO)
├── enabled (boolean)
└── fechaRegistro (date)

Feria
├── id (int)
├── nombre (string)
├── descripcion (string)
├── lugar (string)
├── fechaInicio (date)
├── fechaFinal (date)
├── estado (string)
├── latitud (double)
├── longitud (double)
├── imagenUrl (string)
└── eliminado (boolean)

Producto
├── id (int)
├── nombre (string)
├── descripcion (string)
├── precio (double)
├── categoria (CategoriaProducto)
├── tipoVenta (UNIDAD | PESO | MEDIDA)
├── unidadMedida (string)
├── activo (boolean)
├── eliminado (boolean)
└── imagenes (List<ImagenProducto>)
```

---

## Roles y Permisos

| Funcionalidad | NORMAL | FERIANTE | ADMINISTRADOR |
|---------------|--------|----------|---------------|
| Ver ferias | Si | Si | Si |
| Ver productos | Si | Si | Si |
| Crear productos | No | Si | No |
| Gestionar ferias | No | No | Si |
| Aprobar solicitudes | No | No | Si |
| Solicitar ser feriante | Si | No | No |
| Participar en ferias | No | Si | No |

---

## Estructura de Paginas

```
/web/login.html              # Inicio de sesion
/web/registro.html           # Registro de usuario
/web/forgot-password.html    # Recuperar contrasena
/web/reset-password.html     # Restablecer contrasena
/web/ferias.html             # Listado de ferias (publica)
/web/feria_detalle.html      # Detalle de feria
/web/usuario-perfil.html     # Perfil de usuario normal
/web/feriante/perfil.html    # Dashboard del feriante
/web/admin/dashboard.html    # Dashboard del administrador
/web/admin/solicitudes.html  # Gestion de solicitudes
```

---

## Equipo de Desarrollo

**Proyecto realizado por estudiantes del CENT 35**

- Nazareno Mansilla

---

## Licencia

Este proyecto es de uso educativo y fue desarrollado como parte del curriculum del CENT 35.

---

## Notas Adicionales

- El sistema usa `spring.jpa.hibernate.ddl-auto=create-drop` en desarrollo, lo que recrea las tablas en cada reinicio. Cambiar a `update` o `validate` en produccion.
- Las credenciales de Cloudinary y Gmail en `application.properties` deben ser reemplazadas por variables de entorno en produccion.
- El puerto por defecto es 8080, configurable en `application.properties`.
