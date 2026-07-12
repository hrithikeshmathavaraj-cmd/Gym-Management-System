/* =========================================================
   FILE DOWNLOADS — plain <a href> links never send the JWT,
   so PDF/Excel export buttons must fetch with the Authorization
   header and save the result via a blob URL instead.
   ========================================================= */
async function downloadFile(path, filename) {
  try {
    const token = Api.getToken();
    const response = await fetch(path, {
      headers: token ? { 'Authorization': `Bearer ${token}` } : {}
    });
    if (response.status === 401) {
      Toast.show('Session expired — please log in again', 'error');
      setTimeout(() => Api.logout(), 1200);
      return;
    }
    if (!response.ok) {
      Toast.show('Could not generate the file', 'error');
      return;
    }
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    Toast.show('Network error — please check your connection', 'error');
  }
}

/* =========================================================
   API SERVICE — thin wrapper around fetch()
   Automatically attaches JWT, parses ApiResponse envelope,
   and redirects to login on 401.
   ========================================================= */

const API_BASE = '/api';

const Api = {
  getToken() {
    return localStorage.getItem('gym_token');
  },

  setSession(authResponse) {
    localStorage.setItem('gym_token', authResponse.token);
    localStorage.setItem('gym_user', JSON.stringify({
      id: authResponse.userId,
      name: authResponse.name,
      username: authResponse.username,
      role: authResponse.role
    }));
  },

  getUser() {
    const raw = localStorage.getItem('gym_user');
    return raw ? JSON.parse(raw) : null;
  },

  logout() {
    const user = this.getUser();
    const token = this.getToken();
    // Best-effort: tell the server so it can log this in the audit trail.
    // Uses fetch directly (not Api.request) so a failure here never blocks logout.
    if (user && token) {
      fetch(`${API_BASE}/auth/logout`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ username: user.username })
      }).catch(() => {});
    }
    localStorage.removeItem('gym_token');
    localStorage.removeItem('gym_user');
    window.location.href = '/login.html';
  },

  async request(method, path, body, isFormData) {
    const headers = {};
    const token = this.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (!isFormData) headers['Content-Type'] = 'application/json';

    const options = { method, headers };
    if (body) options.body = isFormData ? body : JSON.stringify(body);

    let response;
    try {
      response = await fetch(`${API_BASE}${path}`, options);
    } catch (networkErr) {
      Toast.show('Network error — please check your connection', 'error');
      throw networkErr;
    }

    if (response.status === 401) {
      Toast.show('Session expired — please log in again', 'error');
      setTimeout(() => this.logout(), 1200);
      throw new Error('Unauthorized');
    }

    if (response.status === 503) {
      let dbMessage = 'Server cannot reach the database. Please check that MongoDB is running.';
      try {
        const payload = await response.clone().json();
        if (payload && payload.message) dbMessage = payload.message;
      } catch (_) { /* ignore parse errors, use default message */ }
      Toast.show(dbMessage, 'error');
      throw new Error('ServiceUnavailable');
    }

    const contentType = response.headers.get('content-type') || '';
    const isJson = contentType.includes('application/json');
    const payload = isJson ? await response.json() : await response.blob();

    if (!response.ok) {
      const message = isJson && payload.message ? payload.message : 'Something went wrong';
      throw new ApiError(message, payload);
    }

    return payload;
  },

  get(path) { return this.request('GET', path); },
  post(path, body) { return this.request('POST', path, body); },
  put(path, body) { return this.request('PUT', path, body); },
  del(path) { return this.request('DELETE', path); },
  upload(path, formData) { return this.request('POST', path, formData, true); }
};

class ApiError extends Error {
  constructor(message, payload) {
    super(message);
    this.payload = payload;
  }
}

/* =========================================================
   TOAST NOTIFICATIONS
   ========================================================= */

const Toast = {
  ensureStack() {
    let stack = document.querySelector('.toast-stack');
    if (!stack) {
      stack = document.createElement('div');
      stack.className = 'toast-stack';
      document.body.appendChild(stack);
    }
    return stack;
  },

  show(message, type = 'info') {
    const icons = { success: 'check_circle', error: 'error', info: 'info' };
    const stack = this.ensureStack();
    const toast = document.createElement('div');
    toast.className = `toast-modern ${type}`;
    toast.innerHTML = `<span class="material-symbols-outlined" style="font-size:20px;">${icons[type] || 'info'}</span><span>${message}</span>`;
    stack.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(24px)';
      toast.style.transition = 'all 200ms ease';
      setTimeout(() => toast.remove(), 220);
    }, 3200);
  }
};

/* =========================================================
   AUTH GUARD — include on every protected page
   ========================================================= */

function requireAuth() {
  if (!Api.getToken()) {
    window.location.href = '/login.html';
  }
}

/* =========================================================
   THEME (dark / light mode)
   ========================================================= */

const ThemeManager = {
  init() {
    const saved = localStorage.getItem('gym_theme') || 'light';
    this.apply(saved);
  },
  apply(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('gym_theme', theme);
    document.querySelectorAll('.theme-toggle-icon').forEach(icon => {
      icon.textContent = theme === 'dark' ? 'light_mode' : 'dark_mode';
    });
  },
  toggle() {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    this.apply(current === 'dark' ? 'light' : 'dark');
  }
};

ThemeManager.init();

/* =========================================================
   RIPPLE EFFECT for .ripple elements
   ========================================================= */

document.addEventListener('click', (e) => {
  const target = e.target.closest('.ripple');
  if (!target) return;
  const rect = target.getBoundingClientRect();
  const ripple = document.createElement('span');
  const size = Math.max(rect.width, rect.height);
  ripple.className = 'ripple-effect';
  ripple.style.width = ripple.style.height = `${size}px`;
  ripple.style.left = `${e.clientX - rect.left - size / 2}px`;
  ripple.style.top = `${e.clientY - rect.top - size / 2}px`;
  target.appendChild(ripple);
  setTimeout(() => ripple.remove(), 600);
});

/* =========================================================
   CREDENTIALS MODAL — shown once when a Member/Trainer login
   account is auto-provisioned, so staff can note it down.
   Requires Bootstrap JS bundle to already be loaded on the page.
   ========================================================= */

function showCredentialsModal(roleLabel, loginId, fullMessage) {
  const passwordMatch = /Password:\s*(\S+)/.exec(fullMessage || '');
  const password = passwordMatch ? passwordMatch[1] : '(see message)';

  let modalEl = document.getElementById('credentialsModal');
  if (!modalEl) {
    modalEl = document.createElement('div');
    modalEl.className = 'modal fade';
    modalEl.id = 'credentialsModal';
    modalEl.tabIndex = -1;
    modalEl.innerHTML = `
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius:var(--radius-lg); border:none;">
          <div class="modal-header border-0">
            <h5 class="modal-title" style="font-weight:600;">
              <span class="material-symbols-outlined align-middle" style="color:var(--success);">check_circle</span>
              <span id="credModalTitle">Account Created</span>
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p class="text-secondary" style="font-size:0.86rem;">Share these sign-in details — they won't be shown again automatically.</p>
            <div class="p-3 rounded-3" style="background:rgba(37,99,235,0.06); border:1px solid var(--border);">
              <div class="mb-2">
                <div style="font-size:0.72rem; color:var(--text-secondary); text-transform:uppercase; letter-spacing:0.05em;">Login ID</div>
                <div style="font-weight:700; font-size:1.05rem;" id="credLoginId">—</div>
              </div>
              <div>
                <div style="font-size:0.72rem; color:var(--text-secondary); text-transform:uppercase; letter-spacing:0.05em;">Password</div>
                <div style="font-weight:700; font-size:1.05rem;" id="credPassword">—</div>
              </div>
            </div>
          </div>
          <div class="modal-footer border-0">
            <button type="button" class="btn btn-gradient ripple w-100" data-bs-dismiss="modal">Got it</button>
          </div>
        </div>
      </div>`;
    document.body.appendChild(modalEl);
  }

  document.getElementById('credModalTitle').textContent = `${roleLabel} Account Created`;
  document.getElementById('credLoginId').textContent = loginId;
  document.getElementById('credPassword').textContent = password;

  new bootstrap.Modal(modalEl).show();
}
