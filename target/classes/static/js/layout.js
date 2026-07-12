/* =========================================================
   SHARED APP LAYOUT — sidebar + topbar
   Call renderLayout('members') from each page after DOM ready.
   ========================================================= */

const ADMIN_NAV_ITEMS = [
  { section: 'Overview', items: [
    { key: 'dashboard', icon: 'space_dashboard', label: 'Dashboard', href: '/dashboard.html' }
  ]},
  { section: 'Management', items: [
    { key: 'members', icon: 'group', label: 'Members', href: '/members.html' },
    { key: 'plans', icon: 'card_membership', label: 'Membership Plans', href: '/plans.html' },
    { key: 'attendance', icon: 'qr_code_scanner', label: 'Attendance', href: '/attendance.html' },
    { key: 'payments', icon: 'payments', label: 'Payments', href: '/payments.html' },
    { key: 'trainers', icon: 'sports_gymnastics', label: 'Trainers', href: '/trainers.html' },
    { key: 'equipment', icon: 'fitness_center', label: 'Equipment', href: '/equipment.html' }
  ]},
  { section: 'Insights', items: [
    { key: 'reports', icon: 'monitoring', label: 'Reports', href: '/reports.html' },
    { key: 'notifications', icon: 'notifications', label: 'Notifications', href: '/notifications.html' }
  ]},
  { section: 'Account', items: [
    { key: 'profile', icon: 'account_circle', label: 'Profile', href: '/profile.html' },
    { key: 'settings', icon: 'settings', label: 'Settings', href: '/settings.html' }
  ]}
];

const MEMBER_NAV_ITEMS = [
  { section: 'Overview', items: [
    { key: 'dashboard', icon: 'space_dashboard', label: 'My Dashboard', href: '/member-dashboard.html' }
  ]},
  { section: 'Account', items: [
    { key: 'notifications', icon: 'notifications', label: 'Notifications', href: '/notifications.html' },
    { key: 'profile', icon: 'account_circle', label: 'Profile', href: '/profile.html' },
    { key: 'settings', icon: 'settings', label: 'Settings', href: '/settings.html' }
  ]}
];

const TRAINER_NAV_ITEMS = [
  { section: 'Overview', items: [
    { key: 'dashboard', icon: 'space_dashboard', label: 'My Dashboard', href: '/trainer-dashboard.html' }
  ]},
  { section: 'Work', items: [
    { key: 'attendance', icon: 'qr_code_scanner', label: 'Attendance', href: '/attendance.html' }
  ]},
  { section: 'Account', items: [
    { key: 'notifications', icon: 'notifications', label: 'Notifications', href: '/notifications.html' },
    { key: 'profile', icon: 'account_circle', label: 'Profile', href: '/profile.html' },
    { key: 'settings', icon: 'settings', label: 'Settings', href: '/settings.html' }
  ]}
];

function navItemsForRole(role) {
  if (role === 'MEMBER') return MEMBER_NAV_ITEMS;
  if (role === 'TRAINER') return TRAINER_NAV_ITEMS;
  return ADMIN_NAV_ITEMS;
}

function renderLayout(activeKey) {
  requireAuth();
  const user = Api.getUser();
  const NAV_ITEMS = navItemsForRole(user?.role);

  const navHtml = NAV_ITEMS.map(section => `
    <div class="nav-section-label">${section.section}</div>
    ${section.items.map(item => `
      <a class="nav-item ripple ${item.key === activeKey ? 'active' : ''}" href="${item.href}">
        <span class="material-symbols-outlined">${item.icon}</span>
        <span class="nav-label">${item.label}</span>
      </a>
    `).join('')}
  `).join('');

  document.getElementById('sidebarRoot').innerHTML = `
    <aside class="sidebar" id="sidebar">
      <div class="sidebar-brand">
        <div class="logo-badge" style="overflow:hidden; padding:0;"><img src="/favicon.png" alt="FitCore" style="width:100%; height:100%; object-fit:cover;"></div>
        <span class="brand-text">FitCore</span>
      </div>
      <nav class="sidebar-nav">${navHtml}</nav>
      <div class="pt-3 border-top border-secondary-subtle mt-2" style="border-color: rgba(255,255,255,0.08) !important;">
        <a class="nav-item ripple" id="logoutBtn" style="color:#F87171;">
          <span class="material-symbols-outlined">logout</span>
          <span class="nav-label sidebar-footer-text">Logout</span>
        </a>
      </div>
    </aside>
  `;

  const initials = (user?.name || 'U').split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();

  document.getElementById('topbarRoot').innerHTML = `
    <div class="topbar">
      <div class="d-flex align-items-center gap-3">
        <button class="btn btn-ghost btn-sm ripple" id="sidebarToggle"><span class="material-symbols-outlined" style="font-size:20px;">menu</span></button>
        <h5 class="mb-0 d-none d-md-block" style="font-weight:600;">${document.title.split('·')[0].trim()}</h5>
      </div>
      <div class="d-flex align-items-center gap-3">
        <button class="btn btn-ghost btn-sm ripple" onclick="ThemeManager.toggle()">
          <span class="material-symbols-outlined theme-toggle-icon" style="font-size:20px;">dark_mode</span>
        </button>
        <a href="/notifications.html" class="btn btn-ghost btn-sm ripple position-relative">
          <span class="material-symbols-outlined" style="font-size:20px;">notifications</span>
        </a>
        <div class="d-flex align-items-center gap-2">
          <div class="avatar">${initials}</div>
          <div class="d-none d-md-block">
            <div style="font-size:0.85rem; font-weight:600;">${user?.name || 'User'}</div>
            <div style="font-size:0.72rem; color:var(--text-secondary);">${user?.role || ''}</div>
          </div>
        </div>
      </div>
    </div>
  `;

  ThemeManager.init();

  document.getElementById('logoutBtn').addEventListener('click', () => Api.logout());

  document.getElementById('sidebarToggle').addEventListener('click', () => {
    const sidebar = document.getElementById('sidebar');
    const main = document.getElementById('mainContent');
    if (window.innerWidth < 992) {
      sidebar.classList.toggle('mobile-open');
    } else {
      sidebar.classList.toggle('collapsed');
      main.classList.toggle('expanded');
    }
  });
}
