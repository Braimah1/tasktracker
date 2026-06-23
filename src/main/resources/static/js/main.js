// ===== TASKTRACKER — MAIN JS =====

document.addEventListener('DOMContentLoaded', () => {
  initSidebar();
  initAlertDismiss();
  initTaskActions();
  initFilterForm();
  animateStats();
});

// ===== SIDEBAR MOBILE TOGGLE =====
function initSidebar() {
  const toggle = document.getElementById('mobileToggle');
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('sidebarOverlay');

  if (!toggle) return;

  toggle.addEventListener('click', () => {
    sidebar.classList.toggle('open');
    overlay.classList.toggle('open');
  });

  overlay?.addEventListener('click', () => {
    sidebar.classList.remove('open');
    overlay.classList.remove('open');
  });
}

// ===== AUTO-DISMISS ALERTS =====
function initAlertDismiss() {
  const alerts = document.querySelectorAll('.alert[data-auto-dismiss]');
  alerts.forEach(alert => {
    setTimeout(() => {
      alert.style.transition = 'opacity .4s, max-height .4s';
      alert.style.opacity = '0';
      alert.style.maxHeight = '0';
      alert.style.overflow = 'hidden';
      alert.style.marginBottom = '0';
      setTimeout(() => alert.remove(), 420);
    }, 4000);
  });
}

// ===== CONFIRM DELETE =====
function initTaskActions() {
  document.querySelectorAll('[data-confirm]').forEach(btn => {
    btn.addEventListener('click', e => {
      const msg = btn.getAttribute('data-confirm') || 'Are you sure?';
      if (!confirm(msg)) e.preventDefault();
    });
  });
}

// ===== FILTER FORM LIVE SUBMIT =====
function initFilterForm() {
  const filterSelects = document.querySelectorAll('.filter-auto-submit');
  filterSelects.forEach(el => {
    el.addEventListener('change', () => {
      el.closest('form').submit();
    });
  });
}

// ===== STAT NUMBER ANIMATION =====
function animateStats() {
  const statValues = document.querySelectorAll('.stat-value[data-value]');
  statValues.forEach(el => {
    const target = parseFloat(el.getAttribute('data-value'));
    const isFloat = target % 1 !== 0;
    let start = 0;
    const duration = 700;
    const step = 16;
    const increment = target / (duration / step);
    const timer = setInterval(() => {
      start = Math.min(start + increment, target);
      el.textContent = isFloat ? start.toFixed(1) + '%' : Math.floor(start);
      if (start >= target) {
        el.textContent = isFloat ? target.toFixed(1) + '%' : target;
        clearInterval(timer);
      }
    }, step);
  });
}

// ===== PASSWORD STRENGTH =====
function checkPasswordStrength(input, indicator) {
  const val = input.value;
  const bars = indicator?.querySelectorAll('.strength-bar');
  if (!bars) return;

  let strength = 0;
  if (val.length >= 8) strength++;
  if (/[A-Z]/.test(val)) strength++;
  if (/[0-9]/.test(val)) strength++;
  if (/[^A-Za-z0-9]/.test(val)) strength++;

  bars.forEach((bar, i) => {
    bar.className = 'strength-bar';
    if (i < strength) {
      bar.classList.add(strength <= 1 ? 'weak' : strength <= 2 ? 'fair' : strength <= 3 ? 'good' : 'strong');
    }
  });
}

// ===== PASSWORD CONFIRM MATCH =====
function initPasswordConfirm() {
  const pwd = document.getElementById('password');
  const confirm = document.getElementById('confirmPassword');
  const hint = document.getElementById('confirmHint');
  const strengthIndicator = document.getElementById('strengthIndicator');

  pwd?.addEventListener('input', () => {
    checkPasswordStrength(pwd, strengthIndicator);
    checkMatch();
  });

  confirm?.addEventListener('input', checkMatch);

  function checkMatch() {
    if (!hint || !confirm?.value) return;
    if (pwd?.value === confirm.value) {
      hint.textContent = '✓ Passwords match';
      hint.className = 'form-hint';
      hint.style.color = '#10B981';
    } else {
      hint.textContent = 'Passwords do not match';
      hint.className = 'form-error';
      hint.style.color = '';
    }
  }
}

document.addEventListener('DOMContentLoaded', initPasswordConfirm);

// ===== CATEGORY MODAL =====
function openCategoryModal() {
  document.getElementById('categoryModal')?.classList.add('open');
}

function closeCategoryModal() {
  document.getElementById('categoryModal')?.classList.remove('open');
}

document.addEventListener('keydown', e => {
  if (e.key === 'Escape') closeCategoryModal();
});
