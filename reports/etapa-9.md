## REPORTE DE ETAPA

### 1. Etapa ejecutada

Etapa 9 — Documentación y entrega final. Ejecutada el 29 de septiembre de 2026. Actualización posterior del mismo día: progreso y ajuste exclusivamente visual de paleta autorizado por el usuario. La Etapa 9 sigue implementada y pendiente de aprobación humana.

### 2. Objetivo

Documentar la aplicación existente y preparar su entrega reproducible; registrar la aprobación y versionado de Etapas 1 a 8 comunicados por el usuario, verificar el resultado y detenerse para revisión humana.

### 3. Archivos creados

- `README.md`.
- `reports/etapa-9.md`.

Evidencias y artefactos locales excluidos de Git: `target/stage9-verify.log`, `target/stage9-frontend.log`, reportes regenerados en `target/surefire-reports/` y JAR `target/catalog-0.0.1-SNAPSHOT.jar`. El intento fallido de lanzamiento en segundo plano también dejó archivos auxiliares `target/stage9-server*`, sin valor como evidencia de arranque; el arranque exitoso se verificó en la sesión de terminal y por HTTP.

### 4. Archivos modificados

- `AGENTS.md`: Etapas 1 a 8 aprobadas y versionadas; Etapa 9 implementada, pendiente de aprobación humana; alcance adicional de paleta registrado.
- `mockup/styles.css`: exclusivamente colores de la paleta solicitada.

### 5. Implementación realizada

README con requisitos y versiones del proyecto, ejecución con Wrapper y JAR, ruta externa del CSV, arquitectura, decisiones técnicas, contrato API, ejemplos PowerShell, pruebas, evidencias, limitaciones, resolución de problemas, uso del agente y preparación de entrega. Incluye Equipo de desarrollo con Matias Bravo, Sergio Elías Fernández, Susana Farías Vera, Felipe Farias, Ricardo Vega Alarcón, Liliana Cedeño y Carlos Córdova.

Se conserva el reporte histórico de Etapa 8 y se distingue su limitación visual de la aprobación humana posterior. En la ejecución documental original no se modificaron datos, mockup, código productivo, pruebas ni dependencias. En el ajuste posterior autorizado se modificó únicamente la paleta de `mockup/styles.css`: crema para fondo y título, blanco para tarjetas/buscador/modal, petróleo para cabecera/texto/página activa, coral para Ver detalle y botones principales, mango como acento del logotipo, menta para imágenes/etiquetas y verde grisáceo para texto secundario. HTML, JavaScript, API, estructura, dimensiones, responsive e interacciones se conservan. El enlace secundario del modal usa petróleo sobre menta por contraste.

### 6. Pruebas ejecutadas

Desde la raíz, PowerShell, Windows 11, Maven 3.9.10, JDK 17.0.15 de `JAVA_HOME` y Node 24.6.0:

```powershell
java -version
node --version
./mvnw.cmd --version
./mvnw.cmd -o '-Dmaven.repo.local=target/review-repository' verify
& "$env:JAVA_HOME/bin/java.exe" -jar target/catalog-0.0.1-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=18081
$env:CATALOG_TEST_URL = 'http://127.0.0.1:18081'
node --test --test-isolation=none src/test/frontend/catalog.test.cjs
node --check mockup/app.js
node --check src/test/frontend/catalog.test.cjs
Invoke-RestMethod 'http://127.0.0.1:18081/api/products?page=0'
Invoke-RestMethod 'http://127.0.0.1:18081/api/products/filters'
Invoke-RestMethod 'http://127.0.0.1:18081/api/products/12049.1'
foreach ($resource in @('/','/app.js','/styles.css')) {
    Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:18081$resource"
}
git diff --check
git diff --exit-code -- data mockup src pom.xml .mvn mvnw mvnw.cmd
git status --short --untracked-files=all
```

Se usó la caché local existente en modo offline y el modo Node sin aislamiento por las restricciones documentadas en Etapa 8. Los logs de Maven y frontend se guardaron con redirección en `target/`. Se revisaron los enlaces relativos Markdown con `Test-Path`, la codificación UTF-8, los siete nombres y el diff. El proceso Java de esta verificación se detuvo al finalizar.

Verificación adicional del ajuste de paleta (29 de septiembre de 2026):

```powershell
./mvnw.cmd -o '-Dmaven.repo.local=target/review-repository' verify
& "$env:JAVA_HOME/bin/java.exe" -jar target/catalog-0.0.1-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=18081
$env:CATALOG_TEST_URL = 'http://127.0.0.1:18081'
node --test --test-isolation=none src/test/frontend/catalog.test.cjs
node --check mockup/app.js
Invoke-WebRequest -UseBasicParsing 'http://127.0.0.1:18081/styles.css'
git diff --check
git diff --exit-code -- data mockup/index.html mockup/app.js src pom.xml .mvn mvnw mvnw.cmd
git status --short --untracked-files=all
```

Logs adicionales: `target/palette-verify.log`, `target/palette-frontend.log` y `target/palette-package.log`. Tras las pruebas se ejecutó `./mvnw.cmd -o '-Dmaven.repo.local=target/review-repository' -DskipTests package` para incorporar el último ajuste de contraste al JAR (BUILD SUCCESS). Comparación exacta del CSS servido con el archivo local y cálculo de contraste mediante Node (luminancia sRGB relativa). No se agregaron dependencias ni pruebas permanentes para el cambio de colores.
### 7. Resultado de pruebas

| Verificación | Resultado |
| --- | --- |
| Maven verify: compilación, pruebas y empaquetado | PASS: BUILD SUCCESS, salida 0 |
| Java: 89 pruebas | PASS: 0 fallos, 0 errores, 0 omitidas |
| Frontend: 16 pruebas con DOM mínimo y HTTP real | PASS: 0 fallos, 0 omitidas, salida 0 |
| Dataset por HTTP | PASS: 4.032 productos, 504 páginas, 26 categorías, 252 formatos |
| Detalle con ID `12049.1` | PASS: conserva ID exacto |
| JAR y recursos `/`, `/app.js`, `/styles.css` | PASS: arranque con Java 17 y respuestas 200 |
| Sintaxis JavaScript, enlaces locales y revisión del diff | PASS |
| Preservación de material y código fuera de alcance | PASS: diff vacío en rutas verificadas |

Total: 105 pruebas automatizadas aprobadas por sus aserciones. Esto no constituye aprobación humana de la etapa.

Incidencias de herramientas: FAIL en un primer intento de edición por codificación de una tubería PowerShell/Python; no escribió cambios, se resolvió con `apply_patch`. FAIL al usar `Start-Process` por claves de entorno duplicadas `Path`/`PATH`; se resolvió ejecutando Java directamente en una sesión de terminal. Ninguna de estas incidencias fue un fallo de la aplicación o de sus aserciones.

Ajuste de paleta: PASS en Maven verify (89 pruebas), regresión frontend (16 pruebas), sintaxis JavaScript, CSS servido idéntico al archivo local y preservación de rutas fuera de alcance. Contraste de texto: petróleo/crema 11,12:1; blanco/petróleo 12,12:1; secundario/crema 5,71:1; secundario/menta 5,09:1; coral/blanco 4,77:1. El foco existente se conserva y el foco del enlace de cabecera usa blanco. Estos cálculos no equivalen a una auditoría completa de accesibilidad.

### 8. Criterios de aceptación

| Criterio | Estado |
| --- | --- |
| Registrar aprobación y versionado humano de Etapas 1 a 8 | PASS |
| README y equipo con los siete integrantes solicitados | PASS |
| Requisitos, arquitectura e instrucciones de ejecución | PASS |
| API documentada conforme a implementación y pruebas | PASS |
| Decisiones técnicas y tratamiento del dataset documentados | PASS |
| Pruebas, evidencias y limitaciones explícitas | PASS |
| Uso del agente y preparación de entrega documentados | PASS |
| Verificaciones finales de compilación, empaquetado, API y frontend | PASS |
| Alcance documental original y posterior ajuste de colores explícitamente autorizado | PASS |
| Paleta solicitada y contraste de texto comprobado | PASS |
| Estructura, funcionalidad, API y comportamiento sin cambios | PASS |
| Inspección visual en navegador de escritorio y móvil del ajuste | PENDIENTE |
| Reporte obligatorio y estado Git informados | PASS |
| Revisión y aprobación humana de Etapa 9 | PENDIENTE |

### 9. Problemas encontrados

`java` en PATH corresponde a Java 22.0.2; Maven y el JAR verificado usaron explícitamente JDK 17.0.15 de `JAVA_HOME`. El README explica cómo comprobar y alinear ambas configuraciones.

No se generaron evidencias visuales nuevas: el arnés del frontend no verifica layout, foco nativo ni descarga real de imágenes. La limitación histórica se conserva sin reabrir ni atribuir al agente la aprobación humana de Etapa 8. Las imágenes externas dependen de terceros. El script de regeneración mencionado en el README original del dataset no está incluido; se documenta esta limitación sin modificar el material original.

Los comandos Linux/macOS no se ejecutaron. La primera descarga de dependencias desde una caché vacía no se verificó; se utilizó la caché local disponible. Git advierte conversión futura LF a CRLF, sin errores de whitespace.

En el ajuste posterior, el inventario de automatización devolvió cero navegadores y aplicaciones conectados: no se obtuvieron capturas ni se verificó visualmente escritorio/móvil. Un primer arranque del JAR coincidió con su reempaquetado Maven y falló con ClassNotFoundException; al finalizar verify se repitió el arranque correctamente, sin cambios de código. La primera comparación HTTP detectó CSS empaquetado anterior al último ajuste de contraste; se regeneró el JAR, se reinició y la comparación exacta pasó. El servidor temporal se detuvo después de las verificaciones.

### 10. Decisiones pendientes

Revisión y aprobación humana de la Etapa 9, del contenido de entrega y del ajuste visual de paleta. No se introducen decisiones arquitectónicas nuevas. El versionado posterior corresponde al equipo humano; no se ejecutaron git add, commit ni push.

### 11. Git status

Estado final, sin cambios en staging:

```text
 M AGENTS.md
 M mockup/styles.css
?? README.md
?? reports/etapa-9.md
```

### 12. Próxima etapa sugerida

No hay otra etapa en el roadmap. Corresponde revisión humana de la Etapa 9 y, si el equipo la aprueba, su versionado y entrega. Estas acciones no se ejecutan.

### 13. Solicitud de revisión

Revisar README, actualización de AGENTS.md, paleta de mockup/styles.css y resultados de este reporte. La pausa se exige en [AGENTS.md, sección 19](../AGENTS.md): «Cuando el agente termine una etapa debe DETENERSE». Se detiene el trabajo sin aprobación automática.

ETAPA FINALIZADA. PENDIENTE DE REVISIÓN Y APROBACIÓN HUMANA.
