# AGENTS.md — NTT DATA Full Stack Agent

## 1. Propósito

Este archivo contiene las instrucciones permanentes para los agentes de IA que trabajen en este repositorio.

El objetivo es completar el desafío Full Stack a partir del mockup y catálogo entregados, manteniendo trazabilidad, calidad técnica y supervisión humana.

IMPORTANTE:

Este documento describe el proyecto completo, pero NO autoriza al agente a implementar automáticamente todo el roadmap.

El agente debe trabajar únicamente sobre la etapa o tarea explícitamente autorizada.

Al finalizar CADA ETAPA debe detenerse, presentar resultados y solicitar revisión antes de continuar.

---

# 2. Contexto del proyecto

El repositorio parte de material proporcionado para un ejercicio de especialización con NTT DATA.

Material original:

- `mockup/`
    - `index.html`
    - `styles.css`
    - `app.js`
    - `assets/`
- `data/`
    - `catalog.csv`
    - `README.md`

El mockup representa una vitrina de productos.

El frontend consulta el catálogo mediante la API. La Etapa 7 conecta los estados reales de interfaz y elimina los controles demostrativos; está aprobada y versionada por el equipo humano.

La aplicación final debe convertir este mockup en una aplicación Full Stack funcional.

---

# 3. Objetivo funcional

Construir una aplicación que permita explorar el catálogo completo proporcionado.

Debe permitir:

1. Visualizar productos.
2. Buscar productos por nombre.
3. Filtrar productos por categoría.
4. Filtrar productos por formato.
5. Combinar búsqueda y filtros.
6. Paginar resultados.
7. Consultar el detalle de un producto.
8. Obtener filtros desde los datos reales.
9. Gestionar estados reales de interfaz.
10. Consumir los datos mediante una API backend.

La búsqueda, filtros y paginación deben resolverse en backend.

---

# 4. Fuente de verdad

Las fuentes de verdad del proyecto son:

1. La consigna original del desafío.
2. `data/catalog.csv`.
3. `data/README.md`.
4. El mockup entregado.
5. Este `AGENTS.md`.
6. Las decisiones explícitamente aprobadas posteriormente por el equipo.

No inventar productos, campos, requisitos o reglas de negocio.

Si existe contradicción o una decisión no está definida, detenerse e informarla como decisión pendiente.

---

# 5. Datos verificados

El catálogo entregado contiene:

- 4.032 productos.
- 12 columnas.
- IDs únicos.
- 26 categorías principales.
- 252 formatos.
- Moneda CLP.
- Sin campos vacíos en el dataset actual.

Campos:

- `id`
- `name`
- `description`
- `format`
- `category`
- `price`
- `priceUnit`
- `originalPrice`
- `currency`
- `imageUrl`
- `productUrl`
- `extractedAt`

Existen IDs con variantes, por ejemplo:

- `12049.1`
- `12049.2`

Por esta razón:

**El ID debe tratarse siempre como String.**

Nunca convertir el identificador a entero.

Los nombres de productos tampoco son identificadores únicos.

---

# 6. Reglas sobre datos

## 6.1 Catálogo

`data/catalog.csv` es la fuente principal de productos.

No modificar:

- `data/catalog.csv`
- `data/README.md`

salvo autorización explícita.

## 6.2 CSV

El CSV debe procesarse mediante un parser correcto.

NO utilizar:

`split(",")`

para procesar filas.

El archivo contiene textos, comas y campos entrecomillados.

## 6.3 Precios

Los precios entregados ya están convertidos a CLP.

NO:

- consultar tipos de cambio;
- convertir monedas;
- modificar precios;
- asumir que `originalPrice` está expresado en otra moneda.

## 6.4 Categorías

`category` contiene una ruta.

Ejemplo:

`Huevos, leche y mantequilla > Leche y bebidas vegetales`

Para el filtro principal se utilizará:

`Huevos, leche y mantequilla`

es decir, el texto anterior al primer `>`.

La ruta completa original debe conservarse.

## 6.5 Formatos

El formato se filtra mediante coincidencia exacta.

No agrupar ni normalizar formatos sin aprobación.

## 6.6 Imágenes

La aplicación utilizará `imageUrl`.

Si una imagen externa falla, el frontend deberá mostrar un fallback visual.

---

# 7. Stack aprobado

Backend:

- Java
- Spring Boot
- Maven
- Maven Wrapper
- Spring Web
- Spring Validation
- Apache Commons CSV
- JUnit / Spring Boot Test

Frontend inicial:

- HTML
- CSS
- JavaScript

No incorporar por defecto:

- MySQL
- PostgreSQL
- JPA
- Hibernate
- JDBC
- React
- Angular
- Vue
- Lombok

La incorporación de nuevas tecnologías requiere aprobación previa.

---

# 8. Arquitectura aprobada

Flujo principal:

Frontend HTML/CSS/JavaScript
|
v
REST API
|
v
ProductController
|
v
ProductService
|
v
ProductRepository
|
v
CsvProductLoader
|
v
data/catalog.csv

Componentes previstos:

- `CatalogApplication`
- `CatalogProperties`
- `Product`
- `CsvProductLoader`
- `ProductRepository`
- `ProductService`
- `ProductController`

DTO:

- `ProductResponse`
- `ProductPageResponse`
- `ProductFiltersResponse`

Errores:

- `CatalogLoadException`
- `ProductNotFoundException`
- `ApiExceptionHandler`

No crear componentes adicionales innecesarios sin justificar su necesidad.

---

# 9. Estrategia de almacenamiento

En esta versión no se utilizará base de datos.

El catálogo deberá:

1. cargarse al iniciar la aplicación;
2. validarse;
3. mantenerse en memoria;
4. conservar un índice por ID para búsquedas de detalle.

NO leer nuevamente todo el CSV por cada petición HTTP.

El catálogo actual de 4.032 registros puede procesarse en memoria.

---

# 10. API mínima

## GET /api/products

Responsabilidad:

- listar productos;
- buscar;
- filtrar;
- paginar.

Parámetros:

- `search`
- `category`
- `format`
- `page`

`search`:

- opcional;
- búsqueda parcial sobre `name`;
- sin distinguir mayúsculas/minúsculas.

Categoría:

- coincidencia con categoría principal.

Formato:

- coincidencia exacta.

Los filtros deben combinarse mediante AND.

Ejemplo:

`search + category + format`

Paginación:

- comienza en página 0;
- tamaño fijo inicial: 8 productos.

El backend debe devolver metadatos suficientes para construir la navegación.

Respuesta conceptual:

- `items`
- `page`
- `size`
- `totalElements`
- `totalPages`
- `hasPrevious`
- `hasNext`

El tamaño inicial de página es 8.

Con 4.032 productos sin filtros:

4032 / 8 = 504 páginas.

---

## GET /api/products/{id}

Debe:

- buscar por ID exacto;
- aceptar IDs como `12049.1`;
- devolver 200 si existe;
- devolver 404 si no existe.

El ID nunca debe interpretarse numéricamente.

---

## GET /api/products/filters

Debe obtener sus valores desde el catálogo real.

Debe proporcionar:

- categorías principales;
- formatos.

No escribir manualmente estas listas en frontend.

Las opciones visuales:

- “Todas las categorías”
- “Todos los formatos”

pertenecen al frontend y no al dataset.

La ruta `/api/products/filters` debe funcionar correctamente y no ser interpretada como un `{id}`.

---

# 11. Orden de resultados

El orden debe ser estable y reproducible.

Inicialmente se conservará el orden del CSV.

No introducir ordenamientos diferentes sin aprobación.

---

# 12. Estados de interfaz obligatorios

La aplicación manejará CUATRO estados reales.

## LOADING

Condición:

La petición está pendiente.

Comportamiento:

Mostrar indicador de carga.

---

## SUCCESS

Condición:

La API responde correctamente y existen productos.

Comportamiento:

Mostrar:

- catálogo;
- productos;
- contador;
- filtros;
- paginación.

SUCCESS corresponde al estado normal de funcionamiento.

No requiere una pantalla independiente.

---

## EMPTY

Condición:

La API responde correctamente pero no existen coincidencias.

Ejemplo:

`items = []`

Comportamiento:

Mostrar el estado “Sin resultados”.

Debe permitir limpiar filtros cuando corresponda.

---

## ERROR

Condición:

Existe fallo de:

- red;
- API;
- procesamiento.

Comportamiento:

Mostrar estado de error y opción:

`Reintentar`

---

# 13. Integración del frontend

El diseño entregado debe conservarse en la medida de lo posible.

La integración de datos reales de la Etapa 6 está aprobada y versionada. Los estados reales de la Etapa 7 están aprobados y versionados por el equipo humano.

Durante la integración deberá:

- eliminarse la dependencia del arreglo local de productos;
- utilizar `fetch`;
- consultar `/api/products`;
- consultar `/api/products/filters`;
- consultar `/api/products/{id}`;
- utilizar `imageUrl`;
- utilizar los metadatos reales de paginación;
- conectar los cuatro estados reales;
- implementar Reintentar;
- implementar Limpiar filtros.

Al cambiar búsqueda o filtros:

`page = 0`

El frontend no debe realizar búsqueda, filtros o paginación sobre los 4.032 productos.

Estas operaciones pertenecen al backend.

---

# 14. Seguridad de presentación

Los textos provenientes del catálogo deben insertarse de forma segura.

No interpolar contenido no confiable directamente como HTML.

Mantener separación entre datos y presentación.

---

# 15. Manejo de errores

Como mínimo:

- ID inexistente -> 404
- page inválido -> 400
- error inesperado -> 500
- CSV ausente o ilegible -> error de inicio claro
- CSV estructuralmente inválido -> error de inicio claro
- imagen externa inaccesible -> fallback frontend

No ocultar errores importantes silenciosamente.

No descartar productos inválidos sin informar.

---

# 16. Testing obligatorio

Las pruebas forman parte del desarrollo y no son opcionales.

Deben cubrir progresivamente:

## CSV

- campos entrecomillados;
- comas internas;
- Unicode;
- IDs con sufijos;
- campos requeridos;
- precios;
- categorías.

## Service

- búsqueda parcial;
- mayúsculas/minúsculas;
- categoría;
- formato;
- filtros combinados;
- paginación;
- cero resultados;
- páginas límite.

## API

- listado;
- filtros;
- detalle;
- ID inexistente;
- `/filters`;
- parámetros inválidos.

## Dataset real

Verificar:

- 4.032 registros;
- 26 categorías principales;
- 252 formatos;
- 504 páginas sin filtros con tamaño 8.

## Frontend

Verificar:

- Loading
- Success
- Empty
- Error
- Reintentar
- Limpiar filtros
- navegación
- detalle
- imágenes fallidas
- escritorio
- móvil

---

# 17. Roadmap

## ETAPA 0 — Material original y repositorio

Estado:

COMPLETADA

Incluye:

- repositorio Git;
- material original;
- `.gitignore`;
- sincronización inicial con GitHub.

---

## ETAPA 1 — Proyecto base Spring Boot / Maven

Estado:

APROBADA Y VERSIONADA

Objetivos:

- `pom.xml`
- Maven Wrapper
- `CatalogApplication`
- `application.properties`
- dependencias mínimas
- compilación correcta

NO implementar todavía lógica de productos.

---

## ETAPA 2 — Modelo y CsvProductLoader

Estado:

APROBADA Y VERSIONADA

Objetivos:

- modelo Product;
- carga UTF-8;
- Apache Commons CSV;
- tipos correctos;
- validaciones;
- categoryGroup;
- pruebas del loader.

---

## ETAPA 3 — ProductRepository

Estado:

APROBADA Y VERSIONADA

Objetivos:

- cargar catálogo una vez;
- colección inmutable;
- índice por ID;
- orden estable;
- verificar 4.032 registros.

---

## ETAPA 4 — ProductService

Estado:

APROBADA Y VERSIONADA

Objetivos:

- búsqueda;
- categoría;
- formato;
- combinación;
- paginación;
- filtros disponibles;
- detalle.

---

## ETAPA 5 — REST API

Estado:

APROBADA Y VERSIONADA

Objetivos:

- ProductController;
- DTO;
- manejo de errores;
- GET /api/products;
- GET /api/products/{id};
- GET /api/products/filters;
- pruebas HTTP.

---

## ETAPA 6 — Integración frontend

Estado:

APROBADA Y VERSIONADA

Objetivos:

- reemplazar datos simulados;
- fetch API;
- filtros reales;
- búsqueda real;
- paginación real;
- detalle real;
- imágenes reales.

---

## ETAPA 7 — Estados de interfaz

Estado:

APROBADA Y VERSIONADA

Implementar y verificar:

- Loading
- Success
- Empty
- Error
- Reintentar
- Limpiar filtros

Eliminar controles puramente demostrativos cuando ya no sean necesarios.

---

## ETAPA 8 — Testing integral

Estado:

PRUEBAS AUTOMATIZADAS EJECUTADAS / VERIFICACIÓN VISUAL PENDIENTE / PENDIENTE DE REVISIÓN HUMANA

Objetivos:

- pruebas unitarias;
- pruebas integración;
- API;
- dataset;
- frontend;
- regresión;
- casos límite.

---

## ETAPA 9 — Documentación y entrega

Estado:

PENDIENTE

Objetivos:

- README;
- requisitos;
- arquitectura;
- instrucciones de ejecución;
- API;
- decisiones técnicas;
- pruebas;
- evidencias;
- limitaciones;
- uso del agente;
- preparación de entrega.

---

# 18. Regla crítica de control de alcance

El roadmap NO constituye autorización para ejecutar todas las etapas.

El agente SOLO puede trabajar sobre la etapa explícitamente solicitada en el prompt actual.

Ejemplo:

Si el usuario solicita:

“Implementa Etapa 2”

el agente NO debe comenzar Etapa 3.

Nunca avanzar automáticamente a otra etapa.

---

# 19. Revisión obligatoria al finalizar CADA etapa

Esta regla es obligatoria.

Cuando el agente termine una etapa debe DETENERSE.

NO debe continuar con la siguiente.

Debe entregar un REPORTE DE REVISIÓN con exactamente estas secciones:

## REPORTE DE ETAPA

### 1. Etapa ejecutada

Indicar número y nombre.

### 2. Objetivo

Explicar brevemente qué debía conseguirse.

### 3. Archivos creados

Enumerar todos.

### 4. Archivos modificados

Enumerar todos.

### 5. Implementación realizada

Explicar concretamente qué se implementó.

### 6. Pruebas ejecutadas

Indicar comandos utilizados.

### 7. Resultado de pruebas

Indicar:

- PASS
- FAIL

y explicar cualquier fallo.

### 8. Criterios de aceptación

Mostrar cada criterio como:

- PASS
- FAIL
- PENDIENTE

### 9. Problemas encontrados

Indicar errores, limitaciones o riesgos.

### 10. Decisiones pendientes

Indicar cualquier decisión que necesite aprobación humana.

### 11. Git status

Mostrar el estado del repositorio.

### 12. Próxima etapa sugerida

Indicar cuál correspondería según roadmap.

IMPORTANTE:

Solo sugerirla.

NO EJECUTARLA.

### 13. Solicitud de revisión

Finalizar siempre indicando:

`ETAPA FINALIZADA. PENDIENTE DE REVISIÓN Y APROBACIÓN HUMANA.`

Después detenerse.

---

# 20. Prohibición de aprobación automática

El agente NO puede declararse a sí mismo:

- aprobado;
- aceptado;
- listo para producción;
- autorizado para continuar.

Puede informar que las pruebas pasan.

La aprobación de una etapa corresponde al equipo humano.

---

# 21. Git

El agente puede utilizar comandos Git de solo lectura para inspección.

Por defecto NO debe ejecutar automáticamente:

- `git add`
- `git commit`
- `git push`
- `git reset --hard`
- `git clean`
- rebase
- merge

salvo autorización explícita.

Los commits representan puntos de aprobación humana.

Antes de solicitar aprobación debe mostrar `git status`.

---

# 22. Protección de material original

No modificar ni eliminar arbitrariamente:

- `data/catalog.csv`
- `data/README.md`
- imágenes originales;
- archivos base del desafío.

El mockup puede modificarse únicamente cuando llegue la etapa de integración frontend o cuando exista autorización explícita.

---

# 23. Cambios fuera de alcance

Si durante una etapa el agente detecta una mejora interesante pero que pertenece a otra etapa:

1. NO implementarla.
2. Registrarla como recomendación.
3. Continuar únicamente con el alcance autorizado.

---

# 24. Decisiones arquitectónicas nuevas

Si aparece una decisión no definida, por ejemplo:

- incorporar base de datos;
- cambiar framework;
- agregar autenticación;
- cambiar contrato API;
- agregar Docker;
- introducir React;
- modificar estructura general;

el agente debe detener esa decisión y presentarla para aprobación.

No asumir autorización.

---

# 25. Calidad

El código debe priorizar:

- claridad;
- mantenibilidad;
- nombres descriptivos;
- responsabilidades separadas;
- simplicidad;
- pruebas;
- manejo explícito de errores.

Evitar sobreingeniería.

No crear abstracciones sin necesidad concreta.

---

# 26. Definition of Done general

Una etapa solo puede presentarse como TERMINADA PARA REVISIÓN cuando:

- su alcance autorizado está implementado;
- compila cuando corresponda;
- las pruebas correspondientes fueron ejecutadas;
- no modificó material fuera de alcance;
- los errores encontrados están documentados;
- los criterios de aceptación fueron revisados;
- `git status` fue informado;
- se generó el reporte de etapa;
- el agente se detuvo.

Terminado para revisión NO significa aprobado.

---

# 27. Definition of Done final

El proyecto completo deberá:

- utilizar los 4.032 productos;
- preservar IDs como String;
- utilizar los datos proporcionados;
- ofrecer búsqueda backend;
- ofrecer filtros backend;
- ofrecer paginación backend;
- ofrecer detalle;
- obtener filtros desde datos reales;
- conservar el diseño esencial del mockup;
- manejar Loading;
- manejar Success;
- manejar Empty;
- manejar Error;
- ofrecer Reintentar;
- ofrecer Limpiar filtros;
- manejar imágenes fallidas;
- contar con pruebas;
- compilar correctamente;
- ejecutarse de forma reproducible;
- estar documentado.

---

# 28. Principio operativo del agente

El agente debe seguir siempre este ciclo:

ANALIZAR
↓
IMPLEMENTAR SOLO EL ALCANCE AUTORIZADO
↓
COMPILAR / PROBAR
↓
REVISAR RESULTADOS
↓
GENERAR REPORTE
↓
DETENERSE
↓
ESPERAR APROBACIÓN HUMANA

Nunca:

ANALIZAR
↓
IMPLEMENTAR TODO EL ROADMAP

---

# 29. Situación actual

El repositorio contiene el material original y las Etapas 1 a 7 aprobadas y versionadas por el equipo humano, según autorización explícita del usuario.

La Etapa 8 — Testing integral es la única etapa autorizada en la tarea actual. Pasaron 89 pruebas Java y 16 pruebas de frontend, incluyendo el recorrido HTTP de las 504 páginas, regresión y casos límite. La verificación visual de escritorio y móvil permanece PENDIENTE: el inventario no muestra navegadores conectados y el navegador integrado no está disponible. La etapa no cumple aún todos los criterios de cierre. El detalle está en reports/etapa-8.md.

La Etapa 9 permanece pendiente y no está autorizada.

Se entrega el reporte de la Etapa 8 con la limitación visual explícita y se detiene el trabajo para revisión humana, sin aprobar automáticamente la etapa ni avanzar a la Etapa 9.

---

# 30. Instrucción final

Ante cualquier duda entre:

- avanzar automáticamente;
- o solicitar revisión;

SIEMPRE elegir solicitar revisión.

La supervisión humana forma parte del proceso de desarrollo de este proyecto.
