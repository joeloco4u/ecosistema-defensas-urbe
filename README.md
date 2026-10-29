# Ecosistema Digital de Defensas - URBE

Plataforma interna para la gestión integral de defensas de proyectos académicos de la Universidad Privada Dr. Rafael Belloso Chacín (URBE). Permite administrar proyectos, estudiantes, docentes, espacios físicos, horarios, jurados, agendamiento de defensas, reportes y generación de documentos institucionales.

El sistema está compuesto por una SPA en Angular, una API REST monolítica en Spring Boot y una base de datos PostgreSQL alojada en Supabase. El acceso es exclusivo para personal autorizado: no existe registro público de usuarios.

## Stack tecnológico

| Capa | Tecnología |
| --- | --- |
| Frontend | Angular 17, TypeScript, Tailwind CSS, RxJS, FullCalendar |
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Autenticación | JWT (JJWT) con BCrypt para contraseñas |
| Base de datos | PostgreSQL 15 / Supabase |
| Despliegue | Vercel (frontend), Render (backend), Supabase (base de datos) |
| Documentos | docxtemplater, PizZip, FileSaver |

## Estructura del repositorio

```text
.
├── backend/            API REST Spring Boot
├── frontend/           Aplicación Angular
├── db-migration/       Scripts auxiliares de inspección y carga de datos
├── docs/               Documentación técnica
├── docker-compose.yml  PostgreSQL local para desarrollo
└── README.md
```

## Requisitos previos

- Node.js 18+ y npm
- Java 17
- Maven 3.9+
- PostgreSQL 15 local o una instancia de Supabase

## Inicio rápido (desarrollo local)

### 1. Clonar el repositorio

```bash
git clone https://github.com/joeloco4u/ecosistema-defensas-urbe.git
cd ecosistema-defensas-urbe
```

### 2. Levantar la base de datos local

```bash
docker compose up -d
```

Esto inicia PostgreSQL en `localhost:5432` con la base `defensas_db` y el usuario definido en `docker-compose.yml`. Ajustar `spring.datasource.url`, `username` y `password` en `backend/src/main/resources/application-dev.properties` para coincidir con esas credenciales.

Si se utiliza Supabase, configurar la conexión JDBC en el mismo archivo mediante variables locales. No versionar credenciales reales.

### 3. Ejecutar el backend

```bash
cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api`. El perfil activo por defecto es `dev`.

### 4. Ejecutar el frontend

```bash
cd frontend
npm install
npm start
```

La aplicación queda disponible en `http://localhost:4200`. Para consumir un backend local, ajustar `apiUrl` en `frontend/src/environments/environment.ts` a `http://localhost:8080/api`.

### 5. Crear el usuario administrador

No existe registro público. Los usuarios se aprovisionan manualmente en la tabla `usuarios` con una contraseña cifrada con BCrypt; la API de administración de usuarios está restringida al rol `COORDINADOR`. Ver [docs/seguridad.md](docs/seguridad.md).

## Documentación técnica

- [Arquitectura](docs/arquitectura.md)
- [Seguridad](docs/seguridad.md)
- [Despliegue](docs/despliegue.md)

## Entornos

| Entorno | Frontend | Backend | Base de datos |
| --- | --- | --- | --- |
| Producción | Vercel | Render | Supabase |
| Desarrollo | `http://localhost:4200` | `http://localhost:8080` | PostgreSQL local / Supabase |

## Notas

- La documentación de usuario final queda fuera del alcance de esta versión.
- Cualquier cambio estructural, de seguridad o de lógica debe reflejarse en los archivos de `docs/`.
