/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Diagnostic Services, Orders, Scheduling & Doctor Review Workflow Script
 * Standardized Statuses: PRESCRIBED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED.
 */

let ordersList = [];
let activeStatusFilter = 'ALL';

document.addEventListener('DOMContentLoaded', () => {
    loadDiagnosticServices();
    loadDiagnosticCentres();
    loadDiagnosticOrders();
    setupOrderForm();
    setupResultsForm();
    setupDoctorReviewForm();
});

async function loadDiagnosticServices() {
    try {
        const services = await api.get('/diagnostics/services');
        const tbody = document.getElementById('services-table-body');
        if (tbody) {
            tbody.innerHTML = services.map(s => `
                <tr>
                    <td><strong>${s.serviceCode}</strong></td>
                    <td><strong>${s.serviceName}</strong></td>
                    <td><span class="badge badge-info">${s.testType}</span></td>
                    <td>₹${s.cost.toFixed(2)}</td>
                    <td>${s.turnaroundHours} hrs</td>
                    <td>${s.isAvailable ? '<span class="badge badge-available">Operational</span>' : '<span class="badge badge-occupied">Offline</span>'}</td>
                </tr>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load diagnostic services:', error);
    }
}

async function loadDiagnosticCentres() {
    try {
        const centres = await api.get('/diagnostics/centres');
        const container = document.getElementById('centres-grid-container');
        if (container) {
            container.innerHTML = centres.map(c => `
                <div class="metric-card card-blue" style="background:#fff; padding:18px;">
                    <div style="font-size:16px; font-weight:700; color:var(--text-primary); margin-bottom:4px;">
                        ${c.centreName}
                    </div>
                    <div class="cell-muted" style="margin-bottom:10px;">
                        📍 ${c.locationName} | 📞 ${c.contactNumber}
                    </div>
                    <div style="font-size:12px; margin-bottom:12px;">
                        <strong>Services:</strong> ${c.availableServices}
                    </div>
                    <div>
                        <span class="badge badge-available">Active Scanning Hub</span>
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load diagnostic centres:', error);
    }
}

async function loadDiagnosticOrders() {
    try {
        ordersList = await api.get('/diagnostics/orders');
        renderOrdersTable(ordersList);
    } catch (error) {
        console.error('Failed to load orders:', error);
    }
}

function renderOrdersTable(orders) {
    const tbody = document.getElementById('orders-table-body');
    if (!tbody) return;

    if (!orders || orders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No diagnostic orders found.</td></tr>`;
        return;
    }

    tbody.innerHTML = orders.map(o => {
        let actionButtons = '';

        if (o.status === 'PRESCRIBED') {
            actionButtons += `
                <button class="btn btn-outline btn-sm" onclick="scheduleOrder('${o.orderId}')">
                    Schedule Test
                </button>
                <button class="btn btn-outline btn-sm" onclick="cancelOrder('${o.orderId}')" style="color:#dc2626; border-color:#fca5a5;">
                    Cancel
                </button>
            `;
        } else if (o.status === 'SCHEDULED') {
            actionButtons += `
                <button class="btn btn-outline btn-sm" onclick="startOrder('${o.orderId}')">
                    Start Test
                </button>
                <button class="btn btn-primary btn-sm" onclick="openRecordResultsModal('${o.orderId}', '${o.testType}')">
                    Record Findings
                </button>
            `;
        } else if (o.status === 'IN_PROGRESS') {
            actionButtons += `
                <button class="btn btn-primary btn-sm" onclick="openRecordResultsModal('${o.orderId}', '${o.testType}')">
                    Complete & Enter Results
                </button>
            `;
        } else if (o.status === 'COMPLETED') {
            actionButtons += `
                <button class="btn btn-secondary btn-sm" onclick="openDoctorReviewModal('${o.orderId}', '${escapeHtml(o.resultsSummary || '')}', '${o.doctorId || ''}', '${escapeHtml(o.doctorReviewNotes || '')}')">
                    ${o.doctorReviewNotes ? '✏️ Update Review' : '🩺 Doctor Review'}
                </button>
            `;
        }

        const reviewDisplay = o.doctorReviewNotes
            ? `<div style="font-size:11.5px; color:#0284c7; margin-top:3px;"><strong>Doctor Note:</strong> ${o.doctorReviewNotes}</div>`
            : '';

        const findingsDisplay = o.resultsSummary
            ? `<div>${o.resultsSummary}</div>${reviewDisplay}`
            : `<span class="cell-muted">Awaiting Procedure</span>`;

        return `
            <tr>
                <td><strong>${o.orderId}</strong></td>
                <td>
                    <div style="font-weight:600;">Visit: ${o.visitId}</div>
                    <div class="cell-muted" style="font-size:11px;">Patient: ${o.patientId}</div>
                </td>
                <td>
                    <span class="badge badge-info">${o.testType}</span>
                    <div class="cell-muted" style="font-size:11px; margin-top:2px;">Ordered by: ${o.doctorId}</div>
                </td>
                <td>${getSeverityBadge(o.priority)}</td>
                <td>${getStatusBadge(o.status)}</td>
                <td>${findingsDisplay}</td>
                <td class="cell-actions">${actionButtons}</td>
            </tr>
        `;
    }).join('');
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

function setupOrderForm() {
    const form = document.getElementById('new-diag-order-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
            visitId: document.getElementById('diag-visit-id').value.trim(),
            patientId: document.getElementById('diag-patient-id').value.trim(),
            doctorId: document.getElementById('diag-doctor-id').value.trim(),
            testType: document.getElementById('diag-test-type').value,
            priority: document.getElementById('diag-priority').value,
            diagnosticCentreId: document.getElementById('diag-centre-id')?.value || null
        };

        try {
            await api.post('/diagnostics/orders', data);
            showToast('Diagnostic order placed successfully! Status: PRESCRIBED', 'success');
            closeModal('new-order-modal');
            form.reset();
            loadDiagnosticOrders();
        } catch (error) {
            console.error('Diagnostic order error:', error);
            showToast(error.message || 'Failed to place diagnostic order', 'error');
        }
    });
}

async function scheduleOrder(orderId) {
    try {
        await api.post(`/diagnostics/orders/${orderId}/schedule`, {});
        showToast(`Diagnostic order ${orderId} scheduled successfully. Status: SCHEDULED`, 'success');
        loadDiagnosticOrders();
    } catch (error) {
        console.error('Schedule error:', error);
        showToast(error.message || 'Failed to schedule order', 'error');
    }
}

async function startOrder(orderId) {
    try {
        await api.post(`/diagnostics/orders/${orderId}/start`, {});
        showToast(`Diagnostic order ${orderId} marked IN_PROGRESS`, 'info');
        loadDiagnosticOrders();
    } catch (error) {
        console.error('Start order error:', error);
        showToast(error.message || 'Failed to start order', 'error');
    }
}

function openRecordResultsModal(orderId, testType) {
    document.getElementById('results-order-id').value = orderId;
    document.getElementById('results-display-order-id').textContent = orderId;
    document.getElementById('results-display-test-type').textContent = testType;
    document.getElementById('results-summary-text').value = '';
    openModal('record-results-modal');
}

function setupResultsForm() {
    const form = document.getElementById('record-results-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const orderId = document.getElementById('results-order-id').value;
        const findings = document.getElementById('results-summary-text').value.trim();

        if (!findings) {
            showToast('Please enter laboratory or imaging findings.', 'warning');
            return;
        }

        try {
            await api.post(`/diagnostics/orders/${orderId}/complete`, { resultsSummary: findings });
            showToast(`Diagnostic test completed! Report is now available for doctor review. Status: COMPLETED`, 'success');
            closeModal('record-results-modal');
            form.reset();
            loadDiagnosticOrders();
        } catch (error) {
            console.error('Complete test error:', error);
            showToast(error.message || 'Failed to complete test', 'error');
        }
    });
}

function openDoctorReviewModal(orderId, findings, doctorId, existingNotes) {
    document.getElementById('review-order-id').value = orderId;
    document.getElementById('review-display-order-id').textContent = orderId;
    document.getElementById('review-display-findings').textContent = findings || 'No findings recorded';
    document.getElementById('review-doctor-id').value = doctorId || 'D-001';
    document.getElementById('doctor-review-notes-text').value = existingNotes || '';
    openModal('doctor-review-modal');
}

function setupDoctorReviewForm() {
    const form = document.getElementById('doctor-review-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const orderId = document.getElementById('review-order-id').value;
        const doctorId = document.getElementById('review-doctor-id').value.trim();
        const notes = document.getElementById('doctor-review-notes-text').value.trim();

        if (!notes) {
            showToast('Please enter doctor observations and review notes.', 'warning');
            return;
        }

        try {
            await api.post(`/diagnostics/orders/${orderId}/review`, {
                doctorId: doctorId,
                doctorReviewNotes: notes
            });
            showToast(`Doctor review recorded successfully on completed diagnostic report.`, 'success');
            closeModal('doctor-review-modal');
            form.reset();
            loadDiagnosticOrders();
        } catch (error) {
            console.error('Doctor review error:', error);
            showToast(error.message || 'Failed to record doctor review', 'error');
        }
    });
}

async function cancelOrder(orderId) {
    const reason = prompt(`Please enter cancellation reason for order ${orderId}:`, 'Patient requested cancellation');
    if (reason === null) return;

    try {
        await api.post(`/diagnostics/orders/${orderId}/cancel`, { reason: reason });
        showToast(`Diagnostic order ${orderId} has been cancelled. Status: CANCELLED`, 'info');
        loadDiagnosticOrders();
    } catch (error) {
        console.error('Cancel order error:', error);
        showToast(error.message || 'Failed to cancel order', 'error');
    }
}
