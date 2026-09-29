# Dataset del curso

## Fuente

El catálogo se genera a partir del **Dataset de precios y características de productos disponibles en la tienda online de Mercadona**, publicado en Zenodo por Jonathan Martínez Herreros y Juan Antonio Martínez Corral.

- Fuente: `zenodo.org/records/17565792`
- DOI: `10.5281/zenodo.17565792`
- Archivo original: `dataset.csv`
- Registros: 4.032
- Idioma: español
- Moneda original: euro (`EUR`)
- Fecha de extracción: noviembre de 2025

La fuente contiene nombre, descripción, formato, categoría, precios, imagen principal, URL del producto y fecha de extracción.

Para nacionalizar el ejercicio, los precios fueron convertidos de EUR a pesos chilenos usando el tipo de cambio nominal publicado por el Banco Central de Chile para el **2 de septiembre de 2026: 1 EUR = 1.085,59 CLP**. La conversión se realiza al generar el CSV y se redondea al peso entero más cercano; la aplicación no calcula ni consulta el tipo de cambio.

## Catálogo entregado

`catalog.csv` conserva los **4.032 productos completos**. El archivo continúa siendo liviano y permite practicar búsqueda, filtros y paginación con un volumen significativo.

Columnas:

```text
id,name,description,format,category,price,priceUnit,originalPrice,currency,imageUrl,productUrl,extractedAt
```

- `id`: identificador del producto en la fuente; conserva los sufijos de variante presentes en la URL original.
- `name`: nombre del producto en español.
- `description`: descripción original en español.
- `format`: presentación del producto, por ejemplo botella, paquete o caja.
- `category`: ruta de categoría original.
- `price`: precio actual convertido a pesos chilenos.
- `priceUnit`: unidad de precio indicada por la fuente.
- `originalPrice`: precio original convertido a pesos chilenos.
- `currency`: peso chileno (`CLP`).
- `imageUrl`: imagen principal.
- `productUrl`: ficha original del producto.
- `extractedAt`: fecha y hora de extracción.

No se inventan ni traducen valores. Los únicos cambios son la conversión monetaria documentada y la normalización de espacios internos de la descripción para que cada registro permanezca en una línea del CSV.

## Imágenes

El CSV conserva la URL de la imagen principal de cada producto. El mockup incluye localmente las fotografías de sus ocho productos de ejemplo para que pueda abrirse sin conexión.

## Reproducibilidad

1. Descarga `dataset.csv` desde Zenodo.
2. Guárdalo como `data/source/mercadona-products.csv`.
3. Ejecuta:

```bash
python scripts/build_course_dataset.py
```

4. El script genera `data/catalog.csv`.

`data/source/` está excluido de Git porque sólo se utiliza durante la generación.
