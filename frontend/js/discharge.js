/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Patient Discharge & Resource Release Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadDischargeHistory();
    setupDischargeForm();
    checkUrlParams();
});

function checkUrlParams() {
    const params = new URLSearchParams(window.location.search);
    const visitId = params.get('visitId');
    if (visitId) {
        const visitInput = document.getElementById('dc-visit-id');
        if (visitInput) visitInput.value = visitId;
    }
}

async function loadDischargeHistory() {
    try {
        const history = await api.get('/admissions/discharge-history');
        renderDischargeTable(history);
    } catch (error) {
        console.error('Failed to load discharge history:', error);
    }
}

function renderDischargeTable(history) {
    const tbody = document.getElementById('discharge-history-body');
    if (!tbody) return;

    if (!history || history.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No discharge records available.</td></tr>`;
        return;
    }

    tbody.innerHTML = history.map(d => `
        <tr>
            <td><strong>${d.dischargeId}</strong></td>
            <td><strong>${d.visitId}</strong> (${d.patientId})</td>
            <td><span class="badge badge-available">Released: ${d.bedId || 'N/A'}</span></td>
            <td><small>${formatDate(d.dischargeDate)}</small></td>
            <td>${d.dischargeSummary || 'Routine Discharge'}</td>
            <td>${d.followUpInstructions || 'Follow-up in OPD after 7 days'}</td>
        </tr>
    `).join('');
}

function setupDischargeForm() {
    const form = document.getElementById('discharge-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
            visitId: document.getElementById('dc-visit-id').value,
            dischargeSummary: document.getElementById('dc-summary').value,
            followUpInstructions: document.getElementById('dc-followup').value,
            consumablesUsed: []
        };

        try {
            await api.post('/admissions/discharge', data);
            showToast('Patient successfully discharged! Bed and equipment released to inventory.', 'success');
            form.reset();
            loadDischargeHistory();
        } catch (error) {
            console.error('Discharge error:', error);
        }
    });
}
