/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Referral Management Script with Dijkstra Shortest Route Calculation
 */

document.addEventListener('DOMContentLoaded', () => {
    loadReferrals();
    loadConnectedHospitals();
    setupReferralForm();
});

async function loadReferrals() {
    try {
        const referrals = await api.get('/referrals');
        renderReferralsTable(referrals);
    } catch (error) {
        console.error('Failed to load referrals:', error);
    }
}

async function loadConnectedHospitals() {
    try {
        const hospitals = await api.get('/referrals/hospitals');
        const container = document.getElementById('hospitals-network-container');
        if (container) {
            container.innerHTML = hospitals.map(h => `
                <div class="metric-card ${h.isPrimary ? 'card-blue' : 'card-green'}" style="background:#fff; padding:18px;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span class="badge ${h.isPrimary ? 'badge-info' : 'badge-available'}">
                            ${h.isPrimary ? 'Primary Headquarters' : 'Referral Center'}
                        </span>
                        <span class="cell-muted" style="font-size:12px;">${h.hospitalId}</span>
                    </div>
                    <div style="font-size:16px; font-weight:700; color:var(--text-primary); margin-bottom:4px;">
                        ${h.hospitalName}
                    </div>
                    <div class="cell-muted" style="margin-bottom:8px;">
                        📍 Location: <strong>${h.locationName}</strong> | 🛏 Beds: ${h.totalBeds}
                    </div>
                    <div style="font-size:12px; color:var(--text-secondary);">
                        📞 ${h.contactNumber} | ${h.address}
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Failed to load hospitals:', error);
    }
}

function renderReferralsTable(referrals) {
    const tbody = document.getElementById('referrals-table-body');
    if (!tbody) return;

    if (!referrals || referrals.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No patient referrals on record.</td></tr>`;
        return;
    }

    tbody.innerHTML = referrals.map(r => `
        <tr>
            <td><strong>${r.referralId}</strong></td>
            <td>${r.visitId} (${r.patientId})</td>
            <td><strong>${r.toHospitalId}</strong></td>
            <td><span class="badge badge-available">⏱ ${r.travelTimeMinutes} mins</span></td>
            <td><small>${r.routePath}</small></td>
            <td>${r.reason}</td>
            <td>${getStatusBadge(r.status)}</td>
        </tr>
    `).join('');
}

function setupReferralForm() {
    const form = document.getElementById('referral-request-form');
    if (!form) return;

    // Dijkstra preview button
    document.getElementById('btn-dijkstra-calc')?.addEventListener('click', async () => {
        const data = {
            visitId: document.getElementById('ref-visit-id').value,
            fromHospitalId: 'H-001',
            requiredBedType: document.getElementById('ref-bed-type').value,
            requiredSpecialization: document.getElementById('ref-specialization').value,
            requiredResources: []
        };

        try {
            const route = await api.post('/referrals/find-fastest', data);
            const previewEl = document.getElementById('dijkstra-route-preview');
            previewEl.style.display = 'block';
            previewEl.innerHTML = `
                <div style="background:#e0f2fe; border:1px solid #bae6fd; border-radius:8px; padding:14px;">
                    <div style="font-weight:700; color:#0369a1; margin-bottom:4px;">
                        🚀 Dijkstra's Algorithm Calculation Result:
                    </div>
                    <div style="font-size:14px; margin-bottom:6px;">
                        <strong>Fastest Eligible Hospital:</strong> ${route.destination}
                    </div>
                    <div style="font-size:13px; margin-bottom:4px;">
                        <strong>Optimal Travel Time:</strong> <span class="badge badge-available">${route.totalTravelTimeMinutes} minutes</span> (${route.totalDistanceKm} km)
                    </div>
                    <div style="font-size:12.5px; color:#334155;">
                        <strong>Shortest Road Route:</strong> ${route.formattedPath}
                    </div>
                </div>
            `;
        } catch (error) {
            console.error('Dijkstra calculation error:', error);
        }
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
            visitId: document.getElementById('ref-visit-id').value,
            fromHospitalId: 'H-001',
            requiredBedType: document.getElementById('ref-bed-type').value,
            requiredSpecialization: document.getElementById('ref-specialization').value,
            reason: document.getElementById('ref-reason').value
        };

        try {
            const referral = await api.post('/referrals/process', data);
            showToast(`Referral confirmed! Referral ID: ${referral.referralId}`, 'success');
            closeModal('new-referral-modal');
            form.reset();
            document.getElementById('dijkstra-route-preview').style.display = 'none';
            loadReferrals();
        } catch (error) {
            console.error('Referral confirmation error:', error);
        }
    });
}
