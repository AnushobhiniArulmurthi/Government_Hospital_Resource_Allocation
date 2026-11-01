/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Treatment Plan Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadTreatmentPlans();
    setupTreatmentPlanForm();
});

async function loadTreatmentPlans() {
    try {
        const plans = await api.get('/treatment-plans');
        renderTreatmentPlans(plans);
    } catch (error) {
        console.error('Failed to load treatment plans:', error);
    }
}

function renderTreatmentPlans(plans) {
    const tbody = document.getElementById('treatment-plans-body');
    if (!tbody) return;

    if (!plans || plans.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No treatment plans recorded.</td></tr>`;
        return;
    }

    tbody.innerHTML = plans.map(tp => `
        <tr>
            <td><strong>${tp.treatmentPlanId}</strong></td>
            <td>${tp.visitId} (${tp.patientId})</td>
            <td><strong>${tp.doctorId}</strong></td>
            <td><span class="badge badge-info">${tp.treatmentCategory}</span></td>
            <td>${tp.requiredBedType || 'NO_BED'}</td>
            <td>${tp.requiredResources || 'None'}</td>
            <td class="cell-actions">
                <button class="btn btn-outline btn-sm" onclick="openEditPlanModal('${tp.treatmentPlanId}', '${tp.visitId}', '${tp.doctorId}', '${tp.treatmentCategory}', '${tp.requiredBedType}', '${tp.doctorNotes || ''}')">
                    Update Plan
                </button>
            </td>
        </tr>
    `).join('');
}

function setupTreatmentPlanForm() {
    const form = document.getElementById('treatment-plan-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const selectedResources = [];
        document.querySelectorAll('input[name="tpResources"]:checked').forEach(cb => {
            selectedResources.push(cb.value);
        });

        const selectedDiagnostics = [];
        document.querySelectorAll('input[name="tpDiagnostics"]:checked').forEach(cb => {
            selectedDiagnostics.push(cb.value);
        });

        const data = {
            visitId: document.getElementById('tp-visit-id').value,
            doctorId: document.getElementById('tp-doctor-id').value,
            treatmentCategory: document.getElementById('tp-category').value,
            requiredBedType: document.getElementById('tp-bed-type').value,
            requiredDoctorSpecialization: document.getElementById('tp-specialization')?.value || null,
            requiredResources: selectedResources,
            diagnosticTests: selectedDiagnostics,
            doctorNotes: document.getElementById('tp-notes').value
        };

        try {
            const plan = await api.post('/treatment-plans', data);
            showToast(`Treatment plan created successfully: ${plan.treatmentPlanId}`, 'success');
            closeModal('treatment-plan-modal');
            form.reset();
            loadTreatmentPlans();
        } catch (error) {
            console.error('Treatment plan error:', error);
        }
    });
}

function openEditPlanModal(planId, visitId, doctorId, category, bedType, notes) {
    document.getElementById('tp-visit-id').value = visitId;
    document.getElementById('tp-doctor-id').value = doctorId;
    document.getElementById('tp-category').value = category;
    document.getElementById('tp-bed-type').value = bedType || 'NO_BED';
    document.getElementById('tp-notes').value = notes;
    openModal('treatment-plan-modal');
}
