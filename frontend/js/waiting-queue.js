/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Waiting Queue Management Script (PriorityQueue DSA)
 */

document.addEventListener('DOMContentLoaded', () => {
    loadQueues();
    setupProcessButtons();
});

async function loadQueues() {
    await Promise.all([
        loadConsultationQueue(),
        loadResourceQueue()
    ]);
}

async function loadConsultationQueue() {
    try {
        const queue = await api.get('/queues/consultation');
        renderConsultationQueue(queue);
    } catch (error) {
        console.error('Failed to load consultation queue:', error);
    }
}

async function loadResourceQueue() {
    try {
        const queue = await api.get('/queues/resource');
        renderResourceQueue(queue);
    } catch (error) {
        console.error('Failed to load resource queue:', error);
    }
}

function renderConsultationQueue(items) {
    const tbody = document.getElementById('consultation-queue-body');
    const badgeCount = document.getElementById('cq-count-badge');
    if (badgeCount) badgeCount.textContent = items ? items.length : 0;
    if (!tbody) return;

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No patients currently in consultation waiting queue. All triaged patients have been assigned.</td></tr>`;
        return;
    }

    tbody.innerHTML = items.map((q, idx) => `
        <tr>
            <td><strong>#${idx + 1}</strong></td>
            <td><strong>${q.visitId}</strong></td>
            <td>${q.patientId}</td>
            <td>${getSeverityBadge(q.triageSeverity)}</td>
            <td>${q.department || 'GENERAL'}</td>
            <td><small>${formatDate(q.triageTime)}</small></td>
            <td><span class="badge ${q.status === 'WAITING' ? 'badge-occupied' : 'badge-info'}">${q.status}</span></td>
        </tr>
    `).join('');
}

function renderResourceQueue(items) {
    const tbody = document.getElementById('resource-queue-body');
    const badgeCount = document.getElementById('rq-count-badge');
    if (badgeCount) badgeCount.textContent = items ? items.length : 0;
    if (!tbody) return;

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No patients currently waiting for beds or equipment.</td></tr>`;
        return;
    }

    tbody.innerHTML = items.map((q, idx) => `
        <tr>
            <td><strong>#${idx + 1}</strong></td>
            <td><strong>${q.visitId}</strong></td>
            <td>${q.patientId}</td>
            <td>${getSeverityBadge(q.triageSeverity)}</td>
            <td><strong>${q.requiredResourceType}</strong> (Qty: ${q.quantityRequested})</td>
            <td><small>${formatDate(q.requestTime)}</small></td>
            <td><span class="badge badge-occupied">${q.status}</span></td>
        </tr>
    `).join('');
}

function setupProcessButtons() {
    document.getElementById('btn-process-cq')?.addEventListener('click', async () => {
        try {
            await api.post('/queues/consultation/process', {});
            showToast('Consultation queue re-evaluated against available doctors!', 'success');
            loadConsultationQueue();
        } catch (error) {
            console.error('Process CQ error:', error);
        }
    });

    document.getElementById('btn-process-rq')?.addEventListener('click', async () => {
        try {
            await api.post('/queues/resource/process', {});
            showToast('Resource queue re-evaluated against released beds and equipment!', 'success');
            loadResourceQueue();
        } catch (error) {
            console.error('Process RQ error:', error);
        }
    });
}
