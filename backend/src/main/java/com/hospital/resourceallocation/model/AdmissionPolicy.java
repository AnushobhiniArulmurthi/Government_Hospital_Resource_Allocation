package com.hospital.resourceallocation.model;

/**
 * Abstraction Interface: Defines the business policy rule contract for patient admission.
 */
public interface AdmissionPolicy {

    boolean canAdmit(PatientVisit visit, Bed bed, Doctor doctor);

    String getPolicyRationale(PatientVisit visit, Bed bed, Doctor doctor);
}
