package com.smartdental.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Trang loi dung chung: 403 (khong co quyen), 500 (loi he thong), va cac loi khac.
 */
@Controller
public class ErrorPageController implements ErrorController {

    @GetMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }

    @GetMapping("/error/500")
    public String serverError() {
        return "error/500";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == 403) {
                return "error/403";
            }
        }
        model.addAttribute("errorMessage", "Da xay ra loi he thong. Vui long thu lai sau.");
        return "error/500";
    }
}
