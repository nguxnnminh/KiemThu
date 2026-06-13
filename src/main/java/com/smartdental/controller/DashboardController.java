package com.smartdental.controller;

import com.smartdental.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Dashboard chinh, noi dung hien thi tuy theo role.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AuthService authService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String role = authService.getCurrentRole()
                .map(Enum::name)
                .orElse("PATIENT");
        model.addAttribute("role", role);
        return "dashboard/index";
    }
}
