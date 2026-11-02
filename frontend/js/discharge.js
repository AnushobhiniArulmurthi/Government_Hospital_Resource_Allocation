/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Discharge Summary Module Script
 * Features:
 *  - Enforce: Only currently admitted patients can be discharged.
 *  - Search admitted patients by Patient ID.
 *  - Prominently display Patient ID, Patient Name, and Discharge Date & Time.
 *  - Automatically release all allocated beds and resources.
 *  - Immediately trigger re-evaluation of priority waiting queue with zero manual intervention.
 */

let admittedPatients = [];
let dischargeHistory = [];

document.addEventListener('DOMContentLoaded', () => {
    loadAdmittedPatientsAndHistory();

    // Check if query param patientId is present
    const urlParams = new URLSearchParams(window.location.search);
    const prePatientId = urlParams.get('patientId');
    if (prePatientId) {
        document.getElementById('admitted-search-input').value = prePatientId;
        searchAdmittedPatient();
    }
});

async function loadAdmittedPatientsAndHistory() {
    await Promise.all([
        loadAdmittedPatients(),
        loadDischargeHistory()
    ]);
}

async function loadAdmittedPatients() {
    try {
        admittedPatients = await api.get('/admissions/admitted-patients');
        const badge = document.getElementById('count-admitted-badge');
        if (badge) badge.textContent = `${admittedPatients.length} Currently Admitted`;
        renderAdmittedTable();
    } catch (error) {
        console.error('Failed to load admitted patients:', error);
        showToast('Error loading admitted patients', 'error');
    }
}

async function loadDischargeHistory() {
    try {
        dischargeHistory = await api.get('/admissions/discharge-history');
        renderHistoryTable();
    } catch (error) {
        console.error('Failed to load discharge history:', error);
    }
}

function renderAdmittedTable() {
    const tbody = document.getElementById('admitted-table-body');
    if (!tbody) return;

    if (!admittedPatients || admittedPatients.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No patients are currently admitted in hospital beds.</td></tr>`;
        return;
    }

    tbody.innerHTML = admittedPatients.map(p => `
        <tr>
            <td><strong>${p.patientId}</strong></td>
            <td>
                <div style="font-weight:700; color:var(--text-primary); font-size:14px;">${p.patientName}</div>
                <div class="cell-muted" style="font-size:11px;">${p.patientAge ? p.patientAge + ' yrs' : ''} / ${p.patientGender || ''}</div>
            </td>
            <td>
                <span class="badge badge-occupied" style="font-weight:700;">Bed: ${p.bedNumber}</span>
                <div class="cell-muted" style="font-size:11px; margin-top:2px;">${p.floorWard || ''} (${p.bedType})</div>
            </td>
            <td>
                <div style="font-weight:600; color:var(--text-primary);">${p.doctorName || 'Attending Physician'}</div>
                <div class="cell-muted" style="font-size:11px;">ID: ${p.doctorId || '-'}</div>
            </td>
            <td>
                <div style="font-size:12.5px; font-weight:600; color:#1e293b;">${p.medicalCondition || 'Under care'}</div>
                <div style="margin-top:2px;">${getSeverityBadge(p.severity || 'Normal')}</div>
            </td>
            <td class="cell-muted" style="font-size:12px;">
                ${p.admissionDate ? p.admissionDate.replace('T', ' ') : '-'}
            </td>
            <td>
                <button type="button" class="btn btn-primary btn-sm" onclick="selectAdmittedPatientById('${p.patientId}')" style="background:#0fa37f; border-color:#0fa37f; padding:5px 12px; font-size:12px;">
                    Select for Discharge
                </button>
            </td>
        </tr>
    `).join('');
}

function renderHistoryTable() {
    const tbody = document.getElementById('discharge-history-body');
    if (!tbody) return;

    if (!dischargeHistory || dischargeHistory.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No discharge records available.</td></tr>`;
        return;
    }

    tbody.innerHTML = dischargeHistory.map(d => `
        <tr>
            <td><strong>${d.dischargeId || 'DC-' + d.visitId}</strong></td>
            <td><strong>${d.patientId || d.visitId}</strong></td>
            <td>
                <div style="font-weight:700; color:var(--text-primary);">${d.patientName || 'Discharged Citizen'}</div>
            </td>
            <td>
                <strong style="color:#047857;">${d.dischargeTime ? d.dischargeTime.replace('T', ' ') : '-'}</strong>
            </td>
            <td>
                <span class="badge badge-available">Freed Bed: ${d.bedNumber || 'Assigned Bed'}</span>
            </td>
            <td>
                <div style="font-size:12.5px; color:#1e293b; max-width:240px;">
                    ${d.dischargeSummary || 'Clinical recovery achieved.'}
                </div>
            </td>
            <td>
                <div class="cell-muted" style="font-size:11.5px; max-width:220px;">
                    ${d.followUpAdvice || 'None'}
                </div>
            </td>
        </tr>
    `).join('');
}

async function searchAdmittedPatient() {
    const query = document.getElementById('admitted-search-input')?.value.trim();
    const errorEl = document.getElementById('search-error-msg');
    const card = document.getElementById('discharge-card');

    if (!query) {
        showToast('Please type a Patient ID to search', 'warning');
        return;
    }

    try {
        const results = await api.get(`/admissions/search-admitted?query=${encodeURIComponent(query)}`);
        if (!results || results.length === 0) {
            // Patient is not currently admitted
            if (errorEl) {
                errorEl.innerHTML = `
                    ⚠️ <strong>Patient "${query}" is NOT currently admitted to any hospital bed.</strong><br>
                    <span style="font-weight:normal; font-size:12px;">Only currently admitted inpatients can be discharged. Outpatient records cannot be discharged.</span>
                `;
                errorEl.style.display = 'block';
            }
            if (card) card.style.display = 'none';
            return;
        }

        // Found admitted patient
        if (errorEl) errorEl.style.display = 'none';
        selectAdmittedPatient(results[0]);
    } catch (error) {
        console.error('Search failed:', error);
        showToast('Search failed: ' + error.message, 'error');
    }
}

function selectAdmittedPatientById(patientId) {
    const found = admittedPatients.find(p => p.patientId === patientId);
    if (found) {
        selectAdmittedPatient(found);
    } else {
        searchAdmittedPatient();
    }
}

function selectAdmittedPatient(p) {
    const card = document.getElementById('discharge-card');
    const errorEl = document.getElementById('search-error-msg');
    if (errorEl) errorEl.style.display = 'none';

    // Set search box value
    const searchInput = document.getElementById('admitted-search-input');
    if (searchInput) searchInput.value = `${p.patientId} - ${p.patientName}`;

    // Fill Display Fields: Patient ID, Patient Name, Discharge Date & Time
    document.getElementById('dc-display-patient-id').textContent = p.patientId;
    document.getElementById('dc-display-patient-name').textContent = p.patientName;

    // Current formatted discharge timestamp
    const now = new Date();
    const formattedTimestamp = now.toLocaleDateString('en-GB') + ' ' + now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    document.getElementById('dc-display-datetime').textContent = formattedTimestamp;

    document.getElementById('dc-card-bed-badge').textContent = `Bed: ${p.bedNumber} (${p.bedType})`;
    document.getElementById('dc-display-admission').textContent = `Adm ID: ${p.admissionId} | Doctor: ${p.doctorName || 'N/A'} | Ward: ${p.floorWard || 'Inpatient'}`;
    document.getElementById('dc-bed-num-label').textContent = p.bedNumber;

    // Hidden inputs
    document.getElementById('dc-selected-patient-id').value = p.patientId;
    document.getElementById('dc-selected-visit-id').value = p.visitId;

    // Pre-fill form fields
    const summaryInput = document.getElementById('dc-clinical-summary');
    if (summaryInput) {
        summaryInput.value = `Patient stabilized; vitals within normal limits; afebrile for 48 hours. Satisfactory recovery from ${p.medicalCondition || 'presenting symptoms'}. Discharged in stable condition.`;
    }

    const adviceInput = document.getElementById('dc-followup-advice');
    if (adviceInput) {
        adviceInput.value = 'Continue prescribed oral medications for 5 days. Ensure adequate hydration. Review in Medicine OPD in 1 week or report immediately if symptoms recur.';
    }

    // Show card and scroll
    card.style.display = 'block';
    card.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function closeDischargeCard() {
    const card = document.getElementById('discharge-card');
    if (card) card.style.display = 'none';
}

async function handleDischargeSubmit(event) {
    event.preventDefault();

    const patientId = document.getElementById('dc-selected-patient-id').value;
    const visitId = document.getElementById('dc-selected-visit-id').value;
    const summary = document.getElementById('dc-clinical-summary').value.trim();
    const advice = document.getElementById('dc-followup-advice').value.trim();

    if (!summary) {
        showToast('Clinical discharge summary is mandatory', 'warning');
        return;
    }

    const submitBtn = document.getElementById('btn-submit-discharge');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `⏳ Releasing Bed & Auto-Allocating Queue...`;
    }

    const payload = {
        patientId: patientId,
        visitId: visitId,
        dischargeSummary: summary,
        followUpAdvice: advice
    };

    try {
        const response = await api.post('/admissions/discharge', payload);
        closeDischargeCard();

        // Render result banner
        const banner = document.getElementById('discharge-result-banner');
        if (banner) {
            banner.className = 'discharge-result-banner success';
            banner.innerHTML = `
                <div style="display:flex; align-items:flex-start; gap:14px;">
                    <span style="font-size:28px;">🎉</span>
                    <div style="flex:1;">
                        <div style="font-size:16px; font-weight:800; color:#065f46; margin-bottom:4px;">
                            INPATIENT DISCHARGE COMPLETE & RESOURCES SUCCESSFULLY RELEASED
                        </div>
                        <p style="font-size:13.5px; margin:0 0 10px 0;">
                            Patient <strong>${patientId}</strong> successfully discharged. All assigned beds and equipment have been released back to <code>AVAILABLE</code> status.
                        </p>
                        <div style="background:#ffffffcc; padding:12px; border-radius:8px; border:1px solid #a7f3d0; font-size:13px;">
                            ⚡ <strong>Synchronized Queue Re-evaluation:</strong> The system automatically processed the Severity-Based Priority Waiting Queue. Newly freed resources were immediately re-evaluated and allocated to eligible waiting patients without manual intervention.
                        </div>
                        <div style="margin-top:12px; display:flex; gap:10px;">
                            <a href="resources.html" class="btn btn-outline" style="padding:5px 12px; font-size:12px;">View Live Inventory in Available Resources</a>
                        </div>
                    </div>
                </div>
            `;
            banner.style.display = 'block';
            banner.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }

        showToast('Patient discharged successfully!', 'success');
        document.getElementById('admitted-search-input').value = '';
        await loadAdmittedPatientsAndHistory();
    } catch (error) {
        console.error('Discharge failed:', error);
        showToast(error.message || 'Failed to discharge patient', 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = `🚀 Complete Discharge & Release Resources`;
        }
    }
}
