# Catálogo de productos — NTT DATA

Aplicación Full Stack del desafío de especialización: permite explorar los 4.032 productos entregados, buscar por nombre, combinar categoría y formato, navegar páginas y consultar el detalle. El backend resuelve búsqueda, filtros y paginación; la interfaz conserva el diseño esencial del mockup.

Las Etapas 1 a 9 fueron completadas, revisadas, aprobadas y versionadas por el equipo humano. Las reglas de trabajo están en [AGENTS.md](AGENTS.md).

## Funcionalidades principales

- Catálogo de 4.032 productos.
- Búsqueda parcial por nombre.
- Filtros combinables por categoría y formato.
- Paginación de 8 productos.
- Detalle de producto.
- Estados Loading, Success, Empty y Error.

## Equipo de desarrollo

- Matias Bravo
- Sergio Elías Fernández
- Susana Farías Vera
- Felipe Farias
- Ricardo Vega Alarcón
- Liliana Cedeño
- Carlos Córdova

## Requisitos

- JDK 17, con `JAVA_HOME` apuntando al JDK y su carpeta `bin` en `PATH`.
- Maven Wrapper incluido: Maven 3.9.10; no requiere instalar Maven globalmente.
- Acceso a los repositorios Maven durante la primera descarga de herramientas y dependencias.
- Navegador con JavaScript, `fetch` y soporte de `<dialog>`.
- Node.js para las pruebas de frontend, sin paquetes npm; verificado con Node 24.6.0.
- El archivo entregado `data/catalog.csv`, legible desde el directorio de ejecución.

Stack fijado en [pom.xml](pom.xml): Spring Boot 4.0.8, Spring MVC, Validation, Apache Commons CSV 1.14.1 y Spring Boot Test/JUnit. Frontend HTML, CSS y JavaScript, sin framework ni proceso npm de compilación.

## Ejecución local

Ejecutar desde la raíz del repositorio. En PowerShell:

```powershell
java -version
./mvnw.cmd --version
./mvnw.cmd verify
./mvnw.cmd spring-boot:run
```

Abrir [http://localhost:8080](http://localhost:8080). Detener con `Ctrl+C`. Spring sirve frontend y API desde el mismo origen; no abrir `mockup/index.html` mediante `file://`.

Para ejecutar el paquete generado por `verify`:

```powershell
java -jar target/catalog-0.0.1-SNAPSHOT.jar
```

En Linux/macOS usar `sh ./mvnw verify` y `sh ./mvnw spring-boot:run`. Estos comandos se proporcionan como equivalentes; las verificaciones de esta entrega se ejecutaron en Windows.

La configuración predeterminada es `catalog.path=data/catalog.csv`, relativa al directorio de trabajo. El CSV **no está incluido en el JAR**: al entregar el paquete, acompañarlo del catálogo original o indicar su ruta explícita. Por ejemplo, en PowerShell:

```powershell
java -jar target/catalog-0.0.1-SNAPSHOT.jar '--catalog.path=C:/catalogo/data/catalog.csv' --server.port=8081
```

La ruta del ejemplo debe reemplazarse por una ruta existente. No se necesita base de datos. El catálogo se valida y carga al inicio; un cambio del archivo requiere reiniciar la aplicación.

## Arquitectura y decisiones técnicas

```text
Navegador: mockup/index.html + styles.css + app.js
    -> REST API / ProductController
    -> ProductService
    -> ProductRepository (colección inmutable e índice por ID)
    -> CsvProductLoader (carga única al iniciar)
    -> data/catalog.csv
```

`CatalogApplication` inicia Spring Boot y `CatalogProperties` configura la ruta. Los DTO separan las respuestas HTTP del modelo; `ApiExceptionHandler` centraliza errores de la API.

- Apache Commons CSV procesa UTF-8, campos entrecomillados y comas internas. Se validan las 12 columnas, campos obligatorios, IDs únicos, precios no negativos, CLP, fechas y categoría principal. Un catálogo inválido impide el arranque con diagnóstico.
- Los IDs son `String`, incluidas variantes como `12049.1`. Los nombres no son identificadores.
- Los precios usan `BigDecimal`. `price` y `originalPrice` ya están en CLP; no hay conversión monetaria.
- Se conserva la ruta completa de categoría y se deriva su grupo principal del texto anterior al primer `>`.
- Se mantiene el orden del CSV, con 8 productos por página. El almacenamiento en memoria es suficiente para este catálogo; no hay base de datos ni recarga por petición.
- Las opciones de filtros se obtienen del catálogo completo, sin valores escritos manualmente: 26 categorías principales y 252 formatos, en orden de primera aparición.
- El frontend usa `fetch`, reinicia la página al cambiar los controles y gestiona Loading, Success, Empty y Error, además de Reintentar y Limpiar filtros. Inserta textos mediante APIs del DOM y muestra un fallback si falla una imagen externa.

## API

Base local: `http://localhost:8080`. Respuestas exitosas en JSON.

| Método y ruta | Comportamiento |
| --- | --- |
| `GET /api/products` | Lista, busca, filtra y pagina |
| `GET /api/products/filters` | Devuelve `categories` y `formats`, ambos arrays de strings |
| `GET /api/products/{id}` | Devuelve un producto por ID exacto o 404 |

Parámetros de listado:

| Parámetro | Regla |
| --- | --- |
| `search` | Opcional; coincidencia parcial en `name`, sin distinguir mayúsculas/minúsculas |
| `category` | Opcional; coincidencia exacta con categoría principal |
| `format` | Opcional; coincidencia exacta con el formato original |
| `page` | Entero desde 0; valor predeterminado 0; tamaño fijo 8 |

Los filtros se combinan con AND. Los valores ausentes o vacíos omiten el filtro; los valores no vacíos no se recortan ni normalizan. No hay parámetro configurable de tamaño ni ordenamiento. Una categoría o formato desconocido devuelve cero coincidencias.

El listado contiene `items`, `page`, `size`, `totalElements`, `totalPages`, `hasPrevious` y `hasNext`. Sin filtros hay 504 páginas, índices 0 a 503. Una página no negativa posterior al límite devuelve 200 con `items: []`, conserva la página solicitada y los totales; `hasPrevious` indica `page > 0` y `hasNext` es falso. Con cero coincidencias, `totalPages` es 0.

Cada producto del listado y del detalle expone los 12 campos del CSV: `id`, `name`, `description`, `format`, `category`, `price`, `priceUnit`, `originalPrice`, `currency`, `imageUrl`, `productUrl` y `extractedAt`. Los precios son números JSON; el ID es texto; la fecha se serializa como fecha/hora ISO sin zona. `categoryGroup` es un valor interno y no forma parte del DTO.

Ejemplos reproducibles en PowerShell:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/products?page=0'
Invoke-RestMethod 'http://localhost:8080/api/products/filters'
Invoke-RestMethod 'http://localhost:8080/api/products/12049.1'
$category = [uri]::EscapeDataString('Huevos, leche y mantequilla')
Invoke-RestMethod "http://localhost:8080/api/products?search=leche&category=$category&page=0"
```

Para añadir `format`, usar un valor exacto de `/filters` y codificarlo con `EscapeDataString`.

| Estado | Caso |
| --- | --- |
| 200 | Consulta válida, incluso sin resultados |
| 400 | `page` negativo, no entero o fuera del rango de un entero Java |
| 404 | ID inexistente |
| 405 | Método HTTP no permitido |
| 500 | Error inesperado, registrado en servidor |

Los errores usan `ProblemDetail` (`application/problem+json`), con campos como `type`, `title`, `status`, `detail` e `instance`. El error 500 ofrece un mensaje genérico sin exponer la excepción interna.

## Pruebas y evidencias

Desde la raíz, ejecutar `./mvnw.cmd verify`: compila, ejecuta pruebas Java y genera el JAR. Con la aplicación encendida, ejecutar en una segunda terminal:

```powershell
$env:CATALOG_TEST_URL = 'http://localhost:8080'
node --test src/test/frontend/catalog.test.cjs
node --check mockup/app.js
node --check src/test/frontend/catalog.test.cjs
git diff --check
```

Las pruebas Java cubren loader, repositorio, servicio, integración HTTP, errores y dataset real, incluido el recorrido de las 504 páginas con 4.032 IDs. Las pruebas de frontend ejecutan el script real con un DOM mínimo y HTTP real; simulan demoras, errores, respuestas antiguas e imágenes fallidas. No miden layout ni sustituyen la revisión en navegador.

En un entorno restringido, si Node informa `spawn EPERM`, usar `node --test --test-isolation=none src/test/frontend/catalog.test.cjs`. Si la caché Maven global no es escribible, puede elegirse una caché local con `./mvnw.cmd '-Dmaven.repo.local=target/review-repository' verify`. Añadir `-o` solo si esa caché ya contiene todas las dependencias; `clean` elimina esa caché dentro de `target`.

- [Reporte de Etapa 8](reports/etapa-8.md): evidencia histórica del testing integral. Su aprobación humana posterior está registrada en AGENTS.md.
- [Reporte de Etapa 9](reports/etapa-9.md): comandos, resultados finales, criterios de aceptación y estado Git de la entrega.
- Evidencias locales regenerables: `target/surefire-reports/`, `target/stage9-verify.log` y `target/stage9-frontend.log`. `target/` está excluido de Git.

## Limitaciones y resolución de problemas

- Las imágenes dependen de servidores externos e Internet. El fallback evita que una imagen inaccesible rompa la interfaz.
- No hay evidencia visual nueva de escritorio/móvil en esta etapa. El reporte histórico documenta que el navegador no estaba disponible; la aprobación de Etapa 8 fue comunicada por el usuario. No se atribuyen al agente comprobaciones visuales no realizadas.
- La versión ofrece consulta de un catálogo estático: no incorpora edición, autenticación, persistencia en base de datos ni despliegue productivo.
- Si falla el inicio por el CSV, comprobar `catalog.path`, permisos y estructura del archivo. No eliminar registros inválidos para ocultar el problema.
- Si el puerto está ocupado, detener la instancia correspondiente o usar `--server.port` y actualizar la URL de las pruebas.
- Si Maven usa un Java diferente al de `java -version`, revisar `JAVA_HOME` y `./mvnw.cmd --version`. Las verificaciones finales usan JDK 17 de `JAVA_HOME`.
- [data/README.md](data/README.md) documenta procedencia y preparación original del dataset. Su referencia a `scripts/build_course_dataset.py` corresponde al material entregado: ese script no está en este repositorio. La ejecución de la aplicación utiliza directamente el CSV incluido y no requiere regenerarlo.

## Uso del agente y preparación de entrega

El trabajo asistido sigue las etapas y límites de AGENTS.md: analizar, implementar el alcance autorizado, probar, reportar y detenerse para revisión humana. El equipo humano aprueba y versiona cada etapa. En Etapa 9 el agente documentó el código existente y ejecutó verificaciones, sin cambiar lógica productiva ni datos. Las Etapas 1 a 9 están finalizadas; su revisión, aprobación y versionado fueron realizados por el equipo humano.

Para revisar la entrega, consultar este README y el reporte de Etapa 9, reproducir las pruebas y revisar el diff. Para distribuir el ejecutable, generar el JAR con `verify` y acompañarlo del CSV original y estas instrucciones. El código fuente, Maven Wrapper y pruebas permanecen en el repositorio. La revisión, aprobación y el versionado final de las Etapas 1 a 9 fueron completados por el equipo humano. Este cierre documental registra ese estado sin ejecutar `git add`, `commit` ni `push`.
