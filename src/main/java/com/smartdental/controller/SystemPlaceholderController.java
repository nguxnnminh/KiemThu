package com.smartdental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Cac man hinh placeholder cho menu chua trien khai o cac nhom chuc nang sau.
 * Tat ca tro ve trang "Chuc nang se duoc trien khai o phan sau".
 */
@Controller
public class SystemPlaceholderController {

    @GetMapping({
            "/coming-soon",
            "/account/profile",
            "/account/audit-logs"
    })
    public String comingSoon(Model model) {
        model.addAttribute("message", "Chức năng này sẽ được triển khai ở phần sau.");
        return "common/coming-soon";
    }
}
