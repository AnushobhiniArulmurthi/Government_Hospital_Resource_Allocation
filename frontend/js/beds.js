/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Bed Management Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadBeds();
    setupBedForm();
    setupBedFilter();
});

let bedsList = [];

async function loadBeds() {
    try {
        bedsList = await api.get('/beds');
        renderBeds(bedsList);
    } catch (error) {
        console.error('Failed to load beds:', error);
    }
}

function renderBeds(beds) {
    const container = document.getElementById('beds-grid-container');
    if (!container) return;

    if (!beds || beds.length === 0) {
        container.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;">No beds registered in system.</div>`;
        return;
    }

    container.innerHTML = beds.map(bed => {
        const isAvail = bed.status === 'AVAILABLE';
        const cardClass = isAvail ? 'card-green' : 'card-red';
        const statusBadge = isAvail
            ? `<span class="badge badge-available">AVAILABLE</span>`
            : `<span class="badge badge-occupied">OCCUPIED</span>`;

        return `
            <div class="metric-card ${cardClass}" style="background-color: #fff; padding: 20px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px;">
                    <span class="badge badge-info">${bed.bedType} WARD</span>
                    ${statusBadge}
                </div>
                <div style="font-size: 20px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px;">
                    ${bed.bedNumber}
                </div>
                <div class="cell-muted" style="font-size: 12px; margin-bottom: 10px;">
                    ${bed.floorWard} (ID: ${bed.bedId})
                </div>
                <div style="font-size: 12.5px; border-top: 1px solid var(--border-subtle); padding-top: 10px;">
                    ${isAvail 
                        ? '<span style="color: var(--secondary); font-weight:600;">🟢 Available for Automatic Allocation</span><div class="cell-muted" style="font-size:11px; margin-top:2px;">Continuously allocated to waiting patients & treatment plans.</div>' 
                        : `<span style="color:#b91c1c; font-weight:600;">🔴 Inpatient Occupied</span><div style="font-size:12px; margin-top:2px;">Patient: <strong>${bed.currentPatientId || '-'}</strong> (Visit: ${bed.currentVisitId || '-'})</div><div class="cell-muted" style="font-size:11px; margin-top:2px;">Automatically released upon clinical discharge.</div>`
                    }
                </div>
            </div>
        `;
    }).join('');
}

function setupBedForm() {
    const form = document.getElementById('add-bed-form');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
            bedId: document.getElementById('bed-id').value,
            bedNumber: document.getElementById('bed-number').value,
            bedType: document.getElementById('bed-type').value,
            hospitalId: document.getElementById('bed-hospital').value,
            floorWard: document.getElementById('bed-ward').value,
            dailyCharge: 0.0
        };

        try {
            await api.post('/beds', data);
            showToast(`Bed ${data.bedNumber} added successfully`, 'success');
            closeModal('add-bed-modal');
            form.reset();
            loadBeds();
        } catch (error) {
            console.error('Failed to add bed:', error);
        }
    });
}

function openAllocateBedModal(bedId, bedNumber) {
    document.getElementById('alloc-bed-id').value = bedId;
    document.getElementById('alloc-bed-title').textContent = `Allocate Bed ${bedNumber} to Patient`;
    openModal('allocate-bed-modal');
}

async function handleAllocateBedSubmit(e) {
    e.preventDefault();
    const bedId = document.getElementById('alloc-bed-id').value;
    const patientId = document.getElementById('alloc-patient-id').value;
    const visitId = document.getElementById('alloc-visit-id').value;

    try {
        await api.post('/beds/allocate', { bedId, patientId, visitId });
        showToast(`Bed allocated successfully!`, 'success');
        closeModal('allocate-bed-modal');
        loadBeds();
    } catch (error) {
        console.error('Failed to allocate bed:', error);
    }
}

async function releaseBed(bedId) {
    if (!confirm(`Are you sure you want to release bed ${bedId}?`)) return;
    try {
        await api.post(`/beds/release/${bedId}`, {});
        showToast(`Bed ${bedId} released successfully!`, 'success');
        loadBeds();
    } catch (error) {
        console.error('Failed to release bed:', error);
    }
}

function setupBedFilter() {
    const filter = document.getElementById('bed-type-filter');
    if (filter) {
        filter.addEventListener('change', (e) => {
            const val = e.target.value;
            if (!val) {
                renderBeds(bedsList);
            } else {
                renderBeds(bedsList.filter(b => b.bedType === val));
            }
        });
    }
}
