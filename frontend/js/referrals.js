/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Referral Hospitals Module Script
 * Features:
 *  - Automated Dijkstra inter-hospital routing & transmission (zero manual re-entry)
 *  - Administrative verification and approval (POST /api/referrals/{id}/approve)
 *  - Automated fallback to Priority Waiting List (POST /api/referrals/{id}/move-to-waiting-queue)
 *  - Regional referral network overview
 */

let referralsList = [];
let hospitalsList = [];

document.addEventListener('DOMContentLoaded', () => {
    loadReferralsData();
});

async function loadReferralsData() {
    await Promise.all([
        loadReferrals(),
        loadHospitals()
    ]);
    updateKpis();
    renderReferralsTable();
    renderHospitalsGrid();
}

async function loadReferrals() {
    try {
        referralsList = await api.get('/referrals/details');
    } catch (error) {
        console.error('Failed to load referrals:', error);
        showToast('Error loading referrals', 'error');
    }
}

async function loadHospitals() {
    try {
        hospitalsList = await api.get('/referrals/hospitals');
    } catch (error) {
        console.error('Failed to load hospitals:', error);
    }
}

function updateKpis() {
    const total = referralsList.length;
    let pendingCount = 0;
    let approvedCount = 0;

    referralsList.forEach(r => {
        if (r.status === 'PENDING_APPROVAL') pendingCount++;
        else if (r.status === 'CONFIRMED' || r.status === 'APPROVED') approvedCount++;
    });

    document.getElementById('kpi-total-ref').textContent = total;
    document.getElementById('kpi-pending-ref').textContent = pendingCount;
    document.getElementById('kpi-approved-ref').textContent = approvedCount;
    document.getElementById('kpi-hospitals-count').textContent = hospitalsList.length || 4;
}

function renderReferralsTable() {
    const tbody = document.getElementById('referrals-table-body');
    if (!tbody) return;

    if (!referralsList || referralsList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state">No inter-hospital referrals recorded. Prescribe unavailable services in the Doctors Module to trigger automated Dijkstra routing.</td></tr>`;
        return;
    }

    tbody.innerHTML = referralsList.map(ref => {
        const isPending = (ref.status === 'PENDING_APPROVAL');
        const isConfirmed = (ref.status === 'CONFIRMED' || ref.status === 'APPROVED');
        const isQueued = (ref.status === 'MOVED_TO_WAITING_QUEUE');

        let statusBadge = '';
        if (isPending) {
            statusBadge = `<span class="badge badge-pending">PENDING VERIFICATION</span>`;
        } else if (isConfirmed) {
            statusBadge = `<span class="badge badge-confirmed">VERIFIED & APPROVED</span>`;
        } else if (isQueued) {
            statusBadge = `<span class="badge badge-queued">MOVED TO QUEUE</span>`;
        } else {
            statusBadge = `<span class="badge badge-info">${ref.status}</span>`;
        }

        let actionHtml = '';
        if (isPending) {
            actionHtml = `
                <div style="display:flex; flex-direction:column; gap:6px;">
                    <button class="btn btn-primary btn-sm" onclick="handleApproveReferral('${ref.referralId}')" style="background:#0fa37f; border-color:#0fa37f; font-size:11.5px; padding:5px 10px;">
                        ✓ Verify & Approve
                    </button>
                    <button class="btn btn-outline btn-sm" onclick="handleMoveToQueue('${ref.referralId}')" style="font-size:11px; padding:4px 8px; color:#d97706; border-color:#fcd34d;">
                        Move to Queue
                    </button>
                </div>
            `;
        } else if (isConfirmed) {
            actionHtml = `<span style="font-size:12px; color:#15803d; font-weight:600;">✅ Dispatched</span>`;
        } else {
            actionHtml = `<span class="cell-muted" style="font-size:11.5px;">Auto Priority Queue</span>`;
        }

        return `
            <tr>
                <td>
                    <strong>${ref.referralId}</strong>
                    <div class="cell-muted" style="font-size:11px;">${ref.createdAt ? ref.createdAt.replace('T', ' ') : ''}</div>
                </td>
                <td>
                    <div style="font-weight:700; color:var(--text-primary);">${ref.patientName || 'Patient'}</div>
                    <div class="cell-muted" style="font-size:11px;">ID: ${ref.patientId} | 📞 ${ref.patientContact || 'N/A'}</div>
                </td>
                <td>
                    ${getSeverityBadge(ref.severity || 'Normal')}
                </td>
                <td>
                    <div style="font-size:12.5px; font-weight:600; color:#b45309;">
                        ${ref.reason || 'Specialized Diagnostic / Bed Required'}
                    </div>
                    <div class="cell-muted" style="font-size:11px; margin-top:2px;">
                        Condition: ${ref.medicalCondition || 'N/A'}
                    </div>
                </td>
                <td>
                    <div style="font-weight:700; color:var(--primary); font-size:13px;">${ref.toHospitalName || ref.toHospitalId}</div>
                    <div class="cell-muted" style="font-size:11px;">📍 ${ref.toHospitalAddress || 'Regional Center'}</div>
                    <div class="cell-muted" style="font-size:11px;">📞 ${ref.toHospitalContact || 'N/A'}</div>
                </td>
                <td>
                    <div style="font-weight:700; color:#0f172a; font-size:13px;">
                        ⏱️ ${ref.travelTimeMinutes || 12} mins
                    </div>
                    <div class="cell-muted" style="font-size:11px; margin-top:2px;">
                        🛣️ ${ref.routePath || 'Optimal Corridor'}
                    </div>
                </td>
                <td>${statusBadge}</td>
                <td>${actionHtml}</td>
            </tr>
        `;
    }).join('');
}

async function handleApproveReferral(referralId) {
    if (!confirm(`Are you sure you want to verify and approve Referral ${referralId} for transfer dispatch?`)) {
        return;
    }

    try {
        const res = await api.post(`/referrals/${referralId}/approve`, {});
        showToast(`Referral ${referralId} successfully verified & approved for transfer!`, 'success');
        await loadReferralsData();
    } catch (error) {
        console.error('Failed to approve referral:', error);
        showToast(error.message || 'Approval failed', 'error');
    }
}

async function handleMoveToQueue(referralId) {
    if (!confirm(`Move patient from Referral ${referralId} to the Severity-Based Priority Waiting List?`)) {
        return;
    }

    try {
        await api.post(`/referrals/${referralId}/move-to-waiting-queue`, {});
        showToast(`Patient from referral ${referralId} moved to Priority Waiting List!`, 'info');
        await loadReferralsData();
    } catch (error) {
        console.error('Failed to move to waiting queue:', error);
        showToast(error.message || 'Operation failed', 'error');
    }
}

function renderHospitalsGrid() {
    const container = document.getElementById('hospitals-grid');
    if (!container) return;

    if (!hospitalsList || hospitalsList.length === 0) {
        container.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;">No registered network hospitals found.</div>`;
        return;
    }

    container.innerHTML = hospitalsList.map(h => {
        const isPrimary = Boolean(h.isPrimary);
        const badge = isPrimary
            ? `<span class="badge badge-info" style="font-weight:700;">PRIMARY HOSPITAL</span>`
            : `<span class="badge" style="background:#f1f5f9; color:#475569; font-weight:700;">REFERRAL CENTER</span>`;

        let capabilities = '';
        if (h.hospitalId === 'H-001') {
            capabilities = 'General Beds, ICU, Emergency, X-Ray, Ultrasound, Blood Lab';
        } else if (h.hospitalId === 'H-002') {
            capabilities = 'Advanced 3.0T MRI, Neurosurgical ICU, CT Scan, Cardiac Cath Lab';
        } else if (h.hospitalId === 'H-003') {
            capabilities = 'Nephrology, Dialysis Center, 128-Slice CT, Trauma Surgery';
        } else {
            capabilities = 'Regional Emergency, Orthopedic Trauma, General Inpatient Wards';
        }

        return `
            <div class="hospital-card" style="${isPrimary ? 'border-color:#93c5fd; background:linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);' : ''}">
                <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px;">
                    <div>
                        <div style="font-weight:800; font-size:15px; color:var(--text-primary);">${h.hospitalName}</div>
                        <div class="cell-muted" style="font-size:11.5px;">Node ID: <strong>${h.hospitalId}</strong> (${h.locationName || 'Nalam'})</div>
                    </div>
                    ${badge}
                </div>
                <div style="font-size:12.5px; color:var(--text-secondary); margin-bottom:8px;">
                    📍 ${h.address}
                </div>
                <div style="font-size:12px; margin-bottom:10px;">
                    📞 Contact: <strong>${h.contactNumber}</strong> | Total Capacity: <strong>${h.totalBeds} Beds</strong>
                </div>
                <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:6px; padding:8px 10px; font-size:11.5px;">
                    <strong style="color:var(--text-primary);">Specialized Capabilities:</strong> ${capabilities}
                </div>
            </div>
        `;
    }).join('');
}
