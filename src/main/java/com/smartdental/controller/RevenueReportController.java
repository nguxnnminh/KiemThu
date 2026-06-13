package com.smartdental.controller;

import com.smartdental.enums.PaymentMethod;
import com.smartdental.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * UC3.6 - Bao cao doanh thu.
 */
@Controller
@RequiredArgsConstructor
public class RevenueReportController {

    private final RevenueReportService revenueReportService;

    @GetMapping("/clinical/revenue")
    public String report(@RequestParam(required = false) LocalDate fromDate,
                          @RequestParam(required = false) LocalDate toDate,
                          @RequestParam(required = false) PaymentMethod paymentMethod,
                          Model model) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to = toDate != null ? toDate : LocalDate.now();

        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("methodFilter", paymentMethod);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("summary", revenueReportService.summarize(from, to, paymentMethod));
        model.addAttribute("byMethod", revenueReportService.summarizeByMethod(from, to));
        model.addAttribute("byDay", revenueReportService.summarizeByDay(from, to, paymentMethod));
        model.addAttribute("payments", revenueReportService.findPayments(from, to, paymentMethod));
        return "clinical/revenue-report";
    }
}
