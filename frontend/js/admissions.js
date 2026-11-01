/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Inpatient Admission Management Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadAdmissions();
    setupAdmissionForm();
});

let admissionsList = [];

async function loadAdmissions() {
    try {
        const admissions = await api.get('/admissions');
        admissionsList = admissions || [];
        renderAdmissionsTable(admissionsList);
    } catch (error) {
        console.error('Failed to load admissions:', error);
    }
}

function renderAdmissionsTable(admissions) {
    const tbody = document.getElementById('admissions-table-body');
    if (!tbody) return;

    if (!admissions || admissions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No inpatient admissions recorded yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = admissions.map(a => `
        <tr>
            <td><strong>${a.admissionId}</strong></td>
            <td><strong>${a.visitId}</strong> (${a.patientId})</td>
            <td><span class="badge badge-info">${a.bedId || 'Auto Assigned'}</span></td>
            <td>${a.admittingDoctorId}</td>
            <td><small>${formatDate(a.admissionTime)}</small></td>
            <td>${getStatusBadge(a.status)}</td>
            <td class="cell-actions">
                ${a.status === 'ADMITTED' ? `
                    <a href="discharge.html?visitId=${a.visitId}" class="btn btn-secondary btn-sm">Discharge</a>
                ` : `
                    <span class="cell-muted">Discharged</span>
                `}
            </td>
        </tr>
    `).join('');
}

function setupAdmissionForm() {
    const form = document.getElementById('new-admission-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
            visitId: document.getElementById('adm-visit-id').value,
            doctorId: document.getElementById('adm-doctor-id').value,
            bedType: document.getElementById('adm-bed-type').value,
            hospitalId: 'H-001',
            notes: document.getElementById('adm-notes').value,
            requiredResources: []
        };

        try {
            const result = await api.post('/admissions/admit', data);
            showToast(`Patient successfully admitted! Bed allocated: ${result.bedId}`, 'success');
            closeModal('new-admission-modal');
            form.reset();
            loadAdmissions();
        } catch (error) {
            console.error('Admission error:', error);
        }
    });
}
