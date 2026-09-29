## REPORTE DE ETAPA

### 1. Etapa ejecutada

Etapa 8 — Testing integral. Ejecución del 29 de septiembre de 2026.
Pruebas automatizadas completadas; verificación visual pendiente por falta de navegador.
No se declara cumplida la Definition of Done de la etapa.

### 2. Objetivo

Verificar las Etapas 1 a 7 aprobadas: pruebas unitarias, integración, API, dataset,
frontend, regresión y casos límite. Actualizar AGENTS.md con la aprobación humana
de la Etapa 7. No ejecutar la Etapa 9.

### 3. Archivos creados

- `reports/etapa-8.md`: este reporte de revisión.

Evidencias generadas, excluidas de Git por estar en `target/`:
`stage8-verify.log`, `stage8-frontend.log`, reportes XML/TXT en `surefire-reports/`
y artefactos de compilación/paquete Maven.

### 4. Archivos modificados

- `AGENTS.md`.
- `src/test/java/com/nttdata/catalog/controller/ProductApiTests.java`.
- `src/test/frontend/catalog.test.cjs`.

### 5. Implementación realizada

Se agregaron dos pruebas HTTP: recorrido de las 504 páginas con los 4.032 IDs en
orden, sin huecos ni duplicados, y recorrido de los resultados de filtros combinados
con comprobación de la página posterior al límite.

Se agregaron cinco pruebas de frontend: reinicio de página por cada control y limpieza
desde la barra; navegación numerada y anterior conservando búsqueda; recuperación de
filtros malformados; descarte de fallos de peticiones antiguas; recuperación del fallback
de imagen en detalle y presentación segura de texto/enlaces.

Se ejecutaron todas las suites existentes. El frontend se probó contra el JAR en
`127.0.0.1:18080`; los fallos, demoras y respuestas desordenadas se simularon mediante
el arnés existente de DOM mínimo. No se incorporaron dependencias ni cambios de
arquitectura o código productivo.

### 6. Pruebas ejecutadas

Desde la raíz, PowerShell, Maven Wrapper 3.9.10, JDK 17.0.15 de `JAVA_HOME`, Node 24.6.0:

```powershell
.\mvnw.cmd verify
.\mvnw.cmd -o '-Dmaven.repo.local=target/review-repository' verify
& "$env:JAVA_HOME/bin/java.exe" -jar target/catalog-0.0.1-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=18080
$env:CATALOG_TEST_URL = 'http://127.0.0.1:18080'
node --test src/test/frontend/catalog.test.cjs
node --test --test-isolation=none src/test/frontend/catalog.test.cjs
node --check mockup/app.js
node --check src/test/frontend/catalog.test.cjs
git diff --check
git diff --exit-code -- data mockup src/main pom.xml .mvn mvnw mvnw.cmd
git status --short --untracked-files=all
```

La caché `target/review-repository` ya existía. No se ejecutó `clean`, que eliminaría
esa caché. Se repitió `verify` sin redirección para confirmar `MAVEN_EXIT_CODE=0`
tras una discrepancia entre el estado de PowerShell y el primer log de éxito.

Intentos de verificación visual mediante la herramienta de navegador:

```javascript
await cua.getState();
await cua.createBrowserTab('iab', 'http://127.0.0.1:18080', { visible: true });
```

El inventario devolvió `apps: [], browsers: []`; la apertura falló con
`Browser is not available: iab`. No se obtuvieron capturas de escritorio o móvil.

### 7. Resultado de pruebas

| Suite | Pruebas | Resultado |
| --- | ---: | --- |
| CatalogApplicationTests | 1 | PASS |
| CsvProductLoaderTests | 40 | PASS |
| ProductRepositoryTests | 3 | PASS |
| ProductRepositoryStartupTests | 2 | PASS |
| ProductServiceTests | 15 | PASS |
| ProductServiceCatalogTests | 4 | PASS |
| ProductApiTests | 22 | PASS |
| ProductApiErrorTests | 2 | PASS |
| Frontend, DOM mínimo y HTTP | 16 | PASS |

Total: **105 pruebas, 0 fallos, 0 errores, 0 omitidas** en las ejecuciones exitosas.
Maven: `BUILD SUCCESS`, salida 0; Node sin aislamiento: salida 0.
Arranque del JAR, recursos estáticos y controles de sintaxis/diff: PASS.

Intentos iniciales: FAIL de entorno, no de asercciones. Maven no pudo escribir en
la caché global (`AccessDeniedException`); Node con aislamiento no pudo crear el
subproceso (`spawn EPERM`). Se resolvieron usando la caché local existente y el
modo de pruebas en un solo proceso. No se desactivó ninguna prueba.

### 8. Criterios de aceptación

| Criterio | Resultado | Evidencia |
| --- | --- | --- |
| CSV: entrecomillados, comas, Unicode, saltos de línea, IDs con sufijos | PASS | CsvProductLoaderTests |
| CSV: 12 campos requeridos, precios, categorías y moneda | PASS | CsvProductLoaderTests |
| CSV ausente, ilegible o estructuralmente inválido: diagnóstico y fallo de inicio | PASS | Loader y StartupTests |
| Carga única, índice por ID, inmutabilidad y orden estable | PASS | ProductRepositoryTests |
| Búsqueda parcial sin distinguir mayúsculas, Unicode | PASS | ServiceTests y ProductApiTests |
| Categoría principal, ruta original, formato exacto y combinación AND | PASS | ServiceTests y ProductApiTests |
| Paginación, cero resultados y páginas límite, incluido Integer.MAX_VALUE | PASS | ServiceTests, CatalogTests y ProductApiTests |
| API: listado, filtros reales y resolución de /filters | PASS | ProductApiTests |
| API: detalle exacto, IDs String, 404, parámetros inválidos 400 | PASS | ProductApiTests |
| API: error inesperado 500 y método no permitido 405 | PASS | ProductApiErrorTests |
| Dataset: 4.032 productos, 26 categorías, 252 formatos, 504 páginas de 8 | PASS | Loader, CatalogTests y recorrido HTTP completo |
| Frontend: Loading, Success, Empty, Error | PASS | catalog.test.cjs, DOM mínimo |
| Frontend: Reintentar y Limpiar filtros | PASS | catalog.test.cjs, fallos simulados y HTTP real |
| Frontend: navegación, detalle y reinicio de página al cambiar controles | PASS | catalog.test.cjs |
| Frontend: imágenes fallidas y recuperación del fallback | PASS | Eventos de error simulados en catalog.test.cjs |
| Regresión: respuestas antiguas, filtros malformados, texto y URLs seguros | PASS | catalog.test.cjs |
| Compilación, empaquetado y ejecución con Java 17 | PASS | Maven verify y arranque del JAR |
| Verificación visual en escritorio | PENDIENTE | Navegador no disponible |
| Verificación visual en móvil | PENDIENTE | Navegador no disponible |
| Preservación de dataset, mockup y código productivo | PASS | git diff sin cambios en rutas protegidas |
| Alcance exclusivo de Etapa 8 y aprobación humana de Etapa 7 registrada | PASS | AGENTS.md y diff |

### 9. Problemas encontrados

La ausencia de navegador impide verificar el diseño, desbordamientos, interacción
nativa del diálogo, foco y presentación de los estados en escritorio y móvil.
El arnés de DOM mínimo no mide layout ni descarga imágenes; sus pruebas de fallback
invocan los eventos de error de forma controlada. No equivale a una prueba visual.

No se detectaron fallos funcionales en las aserciones ejecutadas. Los mensajes de
error 500 y de inicio inválido en el log Java son estímulos esperados de sus pruebas.
Las restricciones de caché y subprocesos se resolvieron sin cambiar dependencias.

### 10. Decisiones pendientes

Completar la verificación visual con un navegador conectado o mediante revisión
manual humana, y después evaluar la aprobación de la Etapa 8. No hay decisiones
arquitectónicas nuevas. La aprobación previa de la Etapa 7 queda respetada.

### 11. Git status

Estado final comprobado antes de entregar, sin staging, commits ni push:

```text
 M AGENTS.md
 M src/test/frontend/catalog.test.cjs
 M src/test/java/com/nttdata/catalog/controller/ProductApiTests.java
?? reports/etapa-8.md
```

### 12. Próxima etapa sugerida

Etapa 9 — Documentación y entrega, únicamente después de completar los pendientes
y obtener aprobación humana de la Etapa 8. No ejecutada.

### 13. Solicitud de revisión

Revisar los cambios y resultados, y completar los dos criterios visuales pendientes.
Se detiene esta ejecución; la frase obligatoria de cierre no declara satisfechos
los criterios pendientes ni constituye aprobación automática. Esta revisión está
exigida por AGENTS.md, sección 19: «Cuando el agente termine una etapa debe DETENERSE».

ETAPA FINALIZADA. PENDIENTE DE REVISIÓN Y APROBACIÓN HUMANA.
