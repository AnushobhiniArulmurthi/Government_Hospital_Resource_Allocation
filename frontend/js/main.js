/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Main Frontend Script: Navigation, Modals, Shared Utilities
 */

document.addEventListener('DOMContentLoaded', () => {
    highlightActiveNav();
});

function highlightActiveNav() {
    const currentPath = window.location.pathname;
    const pageName = currentPath.substring(currentPath.lastIndexOf('/') + 1) || 'dashboard.html';
    
    document.querySelectorAll('.nav-item').forEach(link => {
        const href = link.getAttribute('href');
        if (href === pageName || (pageName === 'index.html' && href === 'dashboard.html')) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
    }
}

// Close modal when clicking outside of modal-card
window.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
    }
});

function formatDateTime(isoString) {
    if (!isoString) return '-';
    const date = new Date(isoString);
    return date.toLocaleString('en-IN', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

function getSeverityBadge(severity) {
    if (!severity) return '';
    const sev = severity.toLowerCase();
    if (sev === 'critical') {
        return `<span class="badge badge-critical" style="background:#fee2e2; color:#b91c1c; font-weight:700; border:1px solid #fca5a5;">Critical</span>`;
    } else if (sev === 'serious') {
        return `<span class="badge badge-serious" style="background:#fef3c7; color:#b45309; font-weight:700; border:1px solid #fcd34d;">Serious</span>`;
    } else if (sev === 'normal' || sev === 'minor') {
        return `<span class="badge badge-minor" style="background:#dcfce7; color:#15803d; font-weight:700; border:1px solid #86efac;">Normal</span>`;
    } else if (sev === 'pending_triage') {
        return `<span class="badge" style="background:#f1f5f9; color:#475569; font-weight:600;">Registered</span>`;
    }
    return `<span class="badge badge-info">${severity}</span>`;
}

function getStatusBadge(status) {
    if (!status) return '';
    const s = status.toUpperCase();
    if (s === 'AVAILABLE' || s === 'COMPLETED' || s === 'DISCHARGED') {
        return `<span class="badge badge-available">${status}</span>`;
    } else if (s === 'OCCUPIED' || s === 'CANCELLED' || s === 'OFF_DUTY' || s === 'DEPLETED' || s === 'DEACTIVATED' || s === 'NO_DOCTOR_AVAILABLE') {
        return `<span class="badge badge-occupied">${status.replace(/_/g, ' ')}</span>`;
    } else if (s === 'WAITING' || s === 'PENDING' || s === 'SCHEDULED' || s === 'LOW_STOCK' || s === 'PENDING_TRIAGE' || s === 'AWAITING_ALTERNATIVE_CONFIRMATION' || s === 'WAITING_FOR_RESOURCE') {
        return `<span class="badge badge-waiting">${status.replace(/_/g, ' ')}</span>`;
    } else if (s === 'IN_PROGRESS' || s === 'IN_CONSULTATION' || s === 'ADMITTED' || s === 'REFERRAL_RECOMMENDED' || s === 'REFERRED') {
        return `<span class="badge badge-info">${status.replace(/_/g, ' ')}</span>`;
    }
    return `<span class="badge badge-info">${status}</span>`;
}
