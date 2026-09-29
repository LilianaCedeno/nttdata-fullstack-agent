const products = [
  {
    id: '10043',
    name: 'Batido de chocolate 90% leche Puleva',
    description: 'Batido de chocolate 90% leche Puleva. Alérgenos: Contiene leche y sus derivados (incluida la lactosa).. Ingredientes: Leche parcialmente desnatada al 1,2% MG (90%), azúcar, cacao desgrasado (mínimo 0,9%), estabilizantes (E-407, E-460, E-466 y E-339ii), aroma y Vitamina D.. Instrucciones de almacenamiento: Una vez abierto el envase, debe conservarse en frío, siendo aconsejable su consumo en los 3 días siguientes.. Instrucciones de uso: Agitar antes de abrir.',
    format: '6 mini bricks x 200 ml',
    category: 'Huevos, leche y mantequilla > Leche y bebidas vegetales',
    price: 2605,
    priceUnit: '/pack',
    originalPrice: 3040,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/10043/batido-chocolate-90-leche-puleva-pack-6',
    extractedAt: '2025-11-06 20:29:59',
    image: 'assets/10043.jpg',
  },
  {
    id: '10286',
    name: 'Dulce de leche Chimbote',
    description: 'Dulce de leche Chimbote. Alérgenos: Contiene leche y sus derivados (incluida la lactosa).. Ingredientes: Leche entera fresca (61%), azúcar, leche descremada en polvo (8,4%), aroma natural de vainilla.. Instrucciones de almacenamiento: Una vez abierto conservar en el frigorifico. Origen: Sains Richaumont - Francia',
    format: 'Tarro',
    category: 'Azúcar, caramelos y chocolate > Chocolate',
    price: 2942,
    priceUnit: '/ud.',
    originalPrice: 2942,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/10286/dulce-leche-chimbote-tarro',
    extractedAt: '2025-11-06 20:31:36',
    image: 'assets/10286.jpg',
  },
  {
    id: '10449',
    name: 'Crema de leche para café Campina',
    description: 'Crema de leche para café Campina. Alérgenos: Contiene leche y sus derivados (incluida la lactosa).. Ingredientes: Nata (10% M.G.). Instrucciones de almacenamiento: Consérvese fuera de la nevera en un ambiente fresco, limpio y sin olor, protegido de la luz solar, (temperatura de almacenamiento recomendada entre +4C y +24C).. Origen: España',
    format: 'Paquete',
    category: 'Cacao, café e infusiones > Café soluble y otras bebidas',
    price: 1520,
    priceUnit: '/ud.',
    originalPrice: 1520,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/10449/crema-leche-cafe-campina-paquete',
    extractedAt: '2025-11-06 20:33:07',
    image: 'assets/10449.jpg',
  },
  {
    id: '10453',
    name: 'Mini burgers de vacuno y cerdo',
    description: 'Mini burgers de vacuno y cerdo. Alérgenos: Contiene dióxido de azufre y sulfitos.. Ingredientes: Carne de vacuno 46%, carne de cerdo 44%, agua, maíz, arroz, fibras vegetales, sal, aromas, especias, conservador (E-221) (sulfito), antioxidantes (E-301 y E-331) y colorante (E-120).. Instrucciones de almacenamiento: Conservar en refrigeración entre 0 y 4C. Una vez abierto consumir antes de 48 horas. Instrucciones de uso: Cocinar completamente antes de su consumo.',
    format: 'Bandeja',
    category: 'Carne > Hamburguesas y picadas',
    price: 3365,
    priceUnit: '/ud.',
    originalPrice: 3365,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/10453/mini-burgers-vacuno-cerdo-bandeja',
    extractedAt: '2025-11-06 20:33:13',
    image: 'assets/10453.jpg',
  },
  {
    id: '29048',
    name: 'Refresco lima limón Hacendado fresh gas',
    description: 'Refresco lima limón Hacendado fresh gas. Ingredientes: Agua carbonatada, azúcar, acidulantes: ácido cítrico, ácido málico, aromas naturales, corrector de acidez: citratos de sodio, edulcorante: glucósidos de esteviol.. Instrucciones de almacenamiento: Proteger de la luz solar y de olores agresivos. Conservar en lugar limpio, fresco y seco.',
    format: 'Lata',
    category: 'Agua y refrescos > Refresco de naranja y de limón',
    price: 402,
    priceUnit: '/ud.',
    originalPrice: 402,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/29048/refresco-lima-limon-hacendado-fresh-gas-lata',
    extractedAt: '2025-11-06 23:03:39',
    image: 'assets/29048.jpg',
  },
  {
    id: '11565',
    name: 'Pepinillos en vinagre Hacendado calibre pequeño',
    description: 'Pepinillos en vinagre Hacendado. Alérgenos: Libre de altramuces y productos a base de altramuces. Libre de mostaza y productos derivados. Libre de apio y productos derivados. Contiene dióxido de azufre y sulfitos.. Ingredientes: Pepinillo (sulfitos), vinagre de vino y sal.. Instrucciones de almacenamiento: Consumir dentro de los 15 días después de la apertura. Una vez abierto, conservar cerrado en el frigorífico con el máximo de líquido posible.. Instrucciones de uso: Producto listo para su consumo.',
    format: 'Tarro',
    category: 'Aperitivos > Aceitunas y encurtidos',
    price: 1574,
    priceUnit: '/ud.',
    originalPrice: 1628,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/11565/pepinillos-vinagre-hacendado-tarro',
    extractedAt: '2025-11-06 20:46:43',
    image: 'assets/11565.jpg',
  },
  {
    id: '11550',
    name: 'Mayonesa Hacendado',
    description: 'Mayonesa Hacendado. Alérgenos: Contiene huevos y productos a base de huevo.. Ingredientes: Aceite refinado de girasol, agua, yema de HUEVO pasterizada, jarabe de glucosa y fructosa, vinagre de uva, estabilizante (almidón modificado de maíz), sal, zumo de limón concentrado, aroma, colorantes (-caroteno y extracto de pimentón) y antioxidante (E-385).. Instrucciones de almacenamiento: Almacenar en lugar fresco y seco. Una vez abierto conservar refrigerado 1 mes.',
    format: 'Bote',
    category: 'Aceite, especias y salsas > Mayonesa, ketchup y mostaza',
    price: 1628,
    priceUnit: '/ud.',
    originalPrice: 1628,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/11550/mayonesa-hacendado-bote',
    extractedAt: '2025-11-06 20:45:54',
    image: 'assets/11550.jpg',
  },
  {
    id: '11564',
    name: 'Tinto de verano Casón Histórico',
    description: 'Tinto de verano Casón Histórico. Alérgenos: Contiene dióxido de azufre y sulfitos.. Ingredientes: Agua carbonatada, vino tinto (uvas, conservador: sulfitos), zumo de uva a partir de concentrado, corrector de acidez: ácido cítrico, edulcorantes: ciclamato sódico y sacarina sódica, aromas y colorante: E-163.. Instrucciones de almacenamiento: Conservar en sitio fresco y protegido de la luz solar.. Instrucciones de uso: Servir frío.',
    format: 'Botella',
    category: 'Bodega > Tinto de verano y sangría',
    price: 1086,
    priceUnit: '/ud.',
    originalPrice: 1086,
    currency: 'CLP',
    productUrl: 'https://tienda.mercadona.es/product/11564/tinto-verano-cason-historico-botella',
    extractedAt: '2025-11-06 20:46:36',
    image: 'assets/11564.jpg',
  },
];

const grid = document.querySelector('#product-grid');
const search = document.querySelector('#search');
const categoryFilter = document.querySelector('#category-filter');
const formatFilter = document.querySelector('#format-filter');
const resultCount = document.querySelector('#result-count');
const dialog = document.querySelector('#product-dialog');

function formatPrice(value, currency) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value);
}

function categoryGroup(category) {
  return category.split(' > ')[0];
}

function productCard(product) {
  return `
    <article class="product-card">
      <button class="card-button" type="button" data-product-id="${product.id}" aria-label="Ver detalle de ${product.name}">
        <span class="image-wrap">
          <img src="${product.image}" alt="" />
          <span class="card-index">#${product.id}</span>
        </span>
        <span class="card-copy">
          <span class="card-type">${categoryGroup(product.category)}</span>
          <strong>${product.name}</strong>
          <span class="card-meta">${product.format} <i>•</i> ${formatPrice(product.price, product.currency)}</span>
          <span class="view-link">Ver detalle <span aria-hidden="true">→</span></span>
        </span>
      </button>
    </article>`;
}

// Estado forzado desde la referencia de estados. Cuando vale null la grilla
// se comporta normalmente y "sin resultados" aparece solo si el filtro no
// encuentra nada, que es como ocurre de verdad.
let estadoForzado = null;

const PLANTILLAS = {
  cargando: `
    <div class="inline-state">
      <span class="state-icon spinner" aria-hidden="true"></span>
      <strong>Cargando productos</strong>
      <p>Espera mientras consultamos el catálogo.</p>
    </div>`,
  vacio: `
    <div class="inline-state empty">
      <span class="state-icon" aria-hidden="true">⌕</span>
      <strong>Sin resultados</strong>
      <p>Prueba con otra búsqueda o elimina algunos filtros.</p>
      <button type="button">Limpiar filtros</button>
    </div>`,
  error: `
    <div class="inline-state error">
      <span class="state-icon" aria-hidden="true">!</span>
      <strong>No pudimos cargar el catálogo</strong>
      <p>Inténtalo nuevamente en unos momentos.</p>
      <button type="button">Reintentar</button>
    </div>`,
};

function renderProducts() {
  if (estadoForzado) {
    grid.innerHTML = PLANTILLAS[estadoForzado];
    resultCount.textContent = 'Referencia de estado: ' + estadoForzado;
    return;
  }
  const query = search.value.trim().toLocaleLowerCase('es');
  const selectedCategory = categoryFilter.value;
  const selectedFormat = formatFilter.value;
  const filtered = products.filter((product) => {
    const matchesQuery = product.name.toLocaleLowerCase('es').includes(query);
    const matchesCategory = !selectedCategory || categoryGroup(product.category) === selectedCategory;
    const matchesFormat = !selectedFormat || product.format === selectedFormat;
    return matchesQuery && matchesCategory && matchesFormat;
  });

  grid.innerHTML = filtered.length
    ? filtered.map(productCard).join('')
    : PLANTILLAS.vacio;

  const usingSampleFilter = query || selectedCategory || selectedFormat;
  resultCount.textContent = usingSampleFilter
    ? `Mostrando ${filtered.length} de 8 productos de ejemplo`
    : 'Mostrando 1-8 de 4.032';
}

function openDetail(product) {
  document.querySelector('#detail-id').textContent = `PRODUCTO #${product.id}`;
  document.querySelector('#detail-title').textContent = product.name;
  document.querySelector('#detail-description').textContent = product.description;
  const image = document.querySelector('#detail-image');
  image.src = product.image;
  image.alt = product.name;

  const values = [
    ['Categoría', product.category],
    ['Formato', product.format],
    ['Unidad de precio', product.priceUnit],
    ['Identificador', product.id],
  ];
  document.querySelector('#detail-list').innerHTML = values
    .map(([label, value]) => `<div><dt>${label}</dt><dd>${value}</dd></div>`)
    .join('');

  document.querySelector('#detail-price').textContent = formatPrice(product.price, product.currency);
  document.querySelector('#detail-currency').textContent = `Moneda: ${product.currency}`;
  document.querySelector('#detail-original-price').textContent = `Precio original: ${formatPrice(product.originalPrice, product.currency)}`;
  document.querySelector('#detail-extracted-at').textContent = `Datos extraídos: ${product.extractedAt}`;
  document.querySelector('#detail-source').href = product.productUrl;
  dialog.showModal();
}

grid.addEventListener('click', (event) => {
  const button = event.target.closest('[data-product-id]');
  if (!button) return;
  const product = products.find((item) => item.id === button.dataset.productId);
  if (product) openDetail(product);
});

document.querySelector('#dialog-close').addEventListener('click', () => dialog.close());
dialog.addEventListener('click', (event) => { if (event.target === dialog) dialog.close(); });
[search, categoryFilter, formatFilter].forEach((control) => control.addEventListener('input', renderProducts));
renderProducts();


// Referencia de estados: fuerza la grilla a cada caso sin salir de la pagina.
document.querySelectorAll('.state-switch-buttons button').forEach((boton) => {
  boton.addEventListener('click', () => {
    document.querySelectorAll('.state-switch-buttons button').forEach((b) => b.classList.remove('on'));
    boton.classList.add('on');
    estadoForzado = boton.dataset.state === 'normal' ? null : boton.dataset.state;
    renderProducts();
  });
});
