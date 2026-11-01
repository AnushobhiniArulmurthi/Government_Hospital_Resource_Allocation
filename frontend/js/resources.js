/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Medical Resources Management Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadResources();
    loadResourceHistory();
    setupCategoryFilter();
});

let resourcesList = [];

async function loadResources() {
    try {
        resourcesList = await api.get('/resources');
        renderResourcesTable(resourcesList);
    } catch (error) {
        console.error('Failed to load resources:', error);
    }
}

async function loadResourceHistory() {
    try {
        const history = await api.get('/resources/history');
        renderResourceHistory(history);
    } catch (error) {
        console.error('Failed to load resource history:', error);
    }
}

function renderResourcesTable(resources) {
    const tbody = document.getElementById('resources-table-body');
    if (!tbody) return;

    if (!resources || resources.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No medical resources found.</td></tr>`;
        return;
    }

    tbody.innerHTML = resources.map(r => {
        const typeBadge = `<span class="badge badge-info">${r.category}</span>`;
        const reusableBadge = r.isReusable 
            ? `<span style="color:var(--primary); font-weight:600; font-size:12px;">Reusable</span>`
            : `<span style="color:var(--status-yellow); font-weight:600; font-size:12px;">Consumable</span>`;

        return `
            <tr>
                <td><strong>${r.resourceId}</strong></td>
                <td><strong>${r.resourceName}</strong></td>
                <td>${typeBadge}</td>
                <td><strong>${r.availableQuantity}</strong> / ${r.totalQuantity}</td>
                <td>${r.allocatedQuantity}</td>
                <td>${reusableBadge}</td>
                <td>
                    <span class="badge" style="background:#f1f5f9; color:#475569; font-size:11px;">
                        ${r.isReusable ? '🔄 Auto-Allocated & Released' : '📉 Deducted on Usage'}
                    </span>
                </td>
            </tr>
        `;
    }).join('');
}

function renderResourceHistory(history) {
    const tbody = document.getElementById('resource-history-body');
    if (!tbody) return;

    if (!history || history.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No resource transactions recorded.</td></tr>`;
        return;
    }

    tbody.innerHTML = history.slice(0, 15).map(h => {
        const actionBadge = h.actionType === 'ALLOCATED'
            ? `<span class="badge badge-occupied">ALLOCATED</span>`
            : h.actionType === 'RELEASED'
            ? `<span class="badge badge-available">RELEASED</span>`
            : `<span class="badge badge-waiting">${h.actionType}</span>`;

        return `
            <tr>
                <td><strong>${h.resourceName}</strong></td>
                <td>${actionBadge}</td>
                <td>${h.quantity} units</td>
                <td>${h.patientId || '-'}</td>
                <td class="cell-muted">${formatDateTime(h.timestamp)}</td>
                <td>${h.remarks || '-'}</td>
            </tr>
        `;
    }).join('');
}

function openResourceAllocateModal(resourceId, resourceName, availableQty) {
    document.getElementById('alloc-res-id').value = resourceId;
    document.getElementById('alloc-res-title').textContent = `Allocate ${resourceName} (Available: ${availableQty})`;
    openModal('allocate-resource-modal');
}

async function handleResourceAllocateSubmit(e) {
    e.preventDefault();
    const resourceId = document.getElementById('alloc-res-id').value;
    const visitId = document.getElementById('alloc-res-visit-id').value;
    const patientId = document.getElementById('alloc-res-patient-id').value;
    const quantity = parseInt(document.getElementById('alloc-res-qty').value, 10);

    try {
        await api.post('/resources/allocate', {
            visitId,
            resourceNames: [document.getElementById('alloc-res-title').textContent.split(' ')[1]],
            quantity
        });
        showToast('Resource allocated successfully', 'success');
        closeModal('allocate-resource-modal');
        loadResources();
        loadResourceHistory();
    } catch (error) {
        console.error('Allocation error:', error);
    }
}

function openResourceReleaseModal(resourceId, resourceName, allocatedQty) {
    const qty = prompt(`Enter quantity to release/consume for ${resourceName} (Allocated: ${allocatedQty}):`, "1");
    if (!qty) return;
    api.post('/resources/release', {
        resourceId,
        quantity: parseInt(qty, 10),
        patientId: 'HOSPITAL',
        visitId: 'SYSTEM'
    }).then(() => {
        showToast('Resource released successfully', 'success');
        loadResources();
        loadResourceHistory();
    });
}

function setupCategoryFilter() {
    const filter = document.getElementById('resource-cat-filter');
    if (filter) {
        filter.addEventListener('change', (e) => {
            const val = e.target.value;
            if (!val) {
                renderResourcesTable(resourcesList);
            } else {
                renderResourcesTable(resourcesList.filter(r => r.category === val));
            }
        });
    }
}
