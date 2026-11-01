/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Hospital Network Management Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadHospitalProfiles();
});

async function loadHospitalProfiles() {
    try {
        const hospitals = await api.get('/referrals/hospitals');
        renderHospitals(hospitals);
    } catch (error) {
        console.error('Failed to load hospitals:', error);
    }
}

function renderHospitals(hospitals) {
    const container = document.getElementById('hospitals-card-grid');
    if (!container) return;

    if (!hospitals || hospitals.length === 0) {
        container.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;">No hospital facilities registered in network.</div>`;
        return;
    }

    container.innerHTML = hospitals.map(h => `
        <div class="metric-card ${h.isPrimary ? 'card-blue' : 'card-green'}" style="background:#fff; padding:24px; display:flex; flex-direction:column; justify-content:space-between; border-radius:12px; box-shadow:0 1px 3px rgba(0,0,0,0.06);">
            <div>
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                    <span class="badge ${h.isPrimary ? 'badge-info' : 'badge-available'}">
                        ${h.isPrimary ? 'District Apex Center (Primary)' : 'Network Referral Hub'}
                    </span>
                    <span style="font-weight:700; color:var(--text-secondary); font-size:13px;">${h.hospitalId}</span>
                </div>
                <h3 style="margin:0 0 8px 0; font-size:18px; color:var(--text-primary);">${h.hospitalName}</h3>
                <p style="margin:0 0 16px 0; font-size:13px; color:var(--text-secondary);">
                    📍 Node: <strong>${h.locationName}</strong> | ${h.address}
                </p>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-bottom:16px; background:var(--bg-light); padding:12px; border-radius:8px;">
                    <div>
                        <div style="font-size:11px; color:var(--text-secondary); text-transform:uppercase; font-weight:600;">Total Bed Capacity</div>
                        <div style="font-size:18px; font-weight:700; color:var(--text-primary);">${h.totalBeds}</div>
                    </div>
                    <div>
                        <div style="font-size:11px; color:var(--text-secondary); text-transform:uppercase; font-weight:600;">Available Now</div>
                        <div style="font-size:18px; font-weight:700; color:#16a34a;">${h.availableBeds}</div>
                    </div>
                </div>
            </div>

            <div style="border-top:1px solid var(--border-color); padding-top:14px; display:flex; justify-content:space-between; align-items:center; font-size:12.5px;">
                <span class="cell-muted">📞 ${h.contactNumber}</span>
                <a href="referrals.html" class="btn btn-outline btn-sm">Dispatch Referral &rarr;</a>
            </div>
        </div>
    `).join('');
}
