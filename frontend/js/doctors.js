/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Doctors Module Script
 * Features:
 *  1. Patient search by ID / Name with autocomplete & condition/severity display.
 *  2. Doctor prescription studio (Bed, Scans, Treatments) with automated local allocation,
 *     Dijkstra automated referral routing, or priority waiting queue.
 *  3. Doctor Registration with visible "Save Doctor" action, automatic sequence ID, and single-click duty toggle.
 */

let doctorsList = [];
let selectedPatient = null;
let searchDebounceTimeout = null;

document.addEventListener('DOMContentLoaded', () => {
    // Set max date constraints
    const todayStr = new Date().toISOString().split('T')[0];
    const dobInput = document.getElementById('doc-dob');
    const joinInput = document.getElementById('doc-joining');
    if (dobInput) dobInput.max = todayStr;
    if (joinInput) joinInput.max = todayStr;
    if (joinInput) joinInput.value = todayStr;

    // Load initial data
    loadDoctors();
    loadActiveDoctorsDropdown();
    loadQuickPatients();

    // Setup prescription form submit
    const prescribeForm = document.getElementById('doctor-prescribe-form');
    if (prescribeForm) {
        prescribeForm.addEventListener('submit', handlePrescriptionSubmit);
    }

    // Check URL query param for pre-selected patient (e.g. from Patients module)
    const urlParams = new URLSearchParams(window.location.search);
    const prePatientId = urlParams.get('patientId');
    if (prePatientId) {
        autoSelectPatientById(prePatientId);
    }

    // Close search dropdown on click outside
    document.addEventListener('click', (e) => {
        const searchBox = document.querySelector('.patient-search-box');
        const results = document.getElementById('patient-search-results');
        if (searchBox && !searchBox.contains(e.target) && results) {
            results.style.display = 'none';
        }
    });
});

/* ==========================================================================
   TAB NAVIGATION (Clinical Workspace vs Doctors Roster)
   ========================================================================== */
function switchMainTab(tab) {
    const secWorkspace = document.getElementById('section-clinical-workspace');
    const secRoster = document.getElementById('section-doctor-roster');
    const btnWorkspace = document.getElementById('tab-btn-workspace');
    const btnRoster = document.getElementById('tab-btn-roster');

    if (tab === 'workspace') {
        if (secWorkspace) secWorkspace.style.display = 'block';
        if (secRoster) secRoster.style.display = 'none';
        btnWorkspace?.classList.add('active');
        btnRoster?.classList.remove('active');
    } else {
        if (secWorkspace) secWorkspace.style.display = 'none';
        if (secRoster) secRoster.style.display = 'block';
        btnWorkspace?.classList.remove('active');
        btnRoster?.classList.add('active');
    }
}

/* ==========================================================================
   PATIENT SEARCH & SELECTION (Step 1)
   ========================================================================== */
function handlePatientSearchInput() {
    clearTimeout(searchDebounceTimeout);
    const query = document.getElementById('patient-search-input')?.value.trim();
    const resultsContainer = document.getElementById('patient-search-results');

    if (!query || query.length < 1) {
        if (resultsContainer) resultsContainer.style.display = 'none';
        return;
    }

    searchDebounceTimeout = setTimeout(async () => {
        try {
            const patients = await api.get(`/patients/overview?search=${encodeURIComponent(query)}`);
            renderPatientSearchResults(patients);
        } catch (error) {
            console.error('Patient search failed:', error);
        }
    }, 250);
}

function renderPatientSearchResults(patients) {
    const resultsContainer = document.getElementById('patient-search-results');
    if (!resultsContainer) return;

    if (!patients || patients.length === 0) {
        resultsContainer.innerHTML = `
            <div style="padding:14px; text-align:center; color:var(--text-secondary); font-size:13px;">
                No patient found matching search query.
            </div>
        `;
        resultsContainer.style.display = 'block';
        return;
    }

    resultsContainer.innerHTML = patients.map(p => `
        <div class="patient-search-item" onclick="selectPatientById('${p.patientId}')">
            <div>
                <div style="font-weight:700; color:var(--text-primary); font-size:14px;">
                    ${p.patientName} <span style="font-size:12px; color:var(--primary); font-weight:600;">(${p.patientId})</span>
                </div>
                <div style="font-size:12px; color:var(--text-secondary); margin-top:2px;">
                    ${p.age ? p.age + ' yrs' : '-'} / ${p.gender || '-'} | Condition: <strong>${p.reportedCondition || 'None'}</strong>
                </div>
            </div>
            <div>
                ${getSeverityBadge(p.severity || 'Normal')}
            </div>
        </div>
    `).join('');

    resultsContainer.style.display = 'block';
}

async function loadQuickPatients() {
    try {
        const patients = await api.get('/patients/overview');
        const quickContainer = document.getElementById('quick-patient-chips');
        if (!quickContainer || !patients) return;

        // Take first 5 active patients
        const samplePatients = patients.slice(0, 5);
        quickContainer.innerHTML = samplePatients.map(p => `
            <button type="button" class="btn btn-outline" style="padding:4px 10px; font-size:11.5px; border-radius:20px;" onclick="selectPatientById('${p.patientId}')">
                👤 ${p.patientId} - ${p.patientName} (${p.severity || 'Normal'})
            </button>
        `).join('');
    } catch (e) {
        console.warn('Could not load quick patients:', e);
    }
}

async function autoSelectPatientById(patientId) {
    await selectPatientById(patientId);
}

async function selectPatientById(patientId) {
    try {
        const patients = await api.get(`/patients/overview?search=${encodeURIComponent(patientId)}`);
        const found = patients.find(p => p.patientId === patientId) || patients[0];
        if (found) {
            selectPatient(found);
        } else {
            showToast(`Patient ${patientId} not found`, 'warning');
        }
    } catch (error) {
        console.error('Failed to select patient:', error);
        showToast('Error loading patient details', 'error');
    }
}

function selectPatient(p) {
    selectedPatient = p;

    // Hide search dropdown
    const resultsContainer = document.getElementById('patient-search-results');
    if (resultsContainer) resultsContainer.style.display = 'none';

    // Populate search input with name
    const searchInput = document.getElementById('patient-search-input');
    if (searchInput) searchInput.value = `${p.patientId} - ${p.patientName}`;

    // Fill details card
    document.getElementById('card-patient-name').textContent = p.patientName;
    document.getElementById('card-patient-id').textContent = p.patientId;
    document.getElementById('card-patient-age-gender').textContent = `${p.age ? p.age + ' Years' : 'Age -'} / ${p.gender || '-'}`;
    document.getElementById('card-patient-contact').textContent = p.contactNumber || 'N/A';
    document.getElementById('card-patient-address').textContent = p.address || 'N/A';
    document.getElementById('card-patient-condition').textContent = p.reportedCondition || 'No symptoms reported during registration';

    // Badges
    const sevBadgeEl = document.getElementById('card-severity-badge');
    if (sevBadgeEl) sevBadgeEl.innerHTML = getSeverityBadge(p.severity || 'Normal');

    const statusBadgeEl = document.getElementById('card-status-badge');
    if (statusBadgeEl) {
        const isAdmitted = p.admissionStatus === 'ADMITTED';
        statusBadgeEl.className = `badge ${isAdmitted ? 'badge-occupied' : 'badge-available'}`;
        statusBadgeEl.textContent = isAdmitted ? 'CURRENTLY ADMITTED' : 'OUTPATIENT';
    }

    // Set Severity dropdown default
    const sevSelect = document.getElementById('prescribe-severity-select');
    if (sevSelect) {
        sevSelect.value = p.severity || 'Normal';
    }

    // Show Card & Prescription Form
    document.getElementById('selected-patient-card').style.display = 'block';
    document.getElementById('prescription-form-container').style.display = 'block';

    // Hide old result banner
    const banner = document.getElementById('prescription-result-banner');
    if (banner) banner.style.display = 'none';

    // Scroll to prescription form
    document.getElementById('prescription-form-container').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function clearSelectedPatient() {
    selectedPatient = null;
    const card = document.getElementById('selected-patient-card');
    const formContainer = document.getElementById('prescription-form-container');
    const banner = document.getElementById('prescription-result-banner');
    const searchInput = document.getElementById('patient-search-input');

    if (card) card.style.display = 'none';
    if (formContainer) formContainer.style.display = 'none';
    if (banner) banner.style.display = 'none';
    if (searchInput) {
        searchInput.value = '';
        searchInput.focus();
    }
}

/* ==========================================================================
   PRESCRIPTION FORM & AUTOMATED ALLOCATION (Step 2)
   ========================================================================== */
function toggleBedTypeSelector() {
    const isChecked = document.getElementById('check-require-admission')?.checked;
    const bedGroup = document.getElementById('bed-type-group');
    if (bedGroup) {
        bedGroup.style.display = isChecked ? 'block' : 'none';
    }
}

async function loadActiveDoctorsDropdown() {
    try {
        const doctors = await api.get('/doctors');
        const select = document.getElementById('prescribe-doctor-select');
        if (!select) return;

        const activeDoctors = doctors.filter(d => Boolean(d.isActive) && d.dutyStatus === 'ON_DUTY');
        if (activeDoctors.length === 0) {
            select.innerHTML = `<option value="">No On-Duty Doctors Available</option>`;
            return;
        }

        select.innerHTML = activeDoctors.map(d => `
            <option value="${d.doctorId}">
                ${d.doctorName} (${d.specialization}) - [${d.doctorId}]
            </option>
        `).join('');
    } catch (e) {
        console.error('Failed to populate prescribing doctor dropdown:', e);
    }
}

async function handlePrescriptionSubmit(e) {
    e.preventDefault();

    if (!selectedPatient) {
        showToast('Please search and select a patient first.', 'warning');
        return;
    }

    const doctorId = document.getElementById('prescribe-doctor-select')?.value;
    if (!doctorId) {
        showToast('Please select a prescribing doctor.', 'warning');
        return;
    }

    const severity = document.getElementById('prescribe-severity-select')?.value || 'Normal';
    const requireAdmission = document.getElementById('check-require-admission')?.checked || false;
    const bedType = requireAdmission ? (document.getElementById('prescribe-bed-type')?.value || 'GENERAL') : 'NONE';

    // Gather checked diagnostic services
    const serviceCheckboxes = document.querySelectorAll('input[name="prescribe-service"]:checked');
    const services = Array.from(serviceCheckboxes).map(cb => cb.value);

    // Gather checked treatments/equipment
    const treatmentCheckboxes = document.querySelectorAll('input[name="prescribe-treatment"]:checked');
    const treatments = Array.from(treatmentCheckboxes).map(cb => cb.value);

    const notes = document.getElementById('prescribe-notes')?.value.trim();

    // Validate that at least one service/bed/treatment was selected
    if (!requireAdmission && services.length === 0 && treatments.length === 0) {
        showToast('Please select at least one prescription service, bed, or treatment.', 'warning');
        return;
    }

    const submitBtn = document.getElementById('btn-prescribe-submit');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `⏳ Verifying Resources & Processing...`;
    }

    const payload = {
        patientId: selectedPatient.patientId,
        doctorId: doctorId,
        admissionRequired: requireAdmission,
        bedType: bedType,
        services: services,
        treatments: treatments,
        notes: notes,
        severity: severity
    };

    try {
        const response = await api.post('/doctors/prescribe', payload);
        renderPrescriptionResultBanner(response);
        showToast('Prescription processed successfully!', 'success');

        // Re-fetch patient details to reflect updated admission or severity
        selectPatientById(selectedPatient.patientId);
    } catch (error) {
        console.error('Prescription processing failed:', error);
        showToast(error.message || 'Prescription submission failed', 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = `⚡ Check Availability & Prescribe Resources`;
        }
    }
}

function renderPrescriptionResultBanner(res) {
    const banner = document.getElementById('prescription-result-banner');
    if (!banner) return;

    if (res.status === 'ALLOCATED') {
        banner.className = 'result-banner success';
        banner.innerHTML = `
            <div style="display:flex; align-items:flex-start; gap:14px;">
                <span style="font-size:28px;">✅</span>
                <div style="flex:1;">
                    <div style="font-size:16px; font-weight:800; color:#065f46; margin-bottom:4px;">
                        RESOURCES SUCCESSFULLY ALLOCATED & LOCKED (Primary Hospital H-001)
                    </div>
                    <p style="font-size:13.5px; margin:0 0 10px 0;">${res.message}</p>
                    <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(180px, 1fr)); gap:10px; background:#ffffffcc; padding:12px; border-radius:8px; border:1px solid #a7f3d0;">
                        ${res.allocatedBedNumber ? `<div><strong>Assigned Bed:</strong> <span class="badge badge-occupied">${res.allocatedBedNumber} (${res.allocatedBedType})</span></div>` : ''}
                        ${res.admissionId ? `<div><strong>Admission ID:</strong> <code>${res.admissionId}</code></div>` : ''}
                        ${res.diagnosticOrders && res.diagnosticOrders.length > 0 ? `<div><strong>Diagnostic Tests:</strong> ${res.diagnosticOrders.join(', ')}</div>` : ''}
                        ${res.allocatedResources && res.allocatedResources.length > 0 ? `<div><strong>Allocated Resources:</strong> ${res.allocatedResources.join(', ')}</div>` : ''}
                    </div>
                    <div style="margin-top:12px; display:flex; gap:10px;">
                        <a href="resources.html" class="btn btn-outline" style="padding:5px 12px; font-size:12px;">View in Available Resources</a>
                        <a href="discharge.html" class="btn btn-outline" style="padding:5px 12px; font-size:12px;">Go to Discharge Summary</a>
                    </div>
                </div>
            </div>
        `;
    } else if (res.status === 'REFERRAL_INITIATED') {
        banner.className = 'result-banner referral';
        banner.innerHTML = `
            <div style="display:flex; align-items:flex-start; gap:14px;">
                <span style="font-size:28px;">🚑</span>
                <div style="flex:1;">
                    <div style="font-size:16px; font-weight:800; color:#92400e; margin-bottom:4px;">
                        LOCAL RESOURCES UNAVAILABLE – AUTOMATED DIJKSTRA REFERRAL INITIATED
                    </div>
                    <p style="font-size:13.5px; margin:0 0 10px 0;">${res.message}</p>
                    <div style="background:#ffffffcc; padding:14px; border-radius:8px; border:1px solid #fde68a;">
                        <div style="font-size:13.5px; font-weight:700; color:#78350f;">
                            Destination: ${res.referralHospitalName} (${res.referralHospitalId})
                        </div>
                        <div style="font-size:12.5px; color:#451a03; margin-top:4px;">
                            📍 Route: <strong>${res.referralRoute || 'Direct Network Highway'}</strong> | Est. Travel Time: <strong>⏱️ ${res.referralTravelTimeMinutes} minutes</strong>
                        </div>
                        <div style="font-size:12px; color:#78350f; margin-top:4px;">
                            📞 Contact: ${res.referralHospitalContact} | Referral ID: <code>${res.referralId}</code>
                        </div>
                    </div>
                    <div style="margin-top:12px; display:flex; gap:10px;">
                        <a href="referrals.html" class="btn btn-primary" style="padding:6px 14px; font-size:12px; background:#d97706; border-color:#d97706;">
                            👉 Review & Approve Referral in Referrals Module
                        </a>
                    </div>
                </div>
            </div>
        `;
    } else if (res.status === 'WAITING_LIST') {
        banner.className = 'result-banner waiting';
        banner.innerHTML = `
            <div style="display:flex; align-items:flex-start; gap:14px;">
                <span style="font-size:28px;">⏳</span>
                <div style="flex:1;">
                    <div style="font-size:16px; font-weight:800; color:#1e40af; margin-bottom:4px;">
                        NETWORK CAPACITY FULL – AUTOMATICALLY ENQUEUED IN SEVERITY PRIORITY QUEUE
                    </div>
                    <p style="font-size:13.5px; margin:0 0 10px 0;">${res.message}</p>
                    <div style="background:#ffffffcc; padding:12px; border-radius:8px; border:1px solid #bfdbfe; font-size:13px;">
                        Queue Entry ID: <code>${res.queueId}</code> | Priority Score: <strong>${res.priorityScore}</strong> (${selectedPatient.severity} Priority)
                        <div style="font-size:11.5px; color:#1e3a8a; margin-top:4px;">
                            ⚡ Automated Auto-Allocation: Newly freed resources upon patient discharge will automatically be assigned to this patient without any manual intervention.
                        </div>
                    </div>
                    <div style="margin-top:12px; display:flex; gap:10px;">
                        <a href="resources.html" class="btn btn-outline" style="padding:5px 12px; font-size:12px;">View Waiting Queue in Resources Module</a>
                    </div>
                </div>
            </div>
        `;
    }

    banner.style.display = 'block';
    banner.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/* ==========================================================================
   DOCTORS ROSTER & REGISTRATION (Section 2)
   ========================================================================== */
async function loadDoctors() {
    try {
        doctorsList = await api.get('/doctors?includeDeactivated=true');
        const countEl = document.getElementById('count-doctors');
        if (countEl) countEl.textContent = doctorsList.filter(d => Boolean(d.isActive)).length;
        renderDoctorsTable(filterDoctors());
    } catch (error) {
        console.error('Failed to load doctors:', error);
    }
}

function filterDoctorsTable() {
    renderDoctorsTable(filterDoctors());
}

function filterDoctors() {
    const specFilter = document.getElementById('spec-filter')?.value;
    const query = document.getElementById('doctor-search')?.value.toLowerCase().trim() || '';

    return doctorsList.filter(doc => {
        const matchesActive = Boolean(doc.isActive);
        const matchesSpec = !specFilter || doc.specialization === specFilter;
        const matchesSearch = !query ||
            doc.doctorName.toLowerCase().includes(query) ||
            doc.doctorId.toLowerCase().includes(query) ||
            (doc.contactNumber && doc.contactNumber.includes(query));

        return matchesActive && matchesSpec && matchesSearch;
    });
}

function renderDoctorsTable(doctors) {
    const tbody = document.getElementById('doctors-table-body');
    if (!tbody) return;

    if (!doctors || doctors.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="empty-state">No doctors found matching criteria.</td></tr>`;
        return;
    }

    tbody.innerHTML = doctors.map(doc => {
        const isActive = Boolean(doc.isActive);

        // Single Duty Status Control Toggle
        let dutyStatusCell = '';
        if (!isActive) {
            dutyStatusCell = `<span class="badge" style="background:#f1f5f9; color:#64748b;">N/A (Departed)</span>`;
        } else if (doc.dutyStatus === 'ON_DUTY') {
            dutyStatusCell = `
                <button class="btn-toggle-duty on" onclick="toggleDutyStatus('${doc.doctorId}')" title="Click to switch to Off Duty">
                    🟢 On Duty
                </button>
            `;
        } else {
            dutyStatusCell = `
                <button class="btn-toggle-duty off" onclick="toggleDutyStatus('${doc.doctorId}')" title="Click to switch to On Duty">
                    🔴 Off Duty
                </button>
            `;
        }

        // Availability Badge
        let availBadge = '';
        if (!isActive) {
            availBadge = `<span class="badge badge-occupied">DEACTIVATED</span>`;
        } else if (doc.dutyStatus === 'OFF_DUTY') {
            availBadge = `<span class="badge" style="background:#fee2e2; color:#991b1b;">OFF DUTY</span>`;
        } else if (doc.availabilityStatus === 'AVAILABLE') {
            availBadge = `<span class="badge badge-available">AVAILABLE</span>`;
        } else if (doc.availabilityStatus === 'IN_CONSULTATION') {
            availBadge = `<span class="badge badge-info">IN CONSULTATION</span>`;
        } else {
            availBadge = `<span class="badge badge-occupied">${doc.availabilityStatus}</span>`;
        }

        const typeLabel = doc.doctorType === 'GENERAL_PHYSICIAN' ? 'General Physician' : 'Specialist';
        const dobDisplay = doc.dateOfBirth ? doc.dateOfBirth : 'N/A';
        const ageDisplay = doc.age != null ? `${doc.age} yrs` : '-';

        return `
            <tr>
                <td><strong>${doc.doctorId}</strong></td>
                <td>
                    <div style="font-weight: 700; color:var(--text-primary);">${doc.doctorName}</div>
                    <div class="cell-muted" style="font-size:11px;">${typeLabel}</div>
                </td>
                <td>
                    <span class="badge badge-info">${doc.specialization}</span>
                    <div class="cell-muted" style="font-size:11px; margin-top:2px;">${doc.qualification}</div>
                </td>
                <td>
                    <div>${ageDisplay} / ${doc.gender || '-'}</div>
                    <div class="cell-muted" style="font-size:11px;">DOB: ${dobDisplay}</div>
                </td>
                <td>${dutyStatusCell}</td>
                <td>${availBadge}</td>
                <td>${doc.currentInpatientCount || 0} / ${doc.maxInpatientCapacity}</td>
                <td>
                    <div>📞 ${doc.contactNumber}</div>
                    <div class="cell-muted" style="font-size:11px;">✉️ ${doc.email}</div>
                </td>
                <td class="cell-actions">
                    <button class="btn btn-outline btn-sm" onclick="deactivateDoctor('${doc.doctorId}', '${doc.doctorName}')" style="color:#dc2626; border-color:#fca5a5;" title="Permanent resignation/departure">
                        Deactivate
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function calculateDoctorAgeFromDob() {
    const dobInput = document.getElementById('doc-dob');
    const ageInput = document.getElementById('doc-age');
    if (!dobInput || !ageInput || !dobInput.value) {
        if (ageInput) ageInput.value = '';
        return;
    }

    const dob = new Date(dobInput.value);
    const today = new Date();

    if (dob > today) {
        showFieldError('doc-dob', 'Date of birth cannot be in the future.');
        ageInput.value = '';
        return;
    }

    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
        age--;
    }

    if (age < 21) {
        showFieldError('doc-dob', `Doctor must be at least 21 years old (Calculated age: ${age}).`);
    } else {
        hideFieldError('doc-dob');
    }

    ageInput.value = Math.max(0, age);
}

function openAddDoctorModal() {
    ['doc-name', 'doc-dob', 'doc-gender', 'doc-spec', 'doc-qual', 'doc-phone', 'doc-email', 'doc-joining'].forEach(hideFieldError);
    const form = document.getElementById('doctor-reg-form');
    if (form) form.reset();
    document.getElementById('doc-age').value = '';
    document.getElementById('doc-joining').value = new Date().toISOString().split('T')[0];
    openModal('add-doctor-modal');
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

function validateDoctorForm() {
    let isValid = true;

    // Full Name
    const name = document.getElementById('doc-name').value.trim();
    if (!name || !/^[A-Za-z\s\.]+$/.test(name)) {
        showFieldError('doc-name', 'Doctor name is mandatory and must contain only valid letters, spaces, and dots.');
        isValid = false;
    } else {
        hideFieldError('doc-name');
    }

    // DOB
    const dob = document.getElementById('doc-dob').value;
    if (!dob) {
        showFieldError('doc-dob', 'Date of birth is mandatory.');
        isValid = false;
    } else {
        const dobDate = new Date(dob);
        const today = new Date();
        let age = today.getFullYear() - dobDate.getFullYear();
        const monthDiff = today.getMonth() - dobDate.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dobDate.getDate())) age--;

        if (dobDate > today) {
            showFieldError('doc-dob', 'Date of birth cannot be in the future.');
            isValid = false;
        } else if (age < 21) {
            showFieldError('doc-dob', `Doctor must be at least 21 years old (Current age: ${age}).`);
            isValid = false;
        } else {
            hideFieldError('doc-dob');
        }
    }

    // Gender
    const gender = document.getElementById('doc-gender').value;
    if (!gender) {
        showFieldError('doc-gender', 'Gender selection is mandatory.');
        isValid = false;
    } else {
        hideFieldError('doc-gender');
    }

    // Specialization
    const spec = document.getElementById('doc-spec').value;
    if (!spec) {
        showFieldError('doc-spec', 'Specialization is mandatory.');
        isValid = false;
    } else {
        hideFieldError('doc-spec');
    }

    // Qualification
    const qual = document.getElementById('doc-qual').value.trim();
    if (!qual) {
        showFieldError('doc-qual', 'Qualification is mandatory.');
        isValid = false;
    } else {
        hideFieldError('doc-qual');
    }

    // Phone (10-digit Indian)
    const phone = document.getElementById('doc-phone').value.trim();
    if (!phone || !/^[6-9]\d{9}$/.test(phone)) {
        showFieldError('doc-phone', 'Please enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.');
        isValid = false;
    } else {
        hideFieldError('doc-phone');
    }

    // Email
    const email = document.getElementById('doc-email').value.trim();
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showFieldError('doc-email', 'Valid official email address is mandatory.');
        isValid = false;
    } else {
        hideFieldError('doc-email');
    }

    // Joining Date
    const joining = document.getElementById('doc-joining').value;
    if (!joining) {
        showFieldError('doc-joining', 'Joining date is mandatory.');
        isValid = false;
    } else {
        const joinDate = new Date(joining);
        const today = new Date();
        if (joinDate > today) {
            showFieldError('doc-joining', 'Joining date cannot be in the future.');
            isValid = false;
        } else {
            hideFieldError('doc-joining');
        }
    }

    return isValid;
}

async function handleDoctorRegistration(event) {
    event.preventDefault();

    if (!validateDoctorForm()) {
        return;
    }

    const saveBtn = document.getElementById('btn-save-doctor');
    if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.innerHTML = `⏳ Saving Doctor...`;
    }

    const data = {
        doctorName: document.getElementById('doc-name').value.trim(),
        dateOfBirth: document.getElementById('doc-dob').value,
        gender: document.getElementById('doc-gender').value,
        specialization: document.getElementById('doc-spec').value,
        qualification: document.getElementById('doc-qual').value.trim(),
        contactNumber: document.getElementById('doc-phone').value.trim(),
        email: document.getElementById('doc-email').value.trim(),
        dateOfJoining: document.getElementById('doc-joining').value,
        hospitalId: 'H-001',
        doctorType: document.getElementById('doc-type')?.value || 'SPECIALIST',
        maxInpatientCapacity: 6,
        dutyStatus: 'ON_DUTY',
        availabilityStatus: 'AVAILABLE'
    };

    try {
        const created = await api.post('/doctors', data);
        showToast(`Doctor successfully registered and saved! Assigned ID: ${created.doctorId}`, 'success');
        closeModal('add-doctor-modal');
        loadDoctors();
        loadActiveDoctorsDropdown();
    } catch (error) {
        console.error('Registration failed:', error);
        showToast(error.message || 'Failed to save doctor', 'error');
    } finally {
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = `💾 Save Doctor`;
        }
    }
}

async function toggleDutyStatus(doctorId) {
    try {
        const updated = await api.post(`/doctors/${doctorId}/toggle-duty`, {});
        const statusText = updated.dutyStatus === 'ON_DUTY' ? '🟢 ON DUTY' : '🔴 OFF DUTY';
        showToast(`Dr. ${updated.doctorName} (${doctorId}) is now ${statusText}`, 'success');
        loadDoctors();
        loadActiveDoctorsDropdown();
    } catch (error) {
        console.error('Failed to toggle duty status:', error);
        showToast(error.message || 'Failed to toggle duty status', 'error');
    }
}

async function deactivateDoctor(doctorId, doctorName) {
    const confirmed = confirm(
        `Are you sure you want to deactivate ${doctorName} (${doctorId})?\n\n` +
        `They will be removed from active rosters and cannot receive new prescriptions.\n` +
        `Historical patient consultation records are strictly preserved.`
    );

    if (!confirmed) return;

    try {
        await api.delete(`/doctors/${doctorId}`);
        showToast(`Doctor ${doctorId} deactivated. Historical records preserved.`, 'info');
        loadDoctors();
        loadActiveDoctorsDropdown();
    } catch (error) {
        console.error('Failed to deactivate doctor:', error);
        showToast(error.message || 'Failed to deactivate doctor', 'error');
    }
}
