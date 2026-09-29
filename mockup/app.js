const grid = document.querySelector('#product-grid');
const search = document.querySelector('#search');
const categoryFilter = document.querySelector('#category-filter');
const formatFilter = document.querySelector('#format-filter');
const resultCount = document.querySelector('#result-count');
const dialog = document.querySelector('#product-dialog');
const previous = document.querySelector('#previous-page');
const next = document.querySelector('#next-page');
const pages = document.querySelector('.pages');
let currentPage = null;
let listRequest = 0;
let detailRequest = 0;
let requestedPage = 0;
let filtersReady = false;
const pagination = document.querySelector('#pagination');
const clearFilters = document.querySelector('#clear-filters');
const filterStatus = document.querySelector('#filter-status');
const detailStatus = document.querySelector('#detail-status');
const detailContent = document.querySelector('#detail-content');

function statePanel(state, title, message, action, callback) {
  const panel = element('div', `inline-state ${state}`);
  panel.setAttribute('role', state === 'error' ? 'alert' : 'status');
  const icon = element('span', state === 'loading' ? 'state-icon spinner' : 'state-icon', state === 'error' ? '!' : '⌕');
  icon.setAttribute('aria-hidden', 'true');
  panel.append(icon, element('strong', '', title), element('p', '', message));
  if (action) {
    const button = element('button', '', action);
    button.type = 'button';
    button.addEventListener('click', callback);
    panel.append(button);
  }
  return panel;
}

function hasFilters() {
  return Boolean(search.value || categoryFilter.value || formatFilter.value);
}

function resetFilters() {
  search.value = categoryFilter.value = formatFilter.value = '';
  loadProducts(0);
}

function setListState(state) {
  grid.dataset.state = state;
  grid.setAttribute('aria-busy', String(state === 'loading'));
  pagination.hidden = state !== 'success';
  if (state !== 'success') {
    previous.disabled = next.disabled = true;
    pages.replaceChildren();
  }
}

function element(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function formatPrice(value, currency) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency', currency, maximumFractionDigits: 0,
  }).format(value);
}

function safeUrl(value) {
  try {
    const url = new URL(value);
    return ['https:', 'http:'].includes(url.protocol) ? url.href : '';
  } catch { return ''; }
}

function setImage(image, product) {
  image.hidden = false;
  image.parentElement.querySelector('.image-fallback')?.remove();
  image.alt = product.name;
  const fallback = () => {
    image.hidden = true;
    if (!image.parentElement.querySelector('.image-fallback')) {
      image.parentElement.append(element('span', 'image-fallback', 'Imagen no disponible'));
    }
  };
  image.onerror = fallback;
  const url = safeUrl(product.imageUrl);
  if (url) image.src = url;
  else { image.removeAttribute('src'); fallback(); }
}

function productCard(product) {
  const card = element('article', 'product-card');
  const button = element('button', 'card-button');
  button.type = 'button';
  button.dataset.productId = product.id;
  button.setAttribute('aria-label', `Ver detalle de ${product.name}`);
  const wrap = element('span', 'image-wrap');
  const image = element('img');
  wrap.append(image, element('span', 'card-index', `#${product.id}`));
  setImage(image, product);
  const copy = element('span', 'card-copy');
  copy.append(
    element('span', 'card-type', product.category.split('>')[0].trim()),
    element('strong', '', product.name),
    element('span', 'card-meta', `${product.format} • ${formatPrice(product.price, product.currency)}`),
    element('span', 'view-link', 'Ver detalle →'),
  );
  button.append(wrap, copy);
  card.append(button);
  return card;
}

async function getJson(url) {
  const response = await fetch(url, { headers: { Accept: 'application/json' } });
  if (!response.ok) throw new Error(`HTTP ${response.status}: ${url}`);
  return response.json();
}

function renderPage(data) {
  grid.replaceChildren(...data.items.map(productCard));
  const first = data.items.length ? data.page * data.size + 1 : 0;
  const last = data.items.length ? first + data.items.length - 1 : 0;
  resultCount.textContent = `Mostrando ${first}-${last} de ${data.totalElements.toLocaleString('es-CL')}`;
  previous.disabled = !data.hasPrevious;
  next.disabled = !data.hasNext;
  pages.replaceChildren();
  if (!data.totalPages) return;
  // Only navigation metadata is processed here; products stay in backend order.
  const numbers = [...new Set([0, data.page - 1, data.page, data.page + 1, data.totalPages - 1])]
    .filter(page => page >= 0 && page < data.totalPages).sort((a, b) => a - b);
  numbers.forEach((page, index) => {
    if (index && page - numbers[index - 1] > 1) pages.append(element('span', '', '…'));
    const button = element('button', page === data.page ? 'active' : '', page + 1);
    button.type = 'button';
    button.setAttribute('aria-label', `Ir a página ${page + 1}`);
    if (page === data.page) button.setAttribute('aria-current', 'page');
    button.addEventListener('click', () => loadProducts(page));
    pages.append(button);
  });
}

async function loadProducts(page = 0) {
  const request = ++listRequest;
  requestedPage = page;
  currentPage = null;
  clearFilters.disabled = !hasFilters();
  setListState('loading');
  const params = new URLSearchParams({ page: String(page) });
  for (const [key, value] of [['search', search.value], ['category', categoryFilter.value], ['format', formatFilter.value]]) {
    if (value) params.set(key, value);
  }
  previous.disabled = next.disabled = true;
  pages.replaceChildren();
  grid.replaceChildren(statePanel('loading', 'Cargando productos', 'Espera mientras consultamos el catálogo.'));
  resultCount.textContent = '';
  try {
    const data = await getJson(`/api/products?${params}`);
    if (request !== listRequest) return;
    renderPage(data);
    currentPage = data;
    setListState(data.items.length ? 'success' : 'empty');
    if (!data.items.length) {
      grid.replaceChildren(statePanel('empty', 'Sin resultados', 'Prueba con otra búsqueda o elimina algunos filtros.',
        hasFilters() ? 'Limpiar filtros' : null, resetFilters));
    }
  } catch (error) {
    if (request !== listRequest) return;
    currentPage = null;
    setListState('error');
    resultCount.textContent = '';
    grid.replaceChildren(statePanel('error', 'No pudimos cargar el catálogo', 'Inténtalo nuevamente en unos momentos.',
      'Reintentar', () => loadProducts(requestedPage)));
    console.error(error);
  }
}

async function loadFilters() {
  if (filtersReady) return;
  categoryFilter.disabled = formatFilter.disabled = true;
  filterStatus.hidden = false;
  filterStatus.replaceChildren(statePanel('loading', 'Cargando filtros', 'Espera mientras consultamos las opciones.'));
  try {
    const data = await getJson('/api/products/filters');
    if (!Array.isArray(data.categories) || !Array.isArray(data.formats) ||
        ![...data.categories, ...data.formats].every(value => typeof value === 'string')) {
      throw new Error('Respuesta de filtros inválida');
    }
    for (const [select, values] of [[categoryFilter, data.categories], [formatFilter, data.formats]]) {
      for (const value of values) {
        const option = element('option', '', value);
        option.value = value;
        select.append(option);
      }
    }
    filtersReady = true;
    categoryFilter.disabled = formatFilter.disabled = false;
    filterStatus.replaceChildren();
    filterStatus.hidden = true;
  } catch (error) {
    filterStatus.replaceChildren(statePanel('error', 'No pudimos cargar los filtros', 'Puedes seguir buscando por nombre.',
      'Reintentar', loadFilters));
    console.error(error);
  }
}

async function openDetail(id) {
  const request = ++detailRequest;
  detailContent.hidden = true;
  detailStatus.hidden = false;
  dialog.setAttribute('aria-label', 'Detalle del producto');
  dialog.removeAttribute('aria-labelledby');
  detailStatus.replaceChildren(statePanel('loading', 'Cargando detalle', 'Espera mientras consultamos el producto.'));
  if (!dialog.open) dialog.showModal();
  try {
    const product = await getJson(`/api/products/${encodeURIComponent(id)}`);
    if (request !== detailRequest) return;
    document.querySelector('#detail-id').textContent = `PRODUCTO #${product.id}`;
    document.querySelector('#detail-title').textContent = product.name;
    document.querySelector('#detail-description').textContent = product.description;
    setImage(document.querySelector('#detail-image'), product);
    const values = [['Categoría', product.category], ['Formato', product.format],
      ['Unidad de precio', product.priceUnit], ['Identificador', product.id]];
    document.querySelector('#detail-list').replaceChildren(...values.map(([label, value]) => {
      const row = element('div');
      row.append(element('dt', '', label), element('dd', '', value));
      return row;
    }));
    document.querySelector('#detail-price').textContent = formatPrice(product.price, product.currency);
    document.querySelector('#detail-currency').textContent = `Moneda: ${product.currency}`;
    document.querySelector('#detail-original-price').textContent = `Precio original: ${formatPrice(product.originalPrice, product.currency)}`;
    document.querySelector('#detail-extracted-at').textContent = `Datos extraídos: ${product.extractedAt}`;
    const source = document.querySelector('#detail-source');
    const url = safeUrl(product.productUrl);
    source.hidden = !url;
    if (url) source.href = url;
    else source.removeAttribute('href');
    detailContent.hidden = false;
    detailStatus.hidden = true;
    detailStatus.replaceChildren();
    dialog.setAttribute('aria-labelledby', 'detail-title');
    dialog.removeAttribute('aria-label');
  } catch (error) {
    if (request !== detailRequest) return;
    detailStatus.replaceChildren(statePanel('error', 'No pudimos cargar el detalle', 'Inténtalo nuevamente en unos momentos.',
      'Reintentar', () => openDetail(id)));
    console.error(error);
  }
}

grid.addEventListener('click', event => {
  const button = event.target.closest('[data-product-id]');
  if (button) openDetail(button.dataset.productId);
});
document.querySelector('#dialog-close').addEventListener('click', () => dialog.close());
dialog.addEventListener('click', event => { if (event.target === dialog) dialog.close(); });
dialog.addEventListener('close', () => { detailRequest++; });
search.addEventListener('input', () => loadProducts(0));
[categoryFilter, formatFilter].forEach(control => control.addEventListener('change', () => loadProducts(0)));
previous.addEventListener('click', () => { if (currentPage?.hasPrevious) loadProducts(currentPage.page - 1); });
next.addEventListener('click', () => { if (currentPage?.hasNext) loadProducts(currentPage.page + 1); });

clearFilters.addEventListener('click', resetFilters);
loadFilters();
loadProducts();
