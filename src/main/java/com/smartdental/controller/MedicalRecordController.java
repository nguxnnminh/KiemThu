package com.smartdental.controller;

import com.smartdental.entity.Employee;
import com.smartdental.enums.Role;
import com.smartdental.repository.TreatmentSessionRepository;
import com.smartdental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MedicalRecordController {

    private final TreatmentSessionRepository treatmentSessionRepository;
    private final UserRepository userRepository;

    @GetMapping("/clinical/medical-records")
    public String list(@RequestParam(required = false) String keyword,
                       Authentication authentication,
                       Model model) {
        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.DOCTOR.name()));
        Long doctorId = null;
        if (isDoctor) {
            Employee doctor = userRepository.findByUsernameIgnoreCaseFetchEmployeeAndPatient(authentication.getName())
                    .map(com.smartdental.entity.User::getEmployee)
                    .orElse(null);
            doctorId = doctor != null ? doctor.getId() : -1L;
        }
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        model.addAttribute("records", treatmentSessionRepository.findMedicalRecordRows(kw, doctorId));
        model.addAttribute("keyword", keyword);
        return "clinical/medical-records";
    }
}
