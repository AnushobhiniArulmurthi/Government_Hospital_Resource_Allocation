/**
 * Government Hospital Resource Allocation System (GHRAS)
 * System Audit & History Logs Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadResourceHistory();
    loadVisitsHistory();
    setupFilters();
});

let resourceLogs = [];
let visitLogs = [];

async function loadResourceHistory() {
    try {
        resourceLogs = await api.get('/resources/history');
        renderResourceLogs(resourceLogs);
    } catch (error) {
        console.error('Failed to load resource history:', error);
    }
}

async function loadVisitsHistory() {
    try {
        visitLogs = await api.get('/patients/visits');
        renderVisitLogs(visitLogs);
    } catch (error) {
        console.error('Failed to load visit history:', error);
    }
}

function renderResourceLogs(logs) {
    const tbody = document.getElementById('resource-history-body');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No resource transaction logs available.</td></tr>`;
        return;
    }

    tbody.innerHTML = logs.map(l => `
        <tr>
            <td><strong>#${l.historyId}</strong></td>
            <td><strong>${l.resourceId}</strong></td>
            <td>${l.visitId || 'Hospital Stock'}</td>
            <td>
                <span class="badge ${l.actionType === 'ALLOCATED' ? 'badge-occupied' : (l.actionType === 'RELEASED' ? 'badge-available' : 'badge-warning')}">
                    ${l.actionType}
                </span>
            </td>
            <td>${l.quantity}</td>
            <td><small>${formatDate(l.timestamp)}</small></td>
            <td>${l.remarks || 'Standard Transaction'}</td>
        </tr>
    `).join('');
}

function renderVisitLogs(visits) {
    const tbody = document.getElementById('visits-history-body');
    if (!tbody) return;

    if (!visits || visits.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No patient visits recorded.</td></tr>`;
        return;
    }

    tbody.innerHTML = visits.map(v => `
        <tr>
            <td><strong>${v.visitId}</strong></td>
            <td>${v.patientId}</td>
            <td>${getSeverityBadge(v.triageSeverity)}</td>
            <td>${v.triageCategory}</td>
            <td><small>${formatDate(v.registrationTime)}</small></td>
            <td>${v.symptoms || 'OPD Checkup'}</td>
            <td>${getStatusBadge(v.status)}</td>
        </tr>
    `).join('');
}

function setupFilters() {
    document.getElementById('history-search-input')?.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const filtered = resourceLogs.filter(l => 
            (l.resourceId && l.resourceId.toLowerCase().includes(query)) ||
            (l.visitId && l.visitId.toLowerCase().includes(query)) ||
            (l.actionType && l.actionType.toLowerCase().includes(query))
        );
        renderResourceLogs(filtered);
    });
}
