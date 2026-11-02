/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Patients Module Script
 * Features:
 *  - Citizen Registration with atomic unique Patient ID generation
 *  - Complete patient directory displaying medical condition & severity (Normal, Serious, Critical)
 *  - Dynamic age calculation from Date of Birth
 *  - Strict separation: Prescription & Resource Allocation are routed exclusively to the Doctors Module
 */

let patientsList = [];
let currentSeverityFilter = 'ALL';

document.addEventListener('DOMContentLoaded', () => {
    // Set max DOB to today
    const todayStr = new Date().toISOString().split('T')[0];
    const dobInput = document.getElementById('pat-dob');
    if (dobInput) dobInput.max = todayStr;

    loadPatients();
});

async function loadPatients() {
    try {
        patientsList = await api.get('/patients/overview');
        updateKpis();
        renderPatientsTable(filterPatients());
    } catch (error) {
        console.error('Failed to load patients:', error);
        showToast('Error loading patients list', 'error');
    }
}

function updateKpis() {
    const total = patientsList.length;
    let critical = 0;
    let serious = 0;
    let normal = 0;

    patientsList.forEach(p => {
        const sev = (p.severity || '').toLowerCase();
        if (sev === 'critical') critical++;
        else if (sev === 'serious') serious++;
        else normal++;
    });

    document.getElementById('kpi-total-patients').textContent = total;
    document.getElementById('kpi-critical-patients').textContent = critical;
    document.getElementById('kpi-serious-patients').textContent = serious;
    document.getElementById('kpi-normal-patients').textContent = normal;

    document.getElementById('count-filter-all').textContent = total;
    document.getElementById('count-filter-critical').textContent = critical;
    document.getElementById('count-filter-serious').textContent = serious;
    document.getElementById('count-filter-normal').textContent = normal;
}

function setSeverityFilter(severity) {
    currentSeverityFilter = severity;

    // Update active button classes
    document.querySelectorAll('.severity-filter-btn').forEach(btn => btn.classList.remove('active'));
    if (severity === 'ALL') document.getElementById('filter-all')?.classList.add('active');
    else if (severity === 'Critical') document.getElementById('filter-critical')?.classList.add('active');
    else if (severity === 'Serious') document.getElementById('filter-serious')?.classList.add('active');
    else if (severity === 'Normal') document.getElementById('filter-normal')?.classList.add('active');

    renderPatientsTable(filterPatients());
}

function filterPatientsTable() {
    renderPatientsTable(filterPatients());
}

function filterPatients() {
    const query = document.getElementById('patient-search-input')?.value.toLowerCase().trim() || '';

    return patientsList.filter(p => {
        // Severity match
        let matchesSeverity = true;
        const sev = (p.severity || '').toLowerCase();
        if (currentSeverityFilter === 'Critical') matchesSeverity = (sev === 'critical');
        else if (currentSeverityFilter === 'Serious') matchesSeverity = (sev === 'serious');
        else if (currentSeverityFilter === 'Normal') matchesSeverity = (sev === 'normal' || sev === 'minor' || !p.severity);

        // Search match
        const matchesQuery = !query ||
            p.patientId.toLowerCase().includes(query) ||
            (p.fullName || p.patientName || '').toLowerCase().includes(query) ||
            (p.contactNumber && p.contactNumber.includes(query)) ||
            (p.reportedCondition && p.reportedCondition.toLowerCase().includes(query)) ||
            (p.medicalCondition && p.medicalCondition.toLowerCase().includes(query));

        return matchesSeverity && matchesQuery;
    });
}

function renderPatientsTable(patients) {
    const tbody = document.getElementById('patients-table-body');
    if (!tbody) return;

    if (!patients || patients.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state">No patient records found matching criteria.</td></tr>`;
        return;
    }

    tbody.innerHTML = patients.map(p => {
        const isAdmitted = p.admissionStatus === 'ADMITTED';
        const admissionBadge = isAdmitted
            ? `<span class="badge badge-occupied">Bed: ${p.assignedBedNumber || 'ADMITTED'}</span>`
            : `<span class="badge badge-available">Outpatient</span>`;

        const ageDisplay = p.age != null ? `${p.age} yrs` : '-';
        const dobDisplay = p.dateOfBirth ? p.dateOfBirth : 'N/A';

        return `
            <tr>
                <td><strong>${p.patientId}</strong></td>
                <td>
                    <div style="font-weight:700; color:var(--text-primary); font-size:14px;">${p.fullName || p.patientName || 'Unknown'}</div>
                    <div class="cell-muted" style="font-size:11px;">Citizen Record</div>
                </td>
                <td>
                    <div>${ageDisplay} / ${p.gender || '-'}</div>
                    <div class="cell-muted" style="font-size:11px;">DOB: ${dobDisplay}</div>
                </td>
                <td>
                    <div>📞 ${p.contactNumber || 'N/A'}</div>
                    <div class="cell-muted" style="font-size:11px; max-width:180px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${p.address || ''}">
                        📍 ${p.address || 'N/A'}
                    </div>
                </td>
                <td>
                    <div style="font-size:12.5px; font-weight:600; color:#1e293b; max-width:260px;">
                        ${p.reportedCondition || 'No chief complaint recorded'}
                    </div>
                </td>
                <td>
                    ${getSeverityBadge(p.severity || 'Normal')}
                </td>
                <td>
                    ${admissionBadge}
                </td>
                <td>
                    <a href="doctors.html?patientId=${p.patientId}" class="btn-prescribe-nav" title="Prescribe Bed, Scans, and Treatments in Doctors Module">
                        <span>🩺</span> Prescribe
                    </a>
                </td>
            </tr>
        `;
    }).join('');
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
    }

    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
        age--;
    }

    if (age < 0 || age > 130) {
        showFieldError('pat-dob', 'Calculated age is out of realistic range.');
        ageInput.value = '';
        return;
    }

    hideFieldError('pat-dob');
    ageInput.value = age;
}

function openNewPatientModal() {
    ['pat-name', 'pat-dob', 'pat-gender', 'pat-phone', 'pat-email', 'pat-address', 'pat-condition'].forEach(hideFieldError);
    const form = document.getElementById('patient-reg-form');
    if (form) form.reset();
    document.getElementById('pat-age').value = '';
    openModal('patient-reg-modal');
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
        showFieldError('pat-name', 'Full name is mandatory and must contain only letters, dots, and spaces.');
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
        if (dobDate > new Date()) {
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

    // Mobile Number
    const phone = document.getElementById('pat-phone').value.trim();
    if (!phone || !/^[6-9]\d{9}$/.test(phone)) {
        showFieldError('pat-phone', 'Enter a valid 10-digit Indian mobile number starting with 6-9.');
        isValid = false;
    } else {
        hideFieldError('pat-phone');
    }

    // Email (optional)
    const email = document.getElementById('pat-email').value.trim();
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showFieldError('pat-email', 'Please provide a valid email format.');
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

    // Medical Condition
    const condition = document.getElementById('pat-condition').value.trim();
    if (!condition) {
        showFieldError('pat-condition', 'Reported medical condition is mandatory.');
        isValid = false;
    } else {
        hideFieldError('pat-condition');
    }

    return isValid;
}

async function handlePatientRegistration(event) {
    event.preventDefault();

    if (!validatePatientForm()) {
        return;
    }

    const saveBtn = document.getElementById('btn-save-patient');
    if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.innerHTML = `⏳ Registering & Generating ID...`;
    }

    const payload = {
        fullName: document.getElementById('pat-name').value.trim(),
        dateOfBirth: document.getElementById('pat-dob').value,
        gender: document.getElementById('pat-gender').value,
        contactNumber: document.getElementById('pat-phone').value.trim(),
        email: document.getElementById('pat-email').value.trim() || null,
        address: document.getElementById('pat-address').value.trim(),
        notes: document.getElementById('pat-condition').value.trim(),
        severityLevel: document.getElementById('pat-severity').value,
        patientCategory: document.getElementById('pat-category').value,
        patientType: 'NEW'
    };

    try {
        const response = await api.post('/patients/register', payload);
        const newPatientId = response.patientId || (response.data && response.data.patientId);
        showToast(`Patient registered successfully! Generated ID: ${newPatientId}`, 'success');
        closeModal('patient-reg-modal');
        await loadPatients();
    } catch (error) {
        console.error('Registration failed:', error);
        showToast(error.message || 'Patient registration failed', 'error');
    } finally {
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = `💾 Register & Generate Patient ID`;
        }
    }
}
