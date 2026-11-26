/* Giao dien web. Viec tinh tien, kiem tra gio hang va dat hang duoc Java xu ly. */
let state = { foods: [], cart: [], count: 0, total: 0 };
let currentCategory = 'Tất cả';
let busy = false;

const $ = id => document.getElementById(id);
const money = value => Number(value).toLocaleString('vi-VN') + ' ₫';
const escapeHtml = value => String(value).replace(/[&<>"']/g, ch => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[ch]));
const removeMarks = text => String(text).normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase();

// Ten hien thi va anh minh hoa. Gia, trang thai va gio hang van lay tu Java.
const productInfo = {
  M01: { name: 'Cơm gà', type: 'Món cơm', photo: 'images/comga.jpg' },
  M02: { name: 'Bún bò', type: 'Món bún', photo: 'images/bunbo.jpeg' },
  M03: { name: 'Bánh mì', type: 'Bánh mì', photo: 'images/banhmi.jpg' },
  M04: { name: 'Trà sữa', type: 'Đồ uống', photo: 'images/trasua.jpg' },
  M05: { name: 'Phở bò', type: 'Món phở', photo: 'images/phobo.jpg' }
};
function niceName(food) { return productInfo[food.id]?.name || food.name; }
function photoUrl(id) {
    return productInfo[id]?.photo || '';
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
  $('checkoutBtn').disabled = state.count === 0 || busy;
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
    $('checkoutBtn').disabled = state.count === 0;
  }
}
function showModal(html) { $('modalBody').innerHTML = html; $('modal').hidden = false; }
function hideModal() { $('modal').hidden = true; }

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
  const data = await action('/api/order', { payment }, 'Đã tạo đơn hàng.');
  if (!data) return;
  const order = data.order;
  const message = payment === 'wallet'
    ? 'Đã trừ tiền trong ví sinh viên mẫu.'
    : 'Đơn đang chờ thanh toán. Đây là bản mô phỏng, không có giao dịch thật.';
  showModal(`<span class="modal-check">✓</span><h2>Đã tạo đơn hàng</h2>
    <p>Mã đơn: <strong>${escapeHtml(order.id)}</strong></p>
    <p>Tổng tiền: <strong>${money(order.total)}</strong></p>
    <p>Hình thức: ${escapeHtml(order.payment)}</p>
    <span class="order-status">${escapeHtml(order.status)}</span>
    <p>${message}</p>`);
  $('cartPanel').classList.remove('open');
});
$('historyBtn').addEventListener('click', async () => {
  try {
    const data = await request('/api/orders');
    const body = data.orders.length ? data.orders.map(order => `
      <div class="order-record"><strong>${escapeHtml(order.id)}</strong>
      <p>${new Date(order.date).toLocaleString('vi-VN')} · ${money(order.total)}</p>
      <p>${escapeHtml(order.payment)} · ${escapeHtml(order.status)}</p>
      <p>${order.details.map(d => escapeHtml(d.name) + ' × ' + d.quantity).join(' · ')}</p></div>`).join('') : '<p>Chưa có đơn hàng nào trong lần chạy này.</p>';
    showModal('<h2>Đơn hàng của tôi</h2><p>Lịch sử trên trang chỉ hiển thị trong lần chạy này. File đơn hàng vẫn được lưu trong thư mục data.</p>' + body);
  } catch (error) { showToast(error.message, true); }
});
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
    showModal(`<h2>Ngân hàng giả lập</h2>
      <div class="wallet-note">Không chuyển tiền thật. Bước tiếp theo mô phỏng hệ thống ngân hàng thông báo giao dịch thành công cho Java.</div>
      <div class="bank-detail"><div>Ngân hàng: <strong>PKA BANK (KHÔNG CÓ THẬT)</strong></div>
        <div>Tài khoản nhận: <strong>PKA-KTX-001</strong></div>
        <div>Số tiền: <strong>${money(t.amount)}</strong></div>
        <div>Nội dung: <strong>${escapeHtml(t.id)}</strong></div></div>
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
