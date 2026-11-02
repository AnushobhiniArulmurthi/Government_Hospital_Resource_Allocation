/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Available Resources Module Script
 * Features:
 *  - Unified inventory including Inpatient Beds, Medical Equipment, Supplies, and Facilities
 *  - Status tracking (Available vs Occupied) with double-allocation prevention
 *  - Severity-Based Priority Waiting Queue (Critical -> Serious -> Normal) with automatic reallocation
 */

let resourcesList = [];
let queueList = [];
let currentCategory = 'ALL';

document.addEventListener('DOMContentLoaded', () => {
    loadResourcesAndQueue();
});

async function loadResourcesAndQueue() {
    await Promise.all([
        loadCombinedResources(),
        loadWaitingQueue()
    ]);
    updateKpis();
    renderResourcesTable(filterResources());
    renderQueueTable();
}

async function loadCombinedResources() {
    try {
        resourcesList = await api.get('/resources/all-combined');
    } catch (error) {
        console.error('Failed to load combined resources:', error);
        showToast('Error loading resources inventory', 'error');
    }
}

async function loadWaitingQueue() {
    try {
        queueList = await api.get('/queues/resource-detailed');
    } catch (error) {
        console.warn('Failed to load waiting queue:', error);
        queueList = [];
    }
}

function updateKpis() {
    const total = resourcesList.length;
    let availableCount = 0;
    let occupiedCount = 0;
    let bedCount = 0;
    let eqCount = 0;
    let spCount = 0;
    let fcCount = 0;

    resourcesList.forEach(r => {
        if (r.status === 'AVAILABLE') availableCount++;
        else occupiedCount++;

        const cat = (r.category || '').toUpperCase();
        if (cat === 'BED') bedCount++;
        else if (cat === 'EQUIPMENT') eqCount++;
        else if (cat === 'SUPPLY') spCount++;
        else if (cat === 'FACILITY') fcCount++;
    });

    document.getElementById('kpi-total-res').textContent = total;
    document.getElementById('kpi-avail-res').textContent = availableCount;
    document.getElementById('kpi-occupied-res').textContent = occupiedCount;
    document.getElementById('kpi-queue-count').textContent = queueList.length;

    document.getElementById('count-all').textContent = total;
    document.getElementById('count-beds').textContent = bedCount;
    document.getElementById('count-eq').textContent = eqCount;
    document.getElementById('count-sp').textContent = spCount;
    document.getElementById('count-fc').textContent = fcCount;
}

function setResourceCategory(category) {
    currentCategory = category;

    document.querySelectorAll('.res-cat-tab').forEach(t => t.classList.remove('active'));
    if (category === 'ALL') document.getElementById('tab-all')?.classList.add('active');
    else if (category === 'BED') document.getElementById('tab-bed')?.classList.add('active');
    else if (category === 'EQUIPMENT') document.getElementById('tab-eq')?.classList.add('active');
    else if (category === 'SUPPLY') document.getElementById('tab-sp')?.classList.add('active');
    else if (category === 'FACILITY') document.getElementById('tab-fc')?.classList.add('active');

    renderResourcesTable(filterResources());
}

function filterResourcesTable() {
    renderResourcesTable(filterResources());
}

function filterResources() {
    const query = document.getElementById('resource-search-input')?.value.toLowerCase().trim() || '';
    const statusFilter = document.getElementById('status-filter')?.value || '';

    return resourcesList.filter(r => {
        // Category
        const matchesCategory = (currentCategory === 'ALL') || (r.category === currentCategory);

        // Status
        const matchesStatus = !statusFilter || (r.status === statusFilter);

        // Search
        const matchesSearch = !query ||
            r.id.toLowerCase().includes(query) ||
            r.name.toLowerCase().includes(query) ||
            (r.subType && r.subType.toLowerCase().includes(query)) ||
            (r.location && r.location.toLowerCase().includes(query)) ||
            (r.allocatedPatientName && r.allocatedPatientName.toLowerCase().includes(query));

        return matchesCategory && matchesStatus && matchesSearch;
    });
}

function renderResourcesTable(items) {
    const tbody = document.getElementById('resources-table-body');
    if (!tbody) return;

    if (!items || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state">No resources or beds found matching criteria.</td></tr>`;
        return;
    }

    tbody.innerHTML = items.map(r => {
        const isAvail = r.status === 'AVAILABLE';
        const statusBadge = isAvail
            ? `<span class="badge badge-available">AVAILABLE</span>`
            : `<span class="badge badge-occupied">OCCUPIED</span>`;

        let catBadge = '';
        if (r.category === 'BED') catBadge = `<span class="badge badge-info">BED (${r.subType || 'GENERAL'})</span>`;
        else if (r.category === 'EQUIPMENT') catBadge = `<span class="badge" style="background:#e0f2fe; color:#0369a1;">EQUIPMENT</span>`;
        else if (r.category === 'SUPPLY') catBadge = `<span class="badge" style="background:#fef3c7; color:#92400e;">CONSUMABLE</span>`;
        else catBadge = `<span class="badge" style="background:#f3e8ff; color:#6b21a8;">FACILITY</span>`;

        let patientDisplay = '<span class="cell-muted">None - Unassigned</span>';
        if (r.allocatedPatientName) {
            patientDisplay = `<strong style="color:var(--text-primary);">${r.allocatedPatientName}</strong> <span class="cell-muted" style="font-size:11px;">(${r.allocatedPatientId || ''})</span>`;
        }

        return `
            <tr>
                <td><strong>${r.id}</strong></td>
                <td>
                    <div style="font-weight:700; color:var(--text-primary); font-size:13.5px;">${r.name}</div>
                    <div class="cell-muted" style="font-size:11px;">${r.subType || ''}</div>
                </td>
                <td>${catBadge}</td>
                <td>${statusBadge}</td>
                <td><strong style="color:#15803d;">${r.availableUnits}</strong> / ${r.totalUnits}</td>
                <td><strong style="color:#b91c1c;">${r.allocatedUnits}</strong> in use</td>
                <td>${patientDisplay}</td>
                <td class="cell-muted" style="font-size:12px;">${r.location || 'H-001'}</td>
            </tr>
        `;
    }).join('');
}

function renderQueueTable() {
    const tbody = document.getElementById('waiting-queue-table-body');
    if (!tbody) return;

    if (!queueList || queueList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state" style="padding:24px; color:#1e40af;">All patient requests are currently served. Priority waiting queue is currently empty.</td></tr>`;
        return;
    }

    tbody.innerHTML = queueList.map(q => {
        let waitDisplay = '-';
        if (q.waitTimeMinutes != null) {
            waitDisplay = `${q.waitTimeMinutes} mins`;
        }

        return `
            <tr>
                <td><strong>${q.queueId}</strong></td>
                <td>
                    <div style="font-weight:700; color:var(--text-primary);">${q.patientName || 'Patient'}</div>
                    <div class="cell-muted" style="font-size:11px;">ID: ${q.patientId} | ${q.age ? q.age + ' yrs' : '-'} / ${q.gender || '-'}</div>
                </td>
                <td>${getSeverityBadge(q.severityLevel || 'Normal')}</td>
                <td><span class="badge badge-info" style="font-weight:800;">Score ${q.priorityScore}</span></td>
                <td><strong>${q.requestedResourceName}</strong></td>
                <td class="cell-muted" style="font-size:12px;">${q.enqueuedTime ? q.enqueuedTime.replace('T', ' ') : '-'}</td>
                <td><strong>${waitDisplay}</strong></td>
                <td>
                    <span class="badge ${q.queueStatus === 'ALLOCATED' ? 'badge-available' : 'badge-warning'}">
                        ${q.queueStatus}
                    </span>
                </td>
            </tr>
        `;
    }).join('');
}
