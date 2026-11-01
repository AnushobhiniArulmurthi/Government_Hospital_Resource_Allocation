/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Patient Management, Segmentation, Clinical Triage & Unified Patient 360 Script
 */

let patientsList = [];
let visitsList = [];
let doctorsList = [];
let activePatientFilter = 'ALL'; // ALL, NEW, REGULAR
let currentRole = 'RECEPTION'; // RECEPTION, DOCTOR, ADMIN

document.addEventListener('DOMContentLoaded', () => {
    // Set max date for DOB to today
    const dobInput = document.getElementById('pat-dob');
    if (dobInput) {
        dobInput.max = new Date().toISOString().split('T')[0];
    }

    loadActiveDoctors();
    loadPatients();
    loadVisits();
    setupPatientForm();
    setupTriageForm();
    setupSearch();
});

function handleRoleChange() {
    const roleSelect = document.getElementById('user-role-select');
    if (roleSelect) {
        currentRole = roleSelect.value;
        renderVisitsTable(visitsList);
    }
}

async function loadActiveDoctors() {
    try {
        doctorsList = await api.get('/doctors');
        populatePreferredDoctorSelect();
    } catch (error) {
        console.error('Failed to load active doctors for selection:', error);
    }
}

function populatePreferredDoctorSelect(preSelectedDocId = '') {
    const select = document.getElementById('pat-pref-doc');
    if (!select) return;

    select.innerHTML = `<option value="">-- No specific preferred doctor (Auto-assign on triage) --</option>`;
    doctorsList.forEach(doc => {
        const selected = doc.doctorId === preSelectedDocId ? 'selected' : '';
        const dutyText = doc.dutyStatus === 'ON_DUTY' ? ' (On Duty)' : ' (Off Duty)';
        select.innerHTML += `<option value="${doc.doctorId}" ${selected}>Dr. ${doc.doctorName} - ${doc.specialization}${dutyText}</option>`;
    });
}

async function loadPatients() {
    try {
        patientsList = await api.get('/patients');
        updatePatientCounts();
        renderPatientsTable(filterPatientsByType(patientsList));
    } catch (error) {
        console.error('Failed to load patients:', error);
    }
}

async function loadVisits() {
    try {
        visitsList = await api.get('/patients/visits');
        renderVisitsTable(visitsList);
    } catch (error) {
        console.error('Failed to load visits:', error);
    }
}

function updatePatientCounts() {
    const all = patientsList.length;
    const newCount = patientsList.filter(p => (p.patientType || 'NEW') === 'NEW').length;
    const regularCount = patientsList.filter(p => p.patientType === 'REGULAR').length;

    const elAll = document.getElementById('count-all');
    const elNew = document.getElementById('count-new');
    const elReg = document.getElementById('count-regular');

    if (elAll) elAll.textContent = all;
    if (elNew) elNew.textContent = newCount;
    if (elReg) elReg.textContent = regularCount;
}

function switchPatientFilter(type) {
    activePatientFilter = type;
    document.querySelectorAll('.patient-type-tab').forEach(btn => btn.classList.remove('active'));
    if (type === 'ALL') document.getElementById('tab-all-patients')?.classList.add('active');
    if (type === 'NEW') document.getElementById('tab-new-patients')?.classList.add('active');
    if (type === 'REGULAR') document.getElementById('tab-regular-patients')?.classList.add('active');

    renderPatientsTable(filterPatientsByType(patientsList));
}

function filterPatientsByType(list) {
    if (activePatientFilter === 'NEW') {
        return list.filter(p => (p.patientType || 'NEW') === 'NEW');
    }
    if (activePatientFilter === 'REGULAR') {
        return list.filter(p => p.patientType === 'REGULAR');
    }
    return list;
}

function calculatePatientAgeFromDob() {
    const dobInput = document.getElementById('pat-dob');
    const ageInput = document.getElementById('pat-age');
    if (!dobInput || !ageInput || !dobInput.value) {
        if (ageInput) ageInput.value = '';
        return;
    }

    const dob = new Date(dobInput.value);
    const today = new Date();

    if (dob > today) {
        showFieldError('pat-dob', 'Date of birth cannot be in the future.');
        ageInput.value = '';
        return;
    } else {
        hideFieldError('pat-dob');
    }

    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
        age--;
    }
    ageInput.value = Math.max(0, age);
}

function renderPatientsTable(patients) {
    const tbody = document.getElementById('patients-table-body');
    if (!tbody) return;

    if (!patients || patients.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No ${activePatientFilter !== 'ALL' ? activePatientFilter.toLowerCase() : ''} patients found.</td></tr>`;
        return;
    }

    tbody.innerHTML = patients.map(p => {
        const typeBadge = (p.patientType === 'REGULAR')
            ? `<span class="p360-badge badge-regular">REGULAR</span>`
            : `<span class="p360-badge badge-new">NEW</span>`;

        const prefDocName = p.preferredDoctorName
            ? `Dr. ${p.preferredDoctorName}`
            : (p.preferredDoctorId ? p.preferredDoctorId : '<span class="cell-muted">None specified</span>');

        const dobDisplay = p.dateOfBirth ? p.dateOfBirth : 'N/A';
        const ageDisplay = p.age != null ? `${p.age} yrs` : '-';

        return `
            <tr>
                <td><strong>${p.patientId}</strong></td>
                <td>
                    <div style="font-weight:700; color:var(--text-primary);">${p.fullName}</div>
                    <div class="cell-muted" style="font-size:11px;">${p.address}</div>
                </td>
                <td>${typeBadge}</td>
                <td>
                    <div>${ageDisplay} / ${p.gender}</div>
                    <div class="cell-muted" style="font-size:11px;">DOB: ${dobDisplay}</div>
                </td>
                <td>
                    <div>📞 ${p.contactNumber}</div>
                    <div class="cell-muted" style="font-size:11px;">${p.email || '-'}</div>
                </td>
                <td>
                    <div style="font-size:12.5px; font-weight:600; color:#0369a1;">${prefDocName}</div>
                </td>
                <td class="cell-actions">
                    <button class="btn btn-outline btn-sm" onclick="openPatient360('${p.patientId}')" title="Unified Medical Profile">
                        🔍 360° Record
                    </button>
                    <button class="btn btn-primary btn-sm" onclick="openNewVisitForPatient('${p.patientId}', '${p.fullName}', '${p.preferredDoctorId || ''}')">
                        + New Visit
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function renderVisitsTable(visits) {
    const tbody = document.getElementById('visits-table-body');
    if (!tbody) return;

    if (!visits || visits.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="empty-state">No clinical visits recorded.</td></tr>`;
        return;
    }

    tbody.innerHTML = visits.map(v => {
        let actionButtons = '';

        if (v.visitStatus === 'PENDING_TRIAGE') {
            actionButtons += `
                <button class="btn btn-primary btn-sm" onclick="openTriageModal('${v.visitId}', '${v.patientId}', '${v.preferredDoctorId || ''}')" style="background:#0284c7;">
                    Assess Triage
                </button>
            `;
        } else if (v.visitStatus === 'AWAITING_ALTERNATIVE_CONFIRMATION') {
            actionButtons += `
                <button class="btn btn-primary btn-sm" onclick="openAlternativeDoctorModal('${v.visitId}')" style="background:#d97706;">
                    Alternative Doctor Offered
                </button>
            `;
        }

        actionButtons += `
            <button class="btn btn-outline btn-sm" onclick="openPatient360('${v.patientId}')">
                360° Record
            </button>
        `;

        const prefDoc = v.preferredDoctorId || '<span class="cell-muted">None</span>';
        const assignedDoc = v.assignedDoctorId
            ? `<strong>${v.assignedDoctorId}</strong>`
            : (v.visitStatus === 'NO_DOCTOR_AVAILABLE'
                ? `<span style="color:#dc2626; font-weight:600;">No Doctor Available</span>`
                : `<span class="cell-muted">Unassigned</span>`);

        return `
            <tr>
                <td><strong>${v.visitId}</strong></td>
                <td>
                    <a href="javascript:void(0)" onclick="openPatient360('${v.patientId}')" style="font-weight:600; text-decoration:underline;">
                        ${v.patientId}
                    </a>
                </td>
                <td>${getSeverityBadge(v.severityLevel)}</td>
                <td><span class="badge badge-info">${v.patientCategory}</span></td>
                <td>${getStatusBadge(v.visitStatus)}</td>
                <td>${prefDoc}</td>
                <td>${assignedDoc}</td>
                <td class="cell-muted">${formatDateTime(v.arrivalTime)}</td>
                <td class="cell-actions">${actionButtons}</td>
            </tr>
        `;
    }).join('');
}

function showFieldError(fieldId, message) {
    const err = document.getElementById(`err-${fieldId}`);
    if (err) {
        err.textContent = message;
        err.style.display = 'block';
    }
}

function hideFieldError(fieldId) {
    const err = document.getElementById(`err-${fieldId}`);
    if (err) {
        err.style.display = 'none';
    }
}

function validatePatientForm() {
    let isValid = true;

    // Full Name
    const name = document.getElementById('pat-name').value.trim();
    if (!name || !/^[A-Za-z\s\.]+$/.test(name)) {
        showFieldError('pat-name', 'Full name is mandatory and must contain only valid letters, spaces, and dots.');
        isValid = false;
    } else {
        hideFieldError('pat-name');
    }

    // DOB
    const dob = document.getElementById('pat-dob').value;
    if (!dob) {
        showFieldError('pat-dob', 'Date of birth is mandatory.');
        isValid = false;
    } else {
        const dobDate = new Date(dob);
        const today = new Date();
        if (dobDate > today) {
            showFieldError('pat-dob', 'Date of birth cannot be in the future.');
            isValid = false;
        } else {
            hideFieldError('pat-dob');
        }
    }

    // Gender
    const gender = document.getElementById('pat-gender').value;
    if (!gender) {
        showFieldError('pat-gender', 'Gender selection is mandatory.');
        isValid = false;
    } else {
        hideFieldError('pat-gender');
    }

    // Phone
    const phone = document.getElementById('pat-phone').value.trim();
    if (!phone || !/^[6-9]\d{9}$/.test(phone)) {
        showFieldError('pat-phone', 'Please enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.');
        isValid = false;
    } else {
        hideFieldError('pat-phone');
    }

    // Email
    const email = document.getElementById('pat-email').value.trim();
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showFieldError('pat-email', 'Please provide a valid email address.');
        isValid = false;
    } else {
        hideFieldError('pat-email');
    }

    // Address
    const address = document.getElementById('pat-address').value.trim();
    if (!address) {
        showFieldError('pat-address', 'Residential address is mandatory.');
        isValid = false;
    } else {
        hideFieldError('pat-address');
    }

    return isValid;
}

function setupPatientForm() {
    const form = document.getElementById('patient-reg-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!validatePatientForm()) {
            return;
        }

        const data = {
            patientId: document.getElementById('existing-patient-id')?.value || null,
            fullName: document.getElementById('pat-name').value.trim(),
            dateOfBirth: document.getElementById('pat-dob').value,
            gender: document.getElementById('pat-gender').value,
            contactNumber: document.getElementById('pat-phone').value.trim(),
            email: document.getElementById('pat-email').value.trim() || null,
            address: document.getElementById('pat-address').value.trim(),
            preferredDoctorId: document.getElementById('pat-pref-doc')?.value || null,
            patientCategory: document.getElementById('pat-category').value,
            preferredSpecialization: document.getElementById('pat-specialization')?.value || 'General Medicine',
            notes: document.getElementById('pat-notes')?.value || 'Intake registered at reception'
        };

        try {
            const visit = await api.post('/patients/register', data);
            showToast(`Registered successfully! Patient ID: ${visit.patientId}, Visit ID: ${visit.visitId}. Status: PENDING_TRIAGE.`, 'success');
            closeModal('patient-reg-modal');
            form.reset();
            loadPatients();
            loadVisits();
        } catch (error) {
            console.error('Patient registration error:', error);
            showToast(error.message || 'Registration failed. Check submitted values.', 'error');
        }
    });
}

function openNewPatientModal() {
    document.getElementById('patient-modal-title').textContent = 'Register New Patient Citizen';
    document.getElementById('existing-patient-id').value = '';
    document.getElementById('pat-name').readOnly = false;
    document.getElementById('pat-dob').readOnly = false;
    document.getElementById('patient-reg-form').reset();
    populatePreferredDoctorSelect();
    hideAllErrors();
    openModal('patient-reg-modal');
}

function openNewVisitForPatient(patientId, fullName, preferredDocId) {
    document.getElementById('patient-modal-title').textContent = `New Clinical Visit for ${fullName} (${patientId})`;
    document.getElementById('existing-patient-id').value = patientId;
    document.getElementById('pat-name').value = fullName;
    document.getElementById('pat-name').readOnly = true;

    // Fetch existing patient details to pre-populate DOB, gender, phone
    const pat = patientsList.find(p => p.patientId === patientId);
    if (pat) {
        if (pat.dateOfBirth) {
            document.getElementById('pat-dob').value = pat.dateOfBirth;
            calculatePatientAgeFromDob();
        }
        if (pat.gender) document.getElementById('pat-gender').value = pat.gender;
        if (pat.contactNumber) document.getElementById('pat-phone').value = pat.contactNumber;
        if (pat.email) document.getElementById('pat-email').value = pat.email;
        if (pat.address) document.getElementById('pat-address').value = pat.address;
    }

    populatePreferredDoctorSelect(preferredDocId || (pat ? pat.preferredDoctorId : ''));
    hideAllErrors();
    openModal('patient-reg-modal');
}

function hideAllErrors() {
    ['pat-name', 'pat-dob', 'pat-gender', 'pat-phone', 'pat-email', 'pat-address'].forEach(hideFieldError);
}

// --- Clinical Triage Workflow ---

function openTriageModal(visitId, patientId, prefDocId) {
    document.getElementById('triage-visit-id').value = visitId;
    document.getElementById('triage-pat-id').textContent = patientId;
    
    const pat = patientsList.find(p => p.patientId === patientId);
    document.getElementById('triage-pat-name').textContent = pat ? pat.fullName : patientId;

    let docName = 'None specified';
    if (prefDocId) {
        const doc = doctorsList.find(d => d.doctorId === prefDocId);
        docName = doc ? `Dr. ${doc.doctorName} (${doc.specialization})` : prefDocId;
    }
    document.getElementById('triage-pref-doc').textContent = docName;

    document.getElementById('triage-severity').value = 'Serious';
    document.getElementById('triage-assessor').value = currentRole === 'DOCTOR' ? 'Duty Medical Officer' : 'Staff Nurse Preethi';
    document.getElementById('triage-notes').value = '';

    openModal('triage-modal');
}

function setupTriageForm() {
    const form = document.getElementById('triage-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const visitId = document.getElementById('triage-visit-id').value;
        const triageData = {
            severityLevel: document.getElementById('triage-severity').value,
            assessedBy: document.getElementById('triage-assessor').value.trim(),
            triageNotes: document.getElementById('triage-notes').value.trim()
        };

        try {
            const decision = await api.post(`/patients/visits/${visitId}/triage`, triageData);
            closeModal('triage-modal');
            loadVisits();

            if (decision.status === 'ASSIGNED') {
                showToast(`Triage Complete! ${decision.message}`, 'success');
            } else if (decision.status === 'ALTERNATIVE_OFFERED') {
                showAlternativeDoctorDialog(visitId, decision);
            } else if (decision.status === 'NO_DOCTOR_AVAILABLE') {
                showToast(`Visit marked No Doctor Available. Patient enrolled in waiting queue.`, 'warning');
            }
        } catch (error) {
            console.error('Triage assessment failed:', error);
            showToast(error.message || 'Triage submission failed.', 'error');
        }
    });
}

function showAlternativeDoctorDialog(visitId, decision) {
    document.getElementById('alt-visit-id').value = visitId;
    document.getElementById('alt-doctor-message').innerHTML = `
        <strong>Preferred Doctor Status:</strong><br>
        ${decision.message}
    `;

    const container = document.getElementById('alt-doctors-list');
    if (!decision.alternativeDoctors || decision.alternativeDoctors.length === 0) {
        container.innerHTML = `<div class="empty-state" style="padding:10px;">No other doctors with matching specialization are currently on duty.</div>`;
    } else {
        container.innerHTML = decision.alternativeDoctors.map((doc, idx) => `
            <label style="display:flex; align-items:center; gap:10px; margin-bottom:8px; cursor:pointer; background:#fff; padding:8px 12px; border-radius:6px; border:1px solid var(--border-color);">
                <input type="radio" name="selectedAltDoctor" value="${doc.doctorId}" ${idx === 0 ? 'checked' : ''}>
                <div>
                    <strong>Dr. ${doc.doctorName}</strong> (${doc.specialization})
                    <div class="cell-muted" style="font-size:11px;">${doc.qualification} | Caseload: ${doc.currentInpatientCount}/${doc.maxInpatientCapacity}</div>
                </div>
            </label>
        `).join('');
    }

    openModal('alt-doctor-modal');
}

async function openAlternativeDoctorModal(visitId) {
    // Re-check alternatives for visit
    const visit = visitsList.find(v => v.visitId === visitId);
    if (!visit) return;
    openModal('alt-doctor-modal');
}

async function respondAlternativeDoctor(accepted) {
    const visitId = document.getElementById('alt-visit-id').value;
    let selectedDoctorId = null;

    if (accepted) {
        const checkedRadio = document.querySelector('input[name="selectedAltDoctor"]:checked');
        if (checkedRadio) {
            selectedDoctorId = checkedRadio.value;
        } else {
            showToast('Please select an available doctor to proceed.', 'warning');
            return;
        }
    }

    try {
        const decision = await api.post(`/patients/visits/${visitId}/doctor-response`, {
            accepted: accepted,
            selectedDoctorId: selectedDoctorId
        });

        closeModal('alt-doctor-modal');
        showToast(decision.message, accepted ? 'success' : 'info');
        loadVisits();
        loadPatients();
    } catch (error) {
        console.error('Failed to submit doctor response:', error);
        showToast(error.message || 'Failed to submit response', 'error');
    }
}

// --- Unified Patient 360 Profile ---

async function openPatient360(patientId) {
    openModal('patient-360-modal');
    const body = document.getElementById('p360-body');
    body.innerHTML = `<div class="empty-state">Loading complete Patient 360° record from database...</div>`;

    try {
        const profile = await api.get(`/patients/${patientId}/profile`);
        renderPatient360(profile);
    } catch (error) {
        console.error('Failed to load patient 360 profile:', error);
        body.innerHTML = `<div class="empty-state" style="color:#dc2626;">Failed to load patient 360 profile: ${error.message}</div>`;
    }
}

function renderPatient360(p360) {
    const pat = p360.patient;
    document.getElementById('p360-header-name').textContent = `${pat.fullName} (${pat.patientId})`;
    document.getElementById('p360-header-id').textContent = `Citizen Profile | ${pat.patientType || 'NEW'} PATIENT | Registered ${pat.createdAt ? formatDateTime(pat.createdAt) : ''}`;

    const prefDocDisplay = p360.preferredDoctor
        ? `Dr. ${p360.preferredDoctor.doctorName} (${p360.preferredDoctor.specialization})`
        : (pat.preferredDoctorId ? pat.preferredDoctorId : 'None specified');

    const visitsHtml = (!p360.visits || p360.visits.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No visit history on record.</div>`
        : `
            <table class="data-table" style="font-size:12px;">
                <thead>
                    <tr>
                        <th>Visit ID</th>
                        <th>Severity</th>
                        <th>Category</th>
                        <th>Status</th>
                        <th>Doctor</th>
                        <th>Bed</th>
                        <th>Arrival</th>
                        <th>Triage Notes</th>
                    </tr>
                </thead>
                <tbody>
                    ${p360.visits.map(v => `
                        <tr>
                            <td><strong>${v.visitId}</strong></td>
                            <td>${getSeverityBadge(v.severityLevel)}</td>
                            <td>${v.patientCategory}</td>
                            <td>${getStatusBadge(v.visitStatus)}</td>
                            <td>${v.assignedDoctorId || '-'}</td>
                            <td>${v.assignedBedId || '-'}</td>
                            <td>${formatDateTime(v.arrivalTime)}</td>
                            <td>${v.triageNotes || '-'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;

    const consultationsHtml = (!p360.consultations || p360.consultations.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No doctor consultation notes recorded yet.</div>`
        : p360.consultations.map(c => `
            <div style="background:#fff; border:1px solid var(--border-color); border-radius:6px; padding:12px; margin-bottom:10px;">
                <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                    <strong>Consultation ${c.consultationId} (Visit: ${c.visitId})</strong>
                    <span class="badge badge-available">${c.status}</span>
                </div>
                <div style="font-size:12.5px;"><strong>Doctor:</strong> ${c.doctorId}</div>
                <div style="font-size:12.5px;"><strong>Symptoms:</strong> ${c.symptoms || 'None'}</div>
                <div style="font-size:12.5px;"><strong>Doctor Observations:</strong> ${c.doctorObservations || 'None'}</div>
                <div style="font-size:12.5px; color:var(--primary); font-weight:600;"><strong>Prescription:</strong> ${c.prescription || 'None'}</div>
            </div>
        `).join('');

    const treatmentPlansHtml = (!p360.treatmentPlans || p360.treatmentPlans.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No treatment plans formulated yet.</div>`
        : p360.treatmentPlans.map(tp => `
            <div style="background:#fff; border:1px solid var(--border-color); border-radius:6px; padding:12px; margin-bottom:10px;">
                <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                    <strong>Treatment Plan ${tp.treatmentPlanId} (Visit: ${tp.visitId})</strong>
                    <span class="badge badge-info">${tp.treatmentCategory}</span>
                </div>
                <div style="font-size:12.5px;"><strong>Required Bed:</strong> ${tp.requiredBedType}</div>
                <div style="font-size:12.5px;"><strong>Specialization:</strong> ${tp.requiredDoctorSpecialization || 'General'}</div>
                <div style="font-size:12.5px;"><strong>Required Resources:</strong> ${tp.requiredResources || 'None'}</div>
                <div style="font-size:12.5px; margin-top:4px;"><strong>Doctor Clinical Notes:</strong> ${tp.doctorNotes || '-'}</div>
            </div>
        `).join('');

    const diagnosticHtml = (!p360.diagnosticOrders || p360.diagnosticOrders.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No diagnostic test orders on record.</div>`
        : `
            <table class="data-table" style="font-size:12px;">
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Test Type</th>
                        <th>Status</th>
                        <th>Lab Findings</th>
                        <th>Doctor Review Notes</th>
                    </tr>
                </thead>
                <tbody>
                    ${p360.diagnosticOrders.map(d => `
                        <tr>
                            <td><strong>${d.orderId}</strong></td>
                            <td>${d.testType}</td>
                            <td>${getStatusBadge(d.status)}</td>
                            <td>${d.resultsSummary || '<span class="cell-muted">Pending</span>'}</td>
                            <td>${d.doctorReviewNotes ? `<span style="color:#0284c7; font-weight:600;">${d.doctorReviewNotes}</span>` : '<span class="cell-muted">Not yet reviewed</span>'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;

    const resourcesHtml = (!p360.resourceAllocations || p360.resourceAllocations.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No allocated resources.</div>`
        : `
            <table class="data-table" style="font-size:12px;">
                <thead>
                    <tr>
                        <th>Allocation ID</th>
                        <th>Resource Name</th>
                        <th>Category</th>
                        <th>Qty</th>
                        <th>Status</th>
                        <th>Allocated At</th>
                    </tr>
                </thead>
                <tbody>
                    ${p360.resourceAllocations.map(r => `
                        <tr>
                            <td><strong>${r.allocationId}</strong></td>
                            <td>${r.resourceName}</td>
                            <td>${r.category}</td>
                            <td>${r.quantity}</td>
                            <td>${getStatusBadge(r.status)}</td>
                            <td>${formatDateTime(r.allocatedAt)}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;

    const admissionsHtml = (!p360.admissions || p360.admissions.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No hospital inpatient admissions.</div>`
        : p360.admissions.map(a => `
            <div style="background:#fff; border:1px solid var(--border-color); border-radius:6px; padding:10px; margin-bottom:8px; font-size:12.5px;">
                <strong>Admission ${a.admissionId} (Visit: ${a.visitId})</strong> - Status: <span class="badge badge-info">${a.status}</span><br>
                Bed Assigned: ${a.bedId || 'None'} | Admitted: ${formatDateTime(a.admissionDate)}<br>
                Notes: ${a.notes || '-'}
            </div>
        `).join('');

    const dischargesHtml = (!p360.discharges || p360.discharges.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No discharge summaries on record.</div>`
        : p360.discharges.map(dh => `
            <div style="background:#fff; border:1px solid var(--border-color); border-radius:6px; padding:10px; margin-bottom:8px; font-size:12.5px;">
                <strong>Discharge ${dh.dischargeId}</strong> (${formatDateTime(dh.dischargeDate)})<br>
                <strong>Summary:</strong> ${dh.dischargeSummary}<br>
                <strong>Released Resources:</strong> ${dh.releasedResourcesSummary || 'Bed and equipment auto-released'}<br>
                <strong>Follow-Up:</strong> ${dh.followUpInstructions || '-'}
            </div>
        `).join('');

    const referralsHtml = (!p360.referrals || p360.referrals.length === 0)
        ? `<div class="cell-muted" style="padding:10px;">No inter-hospital referrals recorded.</div>`
        : p360.referrals.map(ref => `
            <div style="background:#fff; border:1px solid var(--border-color); border-radius:6px; padding:10px; margin-bottom:8px; font-size:12.5px;">
                <strong>Referral ${ref.referralId}</strong> - Target Hospital: <strong>${ref.toHospitalId}</strong><br>
                Dijkstra Shortest Travel Time: ${ref.travelTimeMinutes} mins | Route: ${ref.routePath || '-'}<br>
                Reason: ${ref.referralReason}
            </div>
        `).join('');

    document.getElementById('p360-body').innerHTML = `
        <div class="p360-container">
            <!-- Sidebar Demographic Card -->
            <div class="p360-sidebar">
                <div style="text-align:center; margin-bottom:14px;">
                    <div style="font-size:36px;">👤</div>
                    <div style="font-size:16px; font-weight:700;">${pat.fullName}</div>
                    <div class="p360-badge ${pat.patientType === 'REGULAR' ? 'badge-regular' : 'badge-new'}" style="margin-top:4px;">
                        ${pat.patientType || 'NEW'} PATIENT
                    </div>
                </div>
                
                <div style="font-size:12.5px; line-height:1.7;">
                    <div><strong>Citizen ID:</strong> ${pat.patientId}</div>
                    <div><strong>DOB:</strong> ${pat.dateOfBirth || 'N/A'}</div>
                    <div><strong>Age:</strong> ${pat.age != null ? pat.age + ' yrs' : '-'}</div>
                    <div><strong>Gender:</strong> ${pat.gender}</div>
                    <div><strong>Mobile:</strong> ${pat.contactNumber}</div>
                    <div><strong>Email:</strong> ${pat.email || 'None'}</div>
                    <div><strong>Address:</strong> ${pat.address}</div>
                    <div style="margin-top:8px; border-top:1px solid var(--border-color); padding-top:6px;">
                        <strong>Preferred Doctor:</strong><br>
                        <span style="color:#0369a1; font-weight:600;">${prefDocDisplay}</span>
                    </div>
                </div>
            </div>

            <!-- Main Records Panel -->
            <div>
                <div class="p360-section-title">🏥 Clinical Episodes & Visit History</div>
                ${visitsHtml}

                <div class="p360-section-title">🩺 Doctor Consultations & Prescriptions</div>
                ${consultationsHtml}

                <div class="p360-section-title">📋 Doctor Treatment Plans & Pathways</div>
                ${treatmentPlansHtml}

                <div class="p360-section-title">🔬 Diagnostic Orders, Lab Reports & Doctor Reviews</div>
                ${diagnosticHtml}

                <div class="p360-section-title">📦 Allocated Beds & Medical Resources</div>
                ${resourcesHtml}

                <div class="p360-section-title">📥 Hospital Admissions</div>
                ${admissionsHtml}

                <div class="p360-section-title">🚑 Inter-Hospital Referrals (Dijkstra)</div>
                ${referralsHtml}

                <div class="p360-section-title">📤 Clinical Discharges & Follow-Up Plans</div>
                ${dischargesHtml}
            </div>
        </div>
    `;
}

function setupSearch() {
    const searchInput = document.getElementById('patient-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            const filtered = filterPatientsByType(patientsList).filter(p =>
                p.fullName.toLowerCase().includes(query) ||
                p.patientId.toLowerCase().includes(query) ||
                p.contactNumber.includes(query)
            );
            renderPatientsTable(filtered);
        });
    }
}
