/* Giao dien web. Viec tinh tien, kiem tra gio hang va dat hang duoc Java xu ly. */
let state = { foods: [], cart: [], count: 0, total: 0 };
let currentCategory = 'Tất cả';
let busy = false;
let quoteSerial = 0;

const $ = id => document.getElementById(id);
const money = value => Number(value).toLocaleString('vi-VN') + ' ₫';
const escapeHtml = value => String(value).replace(/[&<>"']/g, ch => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[ch]));
const removeMarks = text => String(text).normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase();

// Ten hien thi va anh minh hoa. Gia, trang thai va gio hang van lay tu Java.
const productInfo = {
  M01: { name: 'Cơm gà', type: 'Món cơm', photo: 'Bogus_Chicken_rice.jpg' },
  M02: { name: 'Bún bò', type: 'Món bún', photo: 'BunboHue.jpg' },
  M03: { name: 'Bánh mì', type: 'Bánh mì', photo: 'Banh_mi_sandwich.jpg' },
  M04: { name: 'Trà sữa', type: 'Đồ uống', photo: 'Bubble_Tea.jpg' },
  M05: { name: 'Phở bò', type: 'Món phở', photo: 'Phở_bò.JPG' }
};
function niceName(food) { return productInfo[food.id]?.name || food.name; }
function photoUrl(id) {
  const file = productInfo[id]?.photo;
  return file ? 'https://commons.wikimedia.org/wiki/Special:FilePath/' + encodeURIComponent(file) + '?width=700' : '';
}
function foodPhoto(id, thumb = false) {
  const name = productInfo[id]?.name || 'Món ăn';
  const url = photoUrl(id);
  if (!url) return `<div class="photo-fallback">${escapeHtml(name)}</div>`;
  return `<div class="photo-fallback">${escapeHtml(name)}</div><img loading="lazy" src="${url}" alt="Ảnh minh họa ${escapeHtml(name)}" referrerpolicy="no-referrer" onerror="this.parentElement.classList.add('no-photo');this.remove()">`;
}

async function request(path, values) {
  const options = values ? {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams(values)
  } : {};
  const response = await fetch(path, options);
  const data = await response.json();
  if (!response.ok || data.ok === false) { const error = new Error(data.message || 'Có lỗi xảy ra.'); error.status = response.status; throw error; }
  return data;
}
async function loadData() {
  try {
    state = await request('/api/state');
    drawFoods();
    drawCart();
    $('studentName').textContent = state.student.name;
    $('studentRoom').textContent = 'Phòng ' + state.student.room;
    $('balance').textContent = money(state.student.balance);
    $('mobileCount').textContent = state.count;
    $('openingInfo').classList.toggle('closed', !state.orderingOpen);
    $('openingInfo').textContent = state.orderingOpen
      ? 'Đang nhận đơn · 05:00 – 23:00 hằng ngày (giờ Việt Nam).'
      : 'Căn tin đã ngừng nhận đơn. Giờ mở đặt hàng: 05:00 – 23:00 (giờ Việt Nam).';
    if (!$('forOther').checked && !$('shippingAddress').value) $('shippingAddress').value = state.student.room;
    updateQuote();
  } catch (error) {
    if (error.status === 401) showLogin();
    else showToast('Không kết nối được với chương trình Java: ' + error.message, true);
  }
}
function showToast(message, isError = false) {
  const node = $('toast');
  node.textContent = message;
  node.className = 'toast show' + (isError ? ' error' : '');
  clearTimeout(window.toastTimeout);
  window.toastTimeout = setTimeout(() => node.className = 'toast', 3200);
}
function drawFoods() {
  const foodTypes = ['Tất cả', ...new Set(state.foods.map(f => productInfo[f.id]?.type || f.category))];
  if (!foodTypes.includes(currentCategory)) currentCategory = 'Tất cả';
  $('categoryButtons').innerHTML = foodTypes.map(type =>
    `<button type="button" class="chip ${type === currentCategory ? 'active' : ''}" data-category="${escapeHtml(type)}">${escapeHtml(type)}</button>`
  ).join('');
  const keyword = removeMarks($('searchInput').value.trim());
  const visible = state.foods.filter(f => {
    const type = productInfo[f.id]?.type || f.category;
    return (currentCategory === 'Tất cả' || currentCategory === type) &&
      (removeMarks(niceName(f)).includes(keyword) || removeMarks(f.name).includes(keyword));
  });
  $('menuCount').textContent = visible.length + ' món';
  $('foodGrid').innerHTML = visible.length ? visible.map(f => `
    <article class="food-card ${f.available ? '' : 'unavailable'}">
      <div class="food-image">
        ${foodPhoto(f.id)}
        ${f.available ? '' : '<span class="stock-badge">Tạm hết hàng</span>'}
      </div>
      <div class="food-details">
        <div class="food-category">${escapeHtml(productInfo[f.id]?.type || f.category)}</div>
        <h3 class="food-name">${escapeHtml(niceName(f))}</h3>
        <div class="food-bottom">
          <span class="food-price">${money(f.price)}</span>
          <button type="button" class="add-btn" data-add="${escapeHtml(f.id)}" ${f.available ? '' : 'disabled'}>${f.available ? '+ Thêm món' : 'Hết hàng'}</button>
        </div>
      </div>
    </article>`).join('') : '<div class="no-result">Không có món phù hợp. Bạn thử tìm bằng tên khác nhé.</div>';
}
function drawCart() {
  $('cartCount').textContent = state.count;
  $('cartTotal').textContent = money(state.total);
  $('checkoutBtn').disabled = state.count === 0 || busy || !state.orderingOpen;
  $('closedNote').hidden = !!state.orderingOpen;
  if (!state.cart.length) {
    $('cartItems').innerHTML = '<div class="empty-cart"><span class="empty-cart-icon">＋</span><div>Chưa có món nào trong giỏ.<br>Chọn món từ thực đơn để bắt đầu.</div></div>';
    return;
  }
  $('cartItems').innerHTML = state.cart.map(item => `
    <div class="cart-item">
      <div class="cart-thumb">${foodPhoto(item.id, true)}</div>
      <div class="cart-details">
        <div class="cart-item-top"><strong>${escapeHtml(niceName(item))}</strong><span class="item-subtotal">${money(item.subtotal)}</span></div>
        <div class="qty-row">
          <button type="button" data-change="${escapeHtml(item.id)}" data-delta="-1" aria-label="Giảm số lượng">−</button>
          <span class="qty-value">${item.quantity}</span>
          <button type="button" data-change="${escapeHtml(item.id)}" data-delta="1" aria-label="Tăng số lượng">+</button>
          <button type="button" class="remove" data-remove="${escapeHtml(item.id)}" aria-label="Xóa món">Xóa</button>
        </div>
      </div>
    </div>`).join('');
}
async function action(path, values, defaultMessage) {
  if (busy) return null;
  busy = true;
  $('checkoutBtn').disabled = true;
  try {
    const data = await request(path, values);
    await loadData();
    showToast(defaultMessage || data.message || 'Đã cập nhật.');
    return data;
  } catch (error) {
    showToast(error.message, true);
    return null;
  } finally {
    busy = false;
    $('checkoutBtn').disabled = state.count === 0 || !state.orderingOpen;
  }
}
function showModal(html) { $('modalBody').innerHTML = html; $('modal').hidden = false; }
function hideModal() { $('modal').hidden = true; }

// Hien ma QR de quet THU trong demo; neu chu cua hang cau hinh ngan hang thi dung VietQR.
// QR thuc chi la MA CHUYEN KHOAN: de xac nhan thanh toan can doi soat giao dich ngan hang.
function isRealBankConfigured() {
  const c = window.KTX_PAYMENT_CONFIG || {};
  return c.mode === 'real' && /^[A-Za-z0-9]{2,12}$/.test(c.bankId || '') &&
    /^\d{6,19}$/.test(c.accountNo || '') && String(c.accountName || '').trim().length >= 3;
}
function demoQrBlock(kind, id, amount) {
  const text = ['KTX FOOD - THANH TOAN MO PHONG', 'Loai: ' + kind,
    'Ma: ' + id, 'So tien VND: ' + amount, 'KHONG CHUYEN TIEN THAT'].join('\n');
  const svg = window.KtxQRCode ? window.KtxQRCode.svg(text) : '<p>Khong tao duoc QR demo.</p>';
  return `<div class="qr-payment-box">
    <div class="qr-label">MÃ QR DEMO · KHÔNG CHUYỂN TIỀN THẬT</div>
    <div class="qr-image">${svg}</div>
    <p class="qr-small">Bạn có thể quét bằng camera để xem thông tin mô phỏng. Đây <strong>không phải</strong> mã VietQR chuyển tiền ngân hàng.</p>
    <div class="qr-details"><div>Mã tham chiếu <strong>${escapeHtml(id)}</strong></div><div>Số tiền <strong>${money(amount)}</strong></div></div>
  </div>`;
}
function bankQrBlock(order) {
  if (!isRealBankConfigured()) return demoQrBlock('DON HANG', order.id, order.total);
  const c = window.KTX_PAYMENT_CONFIG;
  // VietQR Quick Link: so tien + noi dung chuyen khoan gan voi ma don.
  // Noi dung phai ngan va khong dau, de ung dung ngan hang doc duoc.
  const description = order.id.replace(/[^A-Za-z0-9]/g, '').slice(0,25);
  const url='https://img.vietqr.io/image/' + encodeURIComponent(c.bankId) + '-' +
    encodeURIComponent(c.accountNo) + '-compact2.png?amount=' + encodeURIComponent(order.total) +
    '&addInfo=' + encodeURIComponent(description) + '&accountName=' + encodeURIComponent(c.accountName);
  return `<div class="qr-payment-box">
    <div class="qr-label">VIETQR · CHUYỂN KHOẢN NGÂN HÀNG</div>
    <img class="qr-bank-image" src="${escapeHtml(url)}" alt="Mã QR VietQR chuyển khoản" referrerpolicy="no-referrer" onerror="this.style.display='none';this.nextElementSibling.hidden=false">
    <p hidden>Không tải được hình QR. Kiểm tra Internet hoặc thông tin ngân hàng.</p>
    <div class="qr-details"><div>Ngân hàng <strong>${escapeHtml(c.bankId)}</strong></div>
      <div>Số tài khoản <strong>${escapeHtml(c.accountNo)}</strong></div>
      <div>Chủ tài khoản <strong>${escapeHtml(c.accountName)}</strong></div>
      <div>Thành tiền <strong>${money(order.total)}</strong></div>
      <div>Nội dung <strong>${escapeHtml(description)}</strong></div></div>
    <p class="qr-small">Chỉ chuyển tiền khi bạn đã kiểm tra đúng tên người nhận trong ứng dụng ngân hàng. Đơn hàng vẫn <strong>chờ xác nhận</strong> cho tới khi bên nhận kiểm tra giao dịch thật.</p>
  </div>`;
}


$('searchInput').addEventListener('input', drawFoods);
$('categoryButtons').addEventListener('click', event => {
  const button = event.target.closest('[data-category]');
  if (button) { currentCategory = button.dataset.category; drawFoods(); }
});
$('foodGrid').addEventListener('click', event => {
  const button = event.target.closest('[data-add]');
  if (button && !button.disabled) action('/api/cart/add', { foodId: button.dataset.add, quantity: 1 }, 'Đã thêm món vào giỏ hàng.');
});
$('cartItems').addEventListener('click', event => {
  const remove = event.target.closest('[data-remove]');
  if (remove) { action('/api/cart/remove', { foodId: remove.dataset.remove }, 'Đã xóa món khỏi giỏ.'); return; }
  const change = event.target.closest('[data-change]');
  if (change) {
    const item = state.cart.find(f => f.id === change.dataset.change);
    if (!item) return;
    const next = item.quantity + Number(change.dataset.delta);
    if (next < 1) action('/api/cart/remove', { foodId: item.id });
    else action('/api/cart/update', { foodId: item.id, quantity: next });
  }
});
$('checkoutBtn').addEventListener('click', async () => {
  const payment = document.querySelector('input[name="payment"]:checked').value;
  const data = await action('/api/order', { payment, ...getOrderOptions() }, 'Đã tạo đơn hàng.');
  if (!data) return;
  const order = data.order;
  const message = payment === 'wallet'
    ? 'Đã trừ tiền trong ví sinh viên (mô phỏng).'
    : payment === 'bank' ? 'Đơn được tạo và đang chờ xác nhận chuyển khoản.'
    : 'Đơn đang chờ thanh toán bằng tiền mặt.';
  const paymentQr = payment === 'bank' ? bankQrBlock(order) : '';
  showModal(`<span class="modal-check">✓</span><h2>Đã tạo đơn hàng</h2>
    <p>Mã đơn: <strong>${escapeHtml(order.id)}</strong></p>
    <p>Tổng tiền: <strong>${money(order.total)}</strong> (Món ${money(order.subtotal)} · Ship ${money(order.shippingFee)} · Giảm ${money(order.discount)})</p>
    <p>Nhận hàng: ${order.delivery === 'ship' ? 'Ship tới KTX' : 'Tự nhận tại căn tin'} · Người nhận: ${escapeHtml(order.recipientName)}</p>
    <p>Địa chỉ: ${escapeHtml(order.address)}</p>
    <p>Hình thức: ${escapeHtml(order.payment)}</p>
    <span class="order-status">${escapeHtml(order.status)}</span>
    <p>${message}</p>${paymentQr}`);
  $('cartPanel').classList.remove('open');
});

function formattedDate(date) {
  // Date Java LocalDateTime khong kem mui gio; hien thi chuoi gio Viet Nam.
  return escapeHtml(String(date || '').replace('T', ' ').slice(0, 19));
}
function historyTabs(active) {
  const tabs = [['orders','Lịch sử mua'], ['payments','Lịch sử thanh toán'], ['topups','Lịch sử nạp']];
  return `<div class="history-tabs">${tabs.map(([type,label])=>
    `<button type="button" data-history="${type}" class="${active===type?'active':''}">${label}</button>`).join('')}</div>`;
}
async function openHistory(type='orders') {
  try {
    const data = await request('/api/' + type);
    let html='';
    if (type === 'orders') html = data.orders.length ? data.orders.map(o =>
      `<div class="history-item"><strong>Đơn ${escapeHtml(o.id)} · ${money(o.total)}</strong>
      <p>${formattedDate(o.date)} · ${escapeHtml(o.status)}</p>
      <p>${o.details.map(d=>escapeHtml(d.name) + ' × ' + d.quantity).join(' · ')}</p>
      <p>${o.delivery==='ship'?'Giao tới phòng KTX':'Nhận tại căn tin'} · ${escapeHtml(o.recipientName)}${o.forOther?' (đặt hộ)':''}</p>
      <small>Địa điểm: ${escapeHtml(o.address)} · Phí ship: ${money(o.shippingFee)} · Voucher: ${escapeHtml(o.voucher||'Không có')} · Giảm: ${money(o.discount)}</small></div>`).join('') : '<p>Bạn chưa mua đơn hàng nào.</p>';
    if (type === 'payments') html = data.payments.length ? data.payments.map(t =>
      `<div class="history-item"><strong>${money(t.amount)} · ${escapeHtml(t.method)}</strong>
      <p>${formattedDate(t.date)} · Đơn ${escapeHtml(t.orderId)}</p>
      <p>Trạng thái: ${escapeHtml(t.status)}</p></div>`).join('') : '<p>Chưa có giao dịch thanh toán đơn hàng.</p>';
    if (type === 'topups') html = data.topups.length ? data.topups.map(t =>
      `<div class="history-item"><strong>+ ${money(t.amount)}</strong>
      <p>${formattedDate(t.date)} · ${t.confirmed?'Đã nạp thành công (giả lập)':'Chờ xác nhận ngân hàng giả lập'}</p>
      <small>Mã giao dịch: ${escapeHtml(t.id)}</small></div>`).join('') : '<p>Chưa có giao dịch nạp ví.</p>';
    showModal(`<h2>Lịch sử giao dịch</h2>${historyTabs(type)}${html}`);
  } catch(error) { showToast(error.message,true); }
}
$('historyBtn').addEventListener('click',()=>openHistory('orders'));
$('paymentHistoryBtn').addEventListener('click',()=>openHistory('payments'));
$('topupHistoryBtn').addEventListener('click',()=>openHistory('topups'));
$('historyQuick').addEventListener('click',()=>openHistory('orders'));
$('paymentsQuick').addEventListener('click',()=>openHistory('payments'));
$('topupsQuick').addEventListener('click',()=>openHistory('topups'));
$('modalBody').addEventListener('click',e=>{
  const tab=e.target.closest('[data-history]');
  if(tab) openHistory(tab.dataset.history);
});

function getOrderOptions() {
  return {
    delivery: document.querySelector('input[name="delivery"]:checked').value,
    forOther: $('forOther').checked ? 'true' : 'false',
    recipientName: $('forOther').checked ? $('recipientName').value.trim() : '',
    recipientPhone: $('forOther').checked ? $('recipientPhone').value.trim() : '',
    address: $('shippingAddress').value.trim(),
    voucher: $('voucherCode').value.trim().toUpperCase()
  };
}
function updateOptionFields() {
  const ship=document.querySelector('input[name="delivery"]:checked').value==='ship';
  const other=$('forOther').checked;
  $('recipientFields').hidden=!other;
  $('shippingFields').hidden=!ship;
  if (!other && state.student && ship) $('shippingAddress').value=state.student.room;
  if (other && ship && state.student && $('shippingAddress').value===state.student.room) $('shippingAddress').value='';
  updateQuote();
}
async function updateQuote() {
  const serial=++quoteSerial;
  if(!state.cart.length) {
    $('shippingFee').textContent='0 ₫';$('discountAmount').textContent='0 ₫';
    $('grandTotal').textContent='0 ₫';$('voucherMessage').textContent='';return;
  }
  try {
    const data=await request('/api/quote',getOrderOptions());
    if (serial!==quoteSerial) return;
    $('shippingFee').textContent=money(data.quote.shippingFee);
    $('discountAmount').textContent='− '+money(data.quote.discount);
    $('grandTotal').textContent=money(data.quote.total);
    $('voucherMessage').textContent=data.quote.voucher ? 'Đã áp dụng voucher '+data.quote.voucher+'.' : '';
    $('voucherMessage').classList.remove('error');
  } catch (error) {
    if (serial!==quoteSerial) return;
    $('shippingFee').textContent='—';$('discountAmount').textContent='—';
    $('grandTotal').textContent='Kiểm tra mã';
    $('voucherMessage').textContent=error.message;
    $('voucherMessage').classList.add('error');
  }
}
document.querySelectorAll('input[name="delivery"]').forEach(node=>node.addEventListener('change',updateOptionFields));
$('forOther').addEventListener('change',updateOptionFields);
$('applyVoucher').addEventListener('click',updateQuote);
$('voucherCode').addEventListener('keydown',e=>{if(e.key==='Enter'){e.preventDefault();updateQuote();}});
$('modalClose').addEventListener('click', hideModal);
$('modal').addEventListener('click', event => { if (event.target === $('modal')) hideModal(); });
document.addEventListener('keydown', event => { if (event.key === 'Escape') hideModal(); });
$('mobileCartBtn').addEventListener('click', () => $('cartPanel').classList.add('open'));
$('walletBtn').addEventListener('click', openWallet);
$('topupBtn').addEventListener('click', openWallet);
$('logoutBtn').addEventListener('click', logout); 
$('closeCart').addEventListener('click', () => $('cartPanel').classList.remove('open'));
// Trang dang nhap duoc hien neu chua co phien dang nhap.
function showLogin() {
  $('appShell').hidden = true;
  $('loginScreen').hidden = false;
  $('loginPassword').value = '';
}
function showApp() {
  $('loginScreen').hidden = true;
  $('appShell').hidden = false;
}
$('loginForm').addEventListener('submit', async event => {
  event.preventDefault();
  $('loginError').textContent = '';
  const button = $('loginForm').querySelector('button[type="submit"]');
  button.disabled = true;
  try {
    await request('/api/login', { studentId: $('loginId').value, password: $('loginPassword').value });
    showApp();
    await loadData();
  } catch (error) {
    $('loginError').textContent = error.message;
  } finally { button.disabled = false; }
});
async function logout() {
  try { await request('/api/logout', {}); }
  catch (error) { showToast(error.message, true); }
  hideModal();
  $('cartPanel').classList.remove('open');
  showLogin();
}
function topupHistory(list) {
  if (!list.length) return '<p>Chưa có giao dịch nạp ví.</p>';
  return list.map(t => `<p><strong>${money(t.amount)}</strong> · ${t.confirmed ? 'Đã cộng ví' : 'Chờ xác nhận'}<br><small>${escapeHtml(t.id)}</small></p>`).join('');
}
async function openWallet() {
  try {
    const data = await request('/api/topups');
    showModal(`<h2>Nạp tiền vào ví sinh viên</h2>
      <p>Số dư hiện tại: <strong>${money(state.student.balance)}</strong></p>
      <div class="wallet-note">ĐÂY LÀ NGÂN HÀNG GIẢ LẬP. Không chuyển tiền thật và không nhập thông tin tài khoản ngân hàng cá nhân.</div>
      <form class="wallet-form" id="topupForm">
        <label for="topupAmount">Số tiền muốn nạp (VND)</label>
        <input id="topupAmount" type="number" min="10000" max="2000000" step="1000" value="50000" required>
        <button type="submit" class="modal-main-button">Tạo yêu cầu nạp ví</button>
      </form>
      <div class="topup-history"><h3>Lịch sử nạp ví</h3>${topupHistory(data.topups)}</div>`);
  } catch (error) { showToast(error.message, true); }
}
$('modalBody').addEventListener('submit', async event => {
  if (event.target.id !== 'topupForm') return;
  event.preventDefault();
  const button = event.target.querySelector('button');
  button.disabled = true;
  try {
    const data = await request('/api/topups/request', { amount: $('topupAmount').value });
    const t = data.topup;
    showModal(`<h2>Nạp tiền vào ví · QR demo</h2>
      <div class="wallet-note">Đây là ngân hàng giả lập. Quét QR chỉ để xem thông tin, không phát sinh giao dịch thật.</div>
      ${demoQrBlock('NAP VI', t.id, t.amount)}
      <p>Chưa cộng tiền vào ví vì giao dịch đang chờ xác nhận.</p>
      <button class="modal-main-button" type="button" data-confirm-topup="${escapeHtml(t.id)}">Mô phỏng ngân hàng xác nhận đã chuyển</button>`);
  } catch (error) { showToast(error.message, true); button.disabled = false; }
});
$('modalBody').addEventListener('click', async event => {
  const button = event.target.closest('[data-confirm-topup]');
  if (!button) return;
  button.disabled = true;
  try {
    const data = await request('/api/topups/mock-confirm', { id: button.dataset.confirmTopup });
    await loadData();
    showModal(`<span class="modal-check">✓</span><h2>Nạp ví giả lập thành công</h2>
      <p>Đã cộng <strong>${money(data.topup.amount)}</strong> vào ví sinh viên.</p>
      <p>Số dư mới: <strong>${money(state.student.balance)}</strong></p>
      <p>Đây là dữ liệu mô phỏng, không có tiền thật được chuyển.</p>`);
  } catch (error) { showToast(error.message, true); button.disabled = false; }
});
async function startApp() {
  try {
    state = await request('/api/state');
    showApp();
    await loadData();
  } catch (error) { showLogin(); }
}
startApp();
