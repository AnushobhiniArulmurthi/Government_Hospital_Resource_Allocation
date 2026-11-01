/**
 * Government Hospital Resource Allocation System (GHRAS)
 * Dashboard Real-Time Statistics Script
 */

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardStats();
    // Auto refresh every 15 seconds
    setInterval(loadDashboardStats, 15000);
});

async function loadDashboardStats() {
    try {
        const stats = await api.get('/dashboard/stats');
        renderStats(stats);
    } catch (error) {
        console.error('Failed to load dashboard metrics:', error);
    }
}

function renderStats(s) {
    // Patient Stats
    setText('stat-total-patients', s.totalRegisteredPatients);
    setText('stat-critical-patients', s.criticalPatients);
    setText('stat-serious-patients', s.seriousPatients);
    setText('stat-minor-patients', s.minorPatients);
    setText('stat-waiting-consultation', s.waitingForConsultation);
    setText('stat-under-treatment', s.underTreatment);
    setText('stat-admitted-patients', s.admittedPatients);
    setText('stat-discharged-patients', s.dischargedPatients);

    // Doctor Stats
    setText('stat-total-doctors', s.totalDoctors);
    setText('stat-available-doctors', s.availableDoctors);
    setText('stat-consulting-doctors', s.consultingDoctors);
    setText('stat-assigned-inpatients', s.assignedToInpatients);
    setText('stat-offduty-doctors', s.offDutyOrLeave);

    // Bed Stats
    setText('stat-total-beds', s.totalBeds);
    setText('stat-available-beds', s.availableBeds);
    setText('stat-occupied-beds', s.occupiedBeds);
    setText('stat-gen-beds', s.availableGeneralBeds);
    setText('stat-icu-beds', s.availableIcuBeds);
    setText('stat-emg-beds', s.availableEmergencyBeds);

    // Resource Stats
    setText('stat-avail-equipment', s.availableEquipment);
    setText('stat-avail-diagnostics', s.availableDiagnosticServices);
    setText('stat-supply-stock', s.medicalSupplyStock);
    setText('stat-pending-requests', s.pendingResourceRequests);

    // Referral Stats
    setText('stat-total-referrals', s.totalReferrals);
    setText('stat-pending-referrals', s.pendingReferrals);
    setText('stat-completed-referrals', s.completedReferrals);
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = value !== undefined && value !== null ? value : 0;
    }
}
