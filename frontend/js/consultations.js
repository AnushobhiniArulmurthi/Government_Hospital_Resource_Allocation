/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Doctor Consultation Module Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadConsultations();
    loadConsultationQueue();
    setupConsultationForm();
});

async function loadConsultations() {
    try {
        const consultations = await api.get('/consultations');
        renderConsultationsTable(consultations);
    } catch (error) {
        console.error('Failed to load consultations:', error);
    }
}

async function loadConsultationQueue() {
    try {
        const queue = await api.get('/queues/consultation');
        renderQueueTable(queue);
    } catch (error) {
        console.error('Failed to load consultation queue:', error);
    }
}

function renderConsultationsTable(consultations) {
    const tbody = document.getElementById('consultations-table-body');
    if (!tbody) return;

    if (!consultations || consultations.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No clinical consultations recorded.</td></tr>`;
        return;
    }

    tbody.innerHTML = consultations.map(c => `
        <tr>
            <td><strong>${c.consultationId}</strong></td>
            <td>${c.visitId} (${c.patientId})</td>
            <td><strong>${c.doctorId}</strong></td>
            <td>${getStatusBadge(c.status)}</td>
            <td>${c.symptoms || '-'}</td>
            <td>${c.prescription || '-'}</td>
            <td class="cell-actions">
                ${c.status !== 'COMPLETED' ? `
                    <button class="btn btn-primary btn-sm" onclick="openConsultationNotesModal('${c.visitId}', '${c.doctorId}', '${c.symptoms || ''}', '${c.doctorObservations || ''}', '${c.prescription || ''}')">
                        Examine & Prescribe
                    </button>
                ` : '<span class="cell-muted">Completed</span>'}
            </td>
        </tr>
    `).join('');
}

function renderQueueTable(queue) {
    const tbody = document.getElementById('consultation-queue-body');
    if (!tbody) return;

    if (!queue || queue.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No patients waiting in consultation queue.</td></tr>`;
        return;
    }

    tbody.innerHTML = queue.map(q => `
        <tr>
            <td><strong>${q.queueId}</strong></td>
            <td>${q.visitId} (${q.patientId})</td>
            <td>${getSeverityBadge(q.severityLevel)}</td>
            <td><span class="badge badge-info">${q.requiredSpecialization}</span></td>
            <td class="cell-muted">${formatDateTime(q.arrivalTime)}</td>
            <td class="cell-actions">
                <button class="btn btn-outline btn-sm" onclick="openAssignDoctorModal('${q.visitId}', '${q.requiredSpecialization}')">
                    Assign Doctor
                </button>
            </td>
        </tr>
    `).join('');
}

function openAssignDoctorModal(visitId, spec) {
    document.getElementById('assign-visit-id').value = visitId;
    document.getElementById('assign-modal-title').textContent = `Assign Doctor for Visit ${visitId} (${spec})`;
    
    // Load available matching doctors
    api.get(`/doctors/available?specialization=${encodeURIComponent(spec)}`).then(docs => {
        const select = document.getElementById('assign-doctor-select');
        if (!docs || docs.length === 0) {
            select.innerHTML = `<option value="">No available doctors for ${spec} right now</option>`;
        } else {
            select.innerHTML = docs.map(d => `
                <option value="${d.doctorId}">${d.doctorId} - ${d.doctorName} (${d.specialization})</option>
            `).join('');
        }
    });

    openModal('assign-doctor-modal');
}

async function handleAssignDoctorSubmit(e) {
    e.preventDefault();
    const visitId = document.getElementById('assign-visit-id').value;
    const doctorId = document.getElementById('assign-doctor-select').value;
    if (!doctorId) {
        showToast('Please select a valid available doctor', 'error');
        return;
    }

    try {
        await api.post('/consultations/assign', { visitId, doctorId });
        showToast('Doctor assigned successfully!', 'success');
        closeModal('assign-doctor-modal');
        loadConsultations();
        loadConsultationQueue();
    } catch (error) {
        console.error('Assignment error:', error);
    }
}

function openConsultationNotesModal(visitId, doctorId, symptoms, obs, rx) {
    document.getElementById('notes-visit-id').value = visitId;
    document.getElementById('notes-doctor-id').value = doctorId;
    document.getElementById('cons-symptoms').value = symptoms;
    document.getElementById('cons-observations').value = obs;
    document.getElementById('cons-prescription').value = rx;
    openModal('consultation-notes-modal');
}

function setupConsultationForm() {
    const form = document.getElementById('consultation-notes-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
            visitId: document.getElementById('notes-visit-id').value,
            doctorId: document.getElementById('notes-doctor-id').value,
            symptoms: document.getElementById('cons-symptoms').value,
            doctorObservations: document.getElementById('cons-observations').value,
            prescription: document.getElementById('cons-prescription').value,
            status: document.getElementById('cons-mark-complete').checked ? 'COMPLETED' : 'IN_PROGRESS'
        };

        try {
            await api.post('/consultations/update', data);
            showToast('Consultation notes saved successfully!', 'success');
            closeModal('consultation-notes-modal');
            loadConsultations();
            loadConsultationQueue();
        } catch (error) {
            console.error('Failed to save notes:', error);
        }
    });
}
