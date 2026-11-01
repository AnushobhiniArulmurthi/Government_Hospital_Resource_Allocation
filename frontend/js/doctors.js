/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Doctor Management Script
 * Features: Single On/Off Duty toggle, Permanent deactivation (resignation), and 3-level form validation.
 */

let doctorsList = [];
let showDeactivatedView = false;

document.addEventListener('DOMContentLoaded', () => {
    // Set max date for DOB and joining date to today
    const todayStr = new Date().toISOString().split('T')[0];
    const dobInput = document.getElementById('doc-dob');
    const joinInput = document.getElementById('doc-joining');
    if (dobInput) dobInput.max = todayStr;
    if (joinInput) joinInput.max = todayStr;

    loadDoctors();
    setupDoctorForm();
    setupFilters();
});

async function loadDoctors() {
    try {
        doctorsList = await api.get(`/doctors?includeDeactivated=true`);
        updateDoctorCounts();
        renderDoctorsTable(filterDoctors());
    } catch (error) {
        console.error('Failed to load doctors:', error);
    }
}

function updateDoctorCounts() {
    const activeCount = doctorsList.filter(d => Boolean(d.isActive)).length;
    const deactCount = doctorsList.filter(d => !Boolean(d.isActive)).length;

    const elAct = document.getElementById('count-active-doctors');
    const elDeact = document.getElementById('count-deactivated-doctors');
    if (elAct) elAct.textContent = activeCount;
    if (elDeact) elDeact.textContent = deactCount;
}

function switchDoctorFilter(showDeactivated) {
    showDeactivatedView = showDeactivated;
    document.getElementById('tab-active-doctors')?.classList.toggle('active', !showDeactivated);
    document.getElementById('tab-deactivated-doctors')?.classList.toggle('active', showDeactivated);

    const heading = document.getElementById('roster-table-heading');
    if (heading) {
        heading.textContent = showDeactivated
            ? 'Resigned / Permanently Deactivated Doctors (Archived History)'
            : 'Active Hospital Medical Staff Roster';
    }

    renderDoctorsTable(filterDoctors());
}

function filterDoctors() {
    const specFilter = document.getElementById('spec-filter')?.value;
    const query = document.getElementById('doctor-search')?.value.toLowerCase().trim() || '';

    return doctorsList.filter(doc => {
        const matchesActive = showDeactivatedView ? !Boolean(doc.isActive) : Boolean(doc.isActive);
        const matchesSpec = !specFilter || doc.specialization === specFilter;
        const matchesSearch = !query ||
            doc.doctorName.toLowerCase().includes(query) ||
            doc.doctorId.toLowerCase().includes(query) ||
            (doc.contactNumber && doc.contactNumber.includes(query));

        return matchesActive && matchesSpec && matchesSearch;
    });
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

function renderDoctorsTable(doctors) {
    const tbody = document.getElementById('doctors-table-body');
    if (!tbody) return;

    if (!doctors || doctors.length === 0) {
        tbody.innerHTML = `<tr><td colspan="10" class="empty-state">No ${showDeactivatedView ? 'deactivated' : 'active'} doctors found matching criteria.</td></tr>`;
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
        const joiningDisplay = doc.dateOfJoining ? doc.dateOfJoining : 'N/A';

        // Separate Deactivation Action
        let actionButtons = '';
        if (isActive) {
            actionButtons = `
                <button class="btn btn-outline btn-sm" onclick="deactivateDoctor('${doc.doctorId}', '${doc.doctorName}')" style="color:#dc2626; border-color:#fca5a5;" title="Permanent resignation/departure">
                    Deactivate Doctor
                </button>
            `;
        } else {
            actionButtons = `<span class="cell-muted" style="font-size:11px;">Archived Record</span>`;
        }

        return `
            <tr style="${!isActive ? 'background:#fafafa; opacity:0.85;' : ''}">
                <td><strong>${doc.doctorId}</strong></td>
                <td>
                    <div style="font-weight: 700; color:var(--text-primary);">${doc.doctorName}</div>
                    <div class="cell-muted" style="font-size:11px;">${typeLabel}</div>
                </td>
                <td>
                    <div>${ageDisplay} / ${doc.gender || '-'}</div>
                    <div class="cell-muted" style="font-size:11px;">DOB: ${dobDisplay}</div>
                </td>
                <td>
                    <span class="badge badge-info">${doc.specialization}</span>
                    <div class="cell-muted" style="font-size:11px; margin-top:2px;">${doc.qualification}</div>
                </td>
                <td>${dutyStatusCell}</td>
                <td>${availBadge}</td>
                <td>${doc.currentInpatientCount || 0} / ${doc.maxInpatientCapacity}</td>
                <td class="cell-muted">${joiningDisplay}</td>
                <td>
                    <div>📞 ${doc.contactNumber}</div>
                    <div class="cell-muted" style="font-size:11px;">✉️ ${doc.email}</div>
                </td>
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
        } else if (dob) {
            const dobDate = new Date(dob);
            const yearsDiff = joinDate.getFullYear() - dobDate.getFullYear();
            if (yearsDiff < 18) {
                showFieldError('doc-joining', 'Joining date must be at least 18 years after Date of Birth.');
                isValid = false;
            } else {
                hideFieldError('doc-joining');
            }
        } else {
            hideFieldError('doc-joining');
        }
    }

    return isValid;
}

function setupDoctorForm() {
    const form = document.getElementById('doctor-reg-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!validateDoctorForm()) {
            return;
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
            hospitalId: document.getElementById('doc-hospital').value,
            doctorType: document.getElementById('doc-type').value,
            maxInpatientCapacity: parseInt(document.getElementById('doc-capacity').value || '6', 10),
            dutyStatus: 'ON_DUTY',
            availabilityStatus: 'AVAILABLE'
        };

        try {
            const created = await api.post('/doctors', data);
            showToast(`Doctor registered successfully! Generated ID: ${created.doctorId}`, 'success');
            closeModal('add-doctor-modal');
            form.reset();
            loadDoctors();
        } catch (error) {
            console.error('Registration failed:', error);
            showToast(error.message || 'Registration failed. Check entered fields.', 'error');
        }
    });
}

function openAddDoctorModal() {
    ['doc-name', 'doc-dob', 'doc-gender', 'doc-spec', 'doc-qual', 'doc-phone', 'doc-email', 'doc-joining'].forEach(hideFieldError);
    const form = document.getElementById('doctor-reg-form');
    if (form) form.reset();
    document.getElementById('doc-age').value = '';
    // Default joining date to today
    document.getElementById('doc-joining').value = new Date().toISOString().split('T')[0];
    openModal('add-doctor-modal');
}

/**
 * Single duty-status control: toggles between On Duty and Off Duty.
 * Off-duty doctors remain registered but cannot receive new assignments.
 */
async function toggleDutyStatus(doctorId) {
    try {
        const updated = await api.post(`/doctors/${doctorId}/toggle-duty`, {});
        const statusText = updated.dutyStatus === 'ON_DUTY' ? '🟢 ON DUTY' : '🔴 OFF DUTY';
        showToast(`Dr. ${updated.doctorName} (${doctorId}) is now ${statusText}`, 'success');
        loadDoctors();
    } catch (error) {
        console.error('Failed to toggle duty status:', error);
        showToast(error.message || 'Failed to toggle duty status', 'error');
    }
}

/**
 * Deactivate Doctor: represents a doctor who has resigned or permanently left the hospital.
 * Deactivated doctors disappear from active doctor lists and assignment options.
 * Historical consultation and patient records remain strictly preserved.
 */
async function deactivateDoctor(doctorId, doctorName) {
    const reason = prompt(
        `PERMANENT DEACTIVATION / RESIGNATION NOTICE:\n\n` +
        `Are you sure you want to deactivate ${doctorName} (${doctorId})?\n` +
        `This represents a doctor who has resigned or permanently left the hospital.\n` +
        `They will be removed from all active rosters and cannot receive new consultations.\n` +
        `All historical consultation and patient records will be strictly preserved in MySQL.\n\n` +
        `Please enter the reason for deactivation/resignation:`,
        'Doctor resigned from hospital service'
    );

    if (reason === null) return; // User cancelled

    try {
        await api.delete(`/doctors/${doctorId}`);
        showToast(`Doctor ${doctorId} has been permanently deactivated. Historical records preserved.`, 'info');
        loadDoctors();
    } catch (error) {
        console.error('Failed to deactivate doctor:', error);
        showToast(error.message || 'Failed to deactivate doctor', 'error');
    }
}

function setupFilters() {
    const searchInput = document.getElementById('doctor-search');
    const specFilter = document.getElementById('spec-filter');

    if (searchInput) {
        searchInput.addEventListener('input', () => {
            renderDoctorsTable(filterDoctors());
        });
    }
    if (specFilter) {
        specFilter.addEventListener('change', () => {
            renderDoctorsTable(filterDoctors());
        });
    }
}
