package com.smartdental.repository;

import com.smartdental.entity.ToothTreatmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToothTreatmentHistoryRepository extends JpaRepository<ToothTreatmentHistory, Long> {

    List<ToothTreatmentHistory> findByPatientIdAndToothNumberOrderByUpdatedAtDesc(Long patientId, Integer toothNumber);
}
