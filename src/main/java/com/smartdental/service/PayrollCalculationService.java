package com.smartdental.service;

import com.smartdental.entity.ComplexCaseCoefficient;
import com.smartdental.entity.DoctorShiftRegistration;
import com.smartdental.entity.Employee;
import com.smartdental.entity.PayrollItem;
import com.smartdental.entity.TreatmentSession;
import com.smartdental.entity.WorkShift;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.ComplexCaseCoefficientRepository;
import com.smartdental.repository.DoctorHourlyRateRepository;
import com.smartdental.repository.DoctorShiftRegistrationRepository;
import com.smartdental.repository.TreatmentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Tinh toan cac dong luong (PayrollItem) cho mot bac si trong mot thang,
 * dua tren lich truc da duyet (UC2.3), he so ca (UC4.2), he so ca phuc tap
 * da duyet (UC4.3), hoc vi (UC1.2) va muc tien/gio (UC4.1).
 */
@Service
@RequiredArgsConstructor
public class PayrollCalculationService {

    private final DoctorShiftRegistrationRepository doctorShiftRegistrationRepository;
    private final TreatmentSessionRepository treatmentSessionRepository;
    private final ComplexCaseCoefficientRepository complexCaseCoefficientRepository;
    private final DoctorHourlyRateRepository hourlyRateRepository;
    private final ShiftCoefficientService shiftCoefficientService;

    /**
     * Kiem tra co he so ca phuc tap nao dang cho duyet trong thang cua bac si.
     * Neu co, khong cho phep lap/tinh lai phieu luong.
     */
    @Transactional(readOnly = true)
    public boolean hasPendingComplexCoefficient(Long doctorId, YearMonth period) {
        LocalDate fromDate = period.atDay(1);
        LocalDate toDate = period.atEndOfMonth();
        return complexCaseCoefficientRepository.existsPendingForDoctorInPeriod(doctorId, fromDate, toDate);
    }

    /**
     * Tinh danh sach dong luong theo tung phien kham da hoan tat trong thang.
     * Khong luu vao DB - chi tra ve danh sach PayrollItem chua gan slip.
     */
    @Transactional(readOnly = true)
    public List<PayrollItem> calculateItems(Employee doctor, YearMonth period) {
        LocalDate fromDate = period.atDay(1);
        LocalDate toDate = period.atEndOfMonth();

        // Luong bac si tinh theo cac CA TRUC da duoc duyet (APPROVED) trong thang.
        // So gio lay theo do dai ca; he so benh nhan cong them tu cac phien kham
        // hoan tat gan voi ca truc do (he so ca phuc tap da duyet).
        List<DoctorShiftRegistration> registrations =
                doctorShiftRegistrationRepository.findApprovedByDoctorAndPeriod(doctor.getId(), fromDate, toDate);

        if (registrations.isEmpty()) {
            throw new BusinessException("Bac si chua co ca truc nao duoc duyet trong thang nay.");
        }

        BigDecimal hourlyRate = hourlyRateRepository.findActiveOnDate(LocalDate.now())
                .orElseThrow(() -> new BusinessException("Chua thiet lap muc tien luong/gio dang ap dung."))
                .getHourlyRate();

        if (doctor.getDegree() == null) {
            throw new BusinessException("Bac si chua duoc thiet lap hoc vi de tinh he so luong.");
        }
        BigDecimal degreeCoefficient = doctor.getDegree().getCoefficient();

        List<PayrollItem> items = new ArrayList<>();
        for (DoctorShiftRegistration registration : registrations) {
            items.add(calculateItem(registration, hourlyRate, degreeCoefficient));
        }
        return items;
    }

    private PayrollItem calculateItem(TreatmentSession session, BigDecimal hourlyRate, BigDecimal degreeCoefficient) {
        BigDecimal totalHours = computeTotalHours(session);
        WorkShift workShift = session.getAppointment() != null ? session.getAppointment().getWorkShift() : null;
        DoctorShiftRegistration registration = session.getAppointment() != null ? session.getAppointment().getDoctorShiftRegistration() : null;
        if (workShift == null && registration != null) {
            workShift = registration.getWorkShift();
        }

        BigDecimal shiftCoefficient = workShift != null
                ? shiftCoefficientService.getCoefficientForShiftOnDate(workShift.getId(), session.getExaminationDate())
                : BigDecimal.ONE;
        BigDecimal patientCoefficient = computePatientCoefficient(session);

        BigDecimal convertedHours = totalHours.multiply(shiftCoefficient.add(patientCoefficient))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = convertedHours.multiply(degreeCoefficient).multiply(hourlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        PayrollItem item = new PayrollItem();
        item.setTreatmentSession(session);
        item.setDoctorShiftRegistration(registration);
        item.setWorkDate(session.getExaminationDate());
        item.setShiftNameSnapshot(workShift != null ? workShift.getName() : "Phiên khám");
        item.setTotalHours(totalHours);
        item.setShiftCoefficientSnapshot(shiftCoefficient);
        item.setPatientCoefficientSnapshot(patientCoefficient);
        item.setConvertedHours(convertedHours);
        item.setDegreeCoefficientSnapshot(degreeCoefficient);
        item.setHourlyRateSnapshot(hourlyRate);
        item.setAmount(amount);
        return item;
    }

    private PayrollItem calculateItem(DoctorShiftRegistration registration, BigDecimal hourlyRate, BigDecimal degreeCoefficient) {
        BigDecimal totalHours = computeTotalHours(registration);
        BigDecimal shiftCoefficient = shiftCoefficientService.getCoefficientForShiftOnDate(
                registration.getWorkShift().getId(), registration.getWorkDate());
        BigDecimal patientCoefficient = computePatientCoefficient(registration);

        BigDecimal convertedHours = totalHours.multiply(shiftCoefficient.add(patientCoefficient))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal amount = convertedHours.multiply(degreeCoefficient).multiply(hourlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        PayrollItem item = new PayrollItem();
        item.setDoctorShiftRegistration(registration);
        item.setWorkDate(registration.getWorkDate());
        item.setShiftNameSnapshot(registration.getWorkShift().getName());
        item.setTotalHours(totalHours);
        item.setShiftCoefficientSnapshot(shiftCoefficient);
        item.setPatientCoefficientSnapshot(patientCoefficient);
        item.setConvertedHours(convertedHours);
        item.setDegreeCoefficientSnapshot(degreeCoefficient);
        item.setHourlyRateSnapshot(hourlyRate);
        item.setAmount(amount);
        return item;
    }

    private BigDecimal computeTotalHours(DoctorShiftRegistration registration) {
        Duration duration = Duration.between(registration.getWorkShift().getStartTime(), registration.getWorkShift().getEndTime());
        return BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeTotalHours(TreatmentSession session) {
        if (session.getAppointment() != null
                && session.getAppointment().getStartTime() != null
                && session.getAppointment().getEndTime() != null
                && session.getAppointment().getEndTime().isAfter(session.getAppointment().getStartTime())) {
            Duration duration = Duration.between(session.getAppointment().getStartTime(), session.getAppointment().getEndTime());
            return BigDecimal.valueOf(duration.toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computePatientCoefficient(TreatmentSession session) {
        BigDecimal total = BigDecimal.ZERO;
        for (ComplexCaseCoefficient coefficient : complexCaseCoefficientRepository.findApprovedBySessionId(session.getId())) {
            total = total.add(coefficient.getCoefficient());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computePatientCoefficient(DoctorShiftRegistration registration) {
        List<TreatmentSession> sessions = treatmentSessionRepository.findCompletedByDoctorShiftRegistrationId(registration.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (TreatmentSession session : sessions) {
            for (ComplexCaseCoefficient coefficient : complexCaseCoefficientRepository.findApprovedBySessionId(session.getId())) {
                total = total.add(coefficient.getCoefficient());
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
