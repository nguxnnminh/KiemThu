package com.smartdental.repository;

import com.smartdental.entity.DentalToothStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DentalToothStatusRepository extends JpaRepository<DentalToothStatus, Long> {

    Optional<DentalToothStatus> findByPatientIdAndToothNumber(Long patientId, Integer toothNumber);

    List<DentalToothStatus> findByPatientId(Long patientId);
}
