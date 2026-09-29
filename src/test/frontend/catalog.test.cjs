// No dependencies: exercises the actual frontend script and HTTP API with a minimal DOM.
// This does not replace browser layout/accessibility verification.
const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const base = process.env.CATALOG_TEST_URL || 'http://localhost:8080';
const script = fs.readFileSync('mockup/app.js', 'utf8');
const html = fs.readFileSync('mockup/index.html', 'utf8');

class Element {
  constructor(tag = 'div') {
    this.tagName = tag;
    this.children = [];
    this.listeners = {};
    this.attributes = {};
    this.dataset = {};
    this.value = '';
    this.textContent = '';
    this.className = '';
  }
  append(...nodes) { nodes.forEach(node => { node.parentElement = this; this.children.push(node); }); }
  replaceChildren(...nodes) { this.children = []; this.append(...nodes); }
  setAttribute(key, value) { this.attributes[key] = value; }
  removeAttribute(key) { delete this.attributes[key]; }
  addEventListener(type, fn) { (this.listeners[type] ||= []).push(fn); }
  dispatch(type, event = {}) { (this.listeners[type] || []).forEach(fn => fn(event)); }
  querySelector(selector) { return this.all().find(node => selector.startsWith('.') && node.className.split(' ').includes(selector.slice(1))); }
  all() { return this.children.flatMap(node => [node, ...node.all()]); }
  remove() { this.parentElement.children = this.parentElement.children.filter(node => node !== this); }
  after(node) { this.afterNode = node; }
  showModal() { this.open = true; }
  close() { this.open = false; this.dispatch('close'); }
}

function app(fetcher = (url, options) => fetch(new URL(url, base), options)) {
  const nodes = new Map([...html.matchAll(/id="([^"]+)"/g)].map(match => [`#${match[1]}`, new Element()]));
  nodes.set('.pages', new Element());
  nodes.set('.toolbar', new Element());
  new Element().append(nodes.get('#detail-image'));
  const requests = [];
  const errors = [];
  const pending = new Set();
  const document = {
    createElement: tag => new Element(tag),
    querySelector: selector => nodes.get(selector),
    querySelectorAll: () => [],
  };
  const context = vm.createContext({ document, URL, URLSearchParams, Intl,
    console: { error: error => errors.push(error) },
    fetch: (url, options) => {
      requests.push(url);
      const promise = fetcher(url, options);
      pending.add(promise);
      promise.then(() => pending.delete(promise), () => pending.delete(promise));
      return promise;
    },
  });
  vm.runInContext(script, context);
  return {
    nodes, requests, errors, context,
    async settle() {
      // Response body decoding and async event handlers need to complete too.
      await Promise.allSettled([...pending]);
      for (let i = 0; i < 10; i++) await new Promise(resolve => setTimeout(resolve, 10));
    },
    run: code => vm.runInContext(code, context),
  };
}

test('serves frontend and real catalog from the same origin', async () => {
  for (const path of ['/', '/app.js', '/styles.css']) {
    const response = await fetch(base + path);
    assert.equal(response.status, 200, path);
    assert.match(response.headers.get('content-type'), path.endsWith('.js') ? /javascript/ : path.endsWith('.css') ? /css/ : /html/);
  }
  const ui = app(); await ui.settle();
  assert.equal(ui.errors.length, 0);
  const page = await (await fetch(base + '/api/products')).json();
  assert.equal(page.totalElements, 4032);
  assert.equal(page.totalPages, 504);
  assert.deepEqual(ui.nodes.get('#product-grid').children.map(card => card.children[0].dataset.productId), page.items.map(item => item.id));
  assert.equal(ui.nodes.get('#category-filter').children.length, 26);
  assert.equal(ui.nodes.get('#format-filter').children.length, 252);
  assert.match(ui.nodes.get('#result-count').textContent, /1-8 de 4.032/);
  assert.equal(ui.nodes.get('#previous-page').disabled, true);
});

test('navigation uses real metadata, last page and changing filters resets page', async () => {
  const ui = app(); await ui.settle();
  ui.nodes.get('#next-page').dispatch('click'); await ui.settle();
  assert.match(ui.requests.at(-1), /page=1/);
  assert.match(ui.nodes.get('#result-count').textContent, /9-16/);
  await ui.run('loadProducts(503)');
  assert.equal(ui.nodes.get('#next-page').disabled, true);
  assert.match(ui.nodes.get('#result-count').textContent, /4025-4032/);
  ui.nodes.get('#search').value = 'CHOCOLATE';
  ui.nodes.get('#category-filter').value = 'Huevos, leche y mantequilla';
  ui.nodes.get('#format-filter').value = '6 mini bricks x 200 ml';
  ui.nodes.get('#format-filter').dispatch('change'); await ui.settle();
  const url = new URL(ui.requests.at(-1), base);
  assert.equal(url.searchParams.get('page'), '0');
  assert.equal(url.searchParams.get('search'), 'CHOCOLATE');
  assert.equal(url.searchParams.get('category'), 'Huevos, leche y mantequilla');
  assert.equal(url.searchParams.get('format'), '6 mini bricks x 200 ml');
  assert.ok(ui.nodes.get('#product-grid').children.length > 0);
  ui.nodes.get('#search').value = 'no-such-product-xyz';
  ui.nodes.get('#search').dispatch('input'); await ui.settle();
  assert.equal(ui.nodes.get('#product-grid').dataset.state, 'empty');
  assert.equal(ui.nodes.get('#next-page').disabled, true);
});

test('detail fetches exact suffixed ID and displays original fields and image URL', async () => {
  const ui = app(); await ui.settle();
  await ui.run("openDetail('12049.1')");
  const product = await (await fetch(base + '/api/products/12049.1')).json();
  assert.equal(ui.requests.at(-1), '/api/products/12049.1');
  assert.equal(ui.nodes.get('#detail-title').textContent, product.name);
  assert.equal(ui.nodes.get('#detail-description').textContent, product.description);
  assert.equal(ui.nodes.get('#detail-image').src, product.imageUrl);
  assert.equal(ui.nodes.get('#detail-source').href, product.productUrl);
  assert.equal(ui.nodes.get('#detail-list').children[0].children[1].textContent, product.category);
  assert.equal(ui.nodes.get('#product-dialog').open, true);
  ui.nodes.get('#dialog-close').dispatch('click');
  assert.equal(ui.nodes.get('#product-dialog').open, false);
});

test('catalog text remains text and failed images have a reusable fallback', async () => {
  const ui = app(); await ui.settle();
  const card = ui.run("productCard({id:'12049.1', name:'<img onerror=alert(1)>', category:'A > B', format:'<b>test</b>', price:10, currency:'CLP', imageUrl:'https://example.com/missing.jpg'})");
  assert.equal(card.querySelector('.card-copy').children[1].textContent, '<img onerror=alert(1)>');
  const wrap = card.querySelector('.image-wrap');
  const image = wrap.children[0]; image.onerror(); image.onerror();
  assert.equal(image.hidden, true);
  assert.equal(wrap.children.filter(node => node.className === 'image-fallback').length, 1);
  assert.equal(ui.run("safeUrl('javascript:alert(1)')"), '');
  assert.equal(ui.run("safeUrl('data:text/html,test')"), '');
});

test('older list and detail responses cannot overwrite the latest selection', async () => {
  const queue = [];
  const ui = app(url => new Promise(resolve => queue.push({url, resolve})));
  const response = value => ({ ok:true, json:async () => value });
  queue[0].resolve(response({categories:[], formats:[]}));
  ui.run('loadProducts(1)');
  const page = number => ({items:[], page:number, size:8, totalElements:0, totalPages:0, hasPrevious:false, hasNext:false});
  queue[2].resolve(response(page(1)));
  await new Promise(resolve => setImmediate(resolve));
  queue[1].resolve(response(page(0)));
  await ui.settle();
  assert.equal(ui.run('currentPage.page'), 1);
  ui.run("openDetail('12049.1')"); ui.run("openDetail('12049.2')");
  const product = id => ({id, name:id, description:id, category:'A', format:'B', price:10, originalPrice:10, currency:'CLP', imageUrl:'https://example.com/a', productUrl:'https://example.com/b'});
  queue[4].resolve(response(product('12049.2')));
  await new Promise(resolve => setImmediate(resolve));
  queue[3].resolve(response(product('12049.1')));
  await ui.settle();
  assert.equal(ui.nodes.get('#detail-title').textContent, '12049.2');
});

test('HTTP failures are visible and navigation stays disabled', async () => {
  const ui = app(async () => ({ok:false, status:500})); await ui.settle();
  assert.equal(ui.nodes.get('#product-grid').dataset.state, 'error');
  assert.equal(ui.nodes.get('#next-page').disabled, true);
  assert.equal(ui.nodes.get('#category-filter').disabled, true);
  assert.equal(ui.errors.length, 2);
});

const response = value => ({ok:true, json:async () => value});
const emptyPage = {items:[], page:0, size:8, totalElements:0, totalPages:0, hasPrevious:false, hasNext:false};
const clickAction = node => node.all().find(child => child.tagName === 'button').dispatch('click');

test('pending requests show loading; empty results allow clearing all filters', async () => {
  let resolveList;
  const ui = app(url => url.endsWith('/filters') ? Promise.resolve(response({categories:[], formats:[]})) : new Promise(resolve => { resolveList = resolve; }));
  const grid = ui.nodes.get('#product-grid');
  assert.equal(grid.dataset.state, 'loading');
  assert.equal(grid.attributes['aria-busy'], 'true');
  assert.equal(ui.nodes.get('#pagination').hidden, true);
  ui.nodes.get('#search').value = 'missing';
  ui.nodes.get('#category-filter').value = 'A';
  ui.nodes.get('#format-filter').value = 'B';
  resolveList(response(emptyPage)); await ui.settle();
  assert.equal(grid.dataset.state, 'empty');
  assert.equal(grid.attributes['aria-busy'], 'false');
  clickAction(grid);
  assert.equal(ui.requests.at(-1), '/api/products?page=0');
  for (const id of ['search', 'category-filter', 'format-filter']) assert.equal(ui.nodes.get('#' + id).value, '');
  resolveList(response(emptyPage)); await ui.settle();
  assert.equal(grid.all().filter(node => node.tagName === 'button').length, 0);
});

test('network, HTTP, JSON and rendering errors offer retry preserving page and query', async () => {
  for (const failure of [() => Promise.reject(new Error('offline')), async () => ({ok:false,status:503}),
    async () => ({ok:true,json:async () => {throw new Error('invalid JSON');}}), async () => response({items:[{}]})]) {
    let fail = true;
    const ui = app(url => url.endsWith('/filters') ? Promise.resolve(response({categories:[],formats:[]})) : fail ? failure() : Promise.resolve(response(emptyPage)));
    await ui.settle();
    ui.nodes.get('#search').value = 'leche';
    await ui.run('loadProducts(3)');
    assert.equal(ui.nodes.get('#product-grid').dataset.state, 'error');
    assert.equal(ui.nodes.get('#pagination').hidden, true);
    fail = false; clickAction(ui.nodes.get('#product-grid')); await ui.settle();
    assert.equal(ui.requests.at(-1), '/api/products?page=3&search=leche');
    assert.equal(ui.nodes.get('#product-grid').dataset.state, 'empty');
  }
});

test('filter failure can recover without duplicate options or losing the search', async () => {
  let fail = true;
  const ui = app(async url => url.endsWith('/filters') ? fail ? {ok:false,status:500} : response({categories:['A'],formats:['B']}) : response(emptyPage));
  await ui.settle();
  ui.nodes.get('#search').value = 'leche';
  fail = false; clickAction(ui.nodes.get('#filter-status')); await ui.settle();
  assert.equal(ui.nodes.get('#filter-status').hidden, true);
  assert.equal(ui.nodes.get('#category-filter').disabled, false);
  await ui.run('loadFilters()');
  assert.equal(ui.nodes.get('#category-filter').children.length, 1);
  assert.equal(ui.nodes.get('#search').value, 'leche');
});

test('detail loading, retry and closing a pending request preserve the catalog', async () => {
  const queue = [];
  const ui = app(url => url.includes('/12049.1') ? new Promise(resolve => queue.push(resolve)) : Promise.resolve(response(url.endsWith('/filters') ? {categories:[],formats:[]} : emptyPage)));
  await ui.settle();
  ui.run("openDetail('12049.1')");
  assert.equal(ui.nodes.get('#product-dialog').open, true);
  assert.equal(ui.nodes.get('#detail-content').hidden, true);
  queue.shift()({ok:false,status:404}); await ui.settle();
  clickAction(ui.nodes.get('#detail-status'));
  assert.equal(ui.requests.at(-1), '/api/products/12049.1');
  ui.nodes.get('#dialog-close').dispatch('click');
  queue.shift()(response({id:'12049.1'})); await ui.settle();
  assert.equal(ui.nodes.get('#product-dialog').open, false);
  assert.equal(ui.nodes.get('#product-grid').dataset.state, 'empty');
});

test('demo controls are removed', () => {
  assert.doesNotMatch(html, /state-switch|data-state=/);
  assert.doesNotMatch(script, /PLANTILLAS|innerHTML/);
});
