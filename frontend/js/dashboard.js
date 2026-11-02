/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Dashboard Module Script
 * Aggregates data from all backend endpoints to display a comprehensive
 * live command dashboard before navigating to any module.
 */

let dashStats = null;
let bedsList = [];
let admittedList = [];
let referralsList = [];

document.addEventListener('DOMContentLoaded', () => {
    startLiveClock();
    loadDashboard();
    // Auto-refresh every 60 seconds
    setInterval(loadDashboard, 60000);
});

/* ===========================================================
   LIVE CLOCK
=========================================================== */
function startLiveClock() {
    function updateClock() {
        const now = new Date();
        const timeStr = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        const dateStr = now.toLocaleDateString('en-GB', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
        const heroTime = document.getElementById('hero-time');
        if (heroTime) heroTime.textContent = `${dateStr}  |  ${timeStr}`;
    }
    updateClock();
    setInterval(updateClock, 1000);
}

/* ===========================================================
   LOAD ALL DASHBOARD DATA
=========================================================== */
async function loadDashboard() {
    try {
        await Promise.all([
            loadDashboardStats(),
            loadBedData(),
            loadAdmittedPatients(),
            loadReferrals()
        ]);

        renderKpis();
        renderAcuityBar();
        renderBedMap();
        renderAdmittedList();
        renderResourceSummary();
        renderReferralSummary();

        const lastUpdated = document.getElementById('last-updated');
        if (lastUpdated) {
            const now = new Date();
            lastUpdated.textContent = 'Last updated: ' + now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        }
    } catch (error) {
        console.error('Dashboard load error:', error);
    }
}

async function loadDashboardStats() {
    try {
        dashStats = await api.get('/dashboard/stats');
    } catch (e) {
        console.warn('Stats load failed:', e);
    }
}

async function loadBedData() {
    try {
        const all = await api.get('/resources/all-combined');
        bedsList = (all || []).filter(r => r.category === 'BED');
    } catch (e) {
        console.warn('Bed data load failed:', e);
    }
}

async function loadAdmittedPatients() {
    try {
        admittedList = await api.get('/admissions/admitted-patients');
    } catch (e) {
        admittedList = [];
    }
}

async function loadReferrals() {
    try {
        referralsList = await api.get('/referrals/details');
    } catch (e) {
        referralsList = [];
    }
}

/* ===========================================================
   RENDER KPI CARDS
=========================================================== */
function renderKpis() {
    if (!dashStats) return;
    const s = dashStats;

    setText('kpi-total-patients', s.totalRegisteredPatients);
    setText('kpi-admitted', s.admittedPatients);
    setText('kpi-discharged', s.dischargedPatients);

    setText('kpi-critical', s.criticalPatients);
    setText('kpi-serious', s.seriousPatients);
    setText('kpi-minor', s.minorPatients);

    setText('kpi-avail-beds', s.availableBeds);
    setText('kpi-occ-beds', s.occupiedBeds);
    setText('kpi-total-beds', s.totalBeds);

    setText('kpi-total-docs', s.totalDoctors);
    setText('kpi-onduty-docs', s.availableDoctors);
    setText('kpi-offduty-docs', s.offDutyOrLeave);

    setText('kpi-gen-beds', s.availableGeneralBeds);
    setText('kpi-icu-beds', s.availableIcuBeds);
    setText('kpi-emg-beds', s.availableEmergencyBeds);

    setText('kpi-total-refs', s.totalReferrals);
    setText('kpi-pending-refs', s.pendingReferrals);
    setText('kpi-done-refs', s.completedReferrals);

    setText('kpi-avail-eq', s.availableEquipment);
    setText('kpi-diag-svc', s.availableDiagnosticServices);
    setText('kpi-supply-stock', s.medicalSupplyStock);

    // Waiting queue from referrals / queue; default 0 if not available
    setText('kpi-queue-count', s.pendingResourceRequests ?? 0);
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = (value !== undefined && value !== null) ? value : '–';
}

/* ===========================================================
   ACUITY DISTRIBUTION BAR
=========================================================== */
function renderAcuityBar() {
    if (!dashStats) return;
    const total = dashStats.totalRegisteredPatients || 1;
    const c = dashStats.criticalPatients || 0;
    const s = dashStats.seriousPatients   || 0;
    const n = dashStats.minorPatients     || 0;

    const cpct = Math.round((c / total) * 100);
    const spct = Math.round((s / total) * 100);
    const npct = 100 - cpct - spct;

    const segC = document.getElementById('acuity-seg-critical');
    const segS = document.getElementById('acuity-seg-serious');
    const segN = document.getElementById('acuity-seg-normal');

    if (segC) { segC.style.flexBasis = cpct + '%'; segC.textContent = cpct > 8 ? cpct + '%' : ''; }
    if (segS) { segS.style.flexBasis = spct + '%'; segS.textContent = spct > 8 ? spct + '%' : ''; }
    if (segN) { segN.style.flexBasis = npct + '%'; segN.textContent = npct > 8 ? npct + '%' : ''; }

    setText('legend-critical', c);     setText('legend-critical-pct', cpct);
    setText('legend-serious', s);      setText('legend-serious-pct', spct);
    setText('legend-normal', n);       setText('legend-normal-pct', npct);
}

/* ===========================================================
   BED OCCUPANCY MAP
=========================================================== */
function renderBedMap() {
    const container = document.getElementById('bed-map-container');
    if (!container) return;

    if (!bedsList || bedsList.length === 0) {
        container.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">No beds found.</div>`;
        return;
    }

    const occupied = bedsList.filter(b => b.status === 'OCCUPIED').length;
    const total    = bedsList.length;
    const utilPct  = total > 0 ? Math.round((occupied / total) * 100) : 0;

    container.innerHTML = bedsList.map(b => {
        const isOcc = b.status === 'OCCUPIED';
        // Extract the human bed number from the name, e.g. "Bed GEN-01 (Ward A..." → "GEN-01"
        const bedNumMatch = b.name.match(/Bed\s+(\S+)/);
        const shortId = bedNumMatch ? bedNumMatch[1] : b.id;
        return `
            <div class="bed-cell ${isOcc ? 'occupied' : 'available'}" title="${b.name}\nStatus: ${b.status}${isOcc && b.allocatedPatientName ? '\nPatient: ' + b.allocatedPatientName : ''}">
                ${isOcc ? '🔒' : '✅'}
                <div class="bed-cell-id">${shortId}</div>
            </div>
        `;
    }).join('');

    const pctEl = document.getElementById('bed-util-pct');
    const barEl = document.getElementById('bed-util-bar');
    if (pctEl) pctEl.textContent = utilPct + '%';
    if (barEl) {
        barEl.style.width = utilPct + '%';
        barEl.style.background = utilPct > 80
            ? 'linear-gradient(90deg,#ef4444,#b91c1c)'
            : utilPct > 50
                ? 'linear-gradient(90deg,#f59e0b,#ef4444)'
                : 'linear-gradient(90deg,#22c55e,#16a34a)';
    }
}

/* ===========================================================
   ADMITTED PATIENTS LIST
=========================================================== */
function renderAdmittedList() {
    const container = document.getElementById('admitted-list-container');
    if (!container) return;

    if (!admittedList || admittedList.length === 0) {
        container.innerHTML = `<div class="empty-state" style="padding:12px 0;">No patients are currently admitted.</div>`;
        return;
    }

    container.innerHTML = admittedList.map(p => `
        <div class="admitted-item">
            <div>
                <div style="font-weight:700; font-size:13.5px; color:#0f172a;">${p.patientName}</div>
                <div style="font-size:11.5px; color:#64748b; margin-top:2px;">
                    ${p.patientId} &nbsp;|&nbsp; Bed: <strong>${p.bedNumber}</strong> (${p.bedType})
                </div>
                <div style="font-size:11px; color:#64748b;">
                    Dr. ${p.doctorName || '–'}
                </div>
            </div>
            <div style="text-align:right;">
                ${getSeverityBadge(p.severity || 'Normal')}
                <a href="discharge.html?patientId=${p.patientId}"
                   style="display:block; margin-top:5px; font-size:11px; color:#2563eb; font-weight:600; text-decoration:none;">
                    Discharge →
                </a>
            </div>
        </div>
    `).join('');
}

/* ===========================================================
   RESOURCE SUMMARY PANEL
=========================================================== */
function renderResourceSummary() {
    const container = document.getElementById('resource-summary-container');
    if (!container || !dashStats) return;
    const s = dashStats;

    const rows = [
        { label: '🛏 Total Registered Beds',           value: s.totalBeds },
        { label: '✅ Available Beds',                    value: s.availableBeds,         style: 'color:#15803d;' },
        { label: '🔒 Occupied Beds',                    value: s.occupiedBeds,           style: 'color:#dc2626;' },
        { label: '🧪 Available Diagnostic Services',    value: s.availableDiagnosticServices },
        { label: '🩺 Available Medical Equipment',      value: s.availableEquipment },
        { label: '💊 Consumable Supply Stock (units)',  value: s.medicalSupplyStock },
    ];

    container.innerHTML = rows.map(r => `
        <div class="stat-row">
            <span class="stat-row-label">${r.label}</span>
            <span class="stat-row-val" style="${r.style || ''}">${r.value ?? '–'}</span>
        </div>
    `).join('');

    // Add quick link to full inventory
    container.innerHTML += `
        <div style="margin-top:12px;">
            <a href="resources.html" class="btn btn-outline" style="width:100%; text-align:center; font-size:12.5px; padding:8px;">
                View Full Inventory & Priority Queue →
            </a>
        </div>
    `;
}

/* ===========================================================
   REFERRAL SUMMARY PANEL
=========================================================== */
function renderReferralSummary() {
    const container = document.getElementById('referral-summary-container');
    if (!container) return;

    if (!dashStats) return;
    const s = dashStats;

    // Summary metrics
    let html = `
        <div class="stat-row">
            <span class="stat-row-label">🚑 Total Referrals Issued</span>
            <span class="stat-row-val">${s.totalReferrals ?? '–'}</span>
        </div>
        <div class="stat-row">
            <span class="stat-row-label" style="color:#b45309;">⏳ Pending Verification</span>
            <span class="stat-row-val" style="color:#b45309;">${s.pendingReferrals ?? '–'}</span>
        </div>
        <div class="stat-row">
            <span class="stat-row-label" style="color:#15803d;">✅ Completed / Approved</span>
            <span class="stat-row-val" style="color:#15803d;">${s.completedReferrals ?? '–'}</span>
        </div>
        <div class="stat-row">
            <span class="stat-row-label">🏥 Network Hospitals (Dijkstra Nodes)</span>
            <span class="stat-row-val">4</span>
        </div>
    `;

    // Recent referrals
    if (referralsList && referralsList.length > 0) {
        html += `<div style="margin-top:14px; border-top:1px solid #f1f5f9; padding-top:12px;">
            <div style="font-size:12px; font-weight:700; color:#475569; text-transform:uppercase; letter-spacing:0.5px; margin-bottom:10px;">Recent Referrals</div>`;
        referralsList.slice(0, 3).forEach(ref => {
            const isPending = ref.status === 'PENDING_APPROVAL';
            html += `
                <div class="admitted-item">
                    <div>
                        <div style="font-weight:700; font-size:13px; color:#0f172a;">${ref.patientName || ref.patientId}</div>
                        <div style="font-size:11.5px; color:#64748b; margin-top:2px;">
                            → ${ref.toHospitalName || ref.toHospitalId} &nbsp;|&nbsp; ⏱️ ${ref.travelTimeMinutes} mins
                        </div>
                        <div style="font-size:11px; color:#94a3b8;">ID: ${ref.referralId}</div>
                    </div>
                    <div>
                        <span class="badge ${isPending ? '' : 'badge-available'}" style="${isPending ? 'background:#fef3c7;color:#b45309;border:1px solid #fde68a;' : ''}">
                            ${isPending ? 'PENDING' : 'APPROVED'}
                        </span>
                    </div>
                </div>
            `;
        });
        html += '</div>';
    }

    html += `
        <div style="margin-top:12px;">
            <a href="referrals.html" class="btn btn-outline" style="width:100%; text-align:center; font-size:12.5px; padding:8px;">
                Manage Referrals & Approve Transfers →
            </a>
        </div>
    `;

    container.innerHTML = html;
}
