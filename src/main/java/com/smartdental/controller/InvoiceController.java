package com.smartdental.controller;

import com.smartdental.dto.form.InvoiceDiscountForm;
import com.smartdental.dto.form.PaymentForm;
import com.smartdental.dto.form.RefundForm;
import com.smartdental.entity.Invoice;
import com.smartdental.enums.DiscountType;
import com.smartdental.enums.InvoiceStatus;
import com.smartdental.enums.PaymentMethod;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.InvoiceService;
import com.smartdental.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * UC3.5 - Lap hoa don, thu tien, hoan tien, in hoa don.
 */
@Controller
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    @GetMapping("/clinical/invoices")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) InvoiceStatus status,
                        @RequestParam(required = false) LocalDate fromDate,
                        @RequestParam(required = false) LocalDate toDate,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<Invoice> invoices = invoiceService.search(keyword, status, fromDate, toDate, PageRequest.of(page, 20));
        model.addAttribute("invoices", invoices);
        model.addAttribute("stats", invoiceService.stats());
        model.addAttribute("keyword", keyword);
        model.addAttribute("statusFilter", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("statuses", java.util.List.of(InvoiceStatus.UNPAID, InvoiceStatus.PAID, InvoiceStatus.CANCELLED));
        return "clinical/invoices";
    }

    @GetMapping("/clinical/invoices/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("items", invoiceService.getInvoiceItems(invoice.getTreatmentSession().getId()));
        model.addAttribute("payments", paymentService.findByInvoice(id));
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());

        if (!model.containsAttribute("discountForm")) {
            InvoiceDiscountForm discountForm = new InvoiceDiscountForm();
            discountForm.setInvoiceId(id);
            discountForm.setDiscountType(invoice.getDiscountType().name());
            model.addAttribute("discountForm", discountForm);
        }
        if (!model.containsAttribute("paymentForm")) {
            PaymentForm paymentForm = new PaymentForm();
            paymentForm.setInvoiceId(id);
            model.addAttribute("paymentForm", paymentForm);
        }
        if (!model.containsAttribute("refundForm")) {
            RefundForm refundForm = new RefundForm();
            refundForm.setInvoiceId(id);
            model.addAttribute("refundForm", refundForm);
        }
        return "clinical/invoice-detail";
    }

    @PostMapping("/clinical/invoices/{id}/discount")
    public String discount(@PathVariable Long id, @ModelAttribute InvoiceDiscountForm form, RedirectAttributes ra) {
        form.setInvoiceId(id);
        try {
            invoiceService.applyDiscount(form);
            ra.addFlashAttribute("successMessage", "Cap nhat giam gia thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/clinical/invoices/" + id;
    }

    @PostMapping("/clinical/invoices/{id}/payment")
    public String collect(@PathVariable Long id, @ModelAttribute PaymentForm form, RedirectAttributes ra) {
        form.setInvoiceId(id);
        try {
            paymentService.collect(form);
            ra.addFlashAttribute("successMessage", "Thu tien thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/clinical/invoices/" + id;
    }

    @PostMapping("/clinical/invoices/{id}/refund")
    public String refund(@PathVariable Long id, @ModelAttribute RefundForm form, RedirectAttributes ra) {
        form.setInvoiceId(id);
        try {
            paymentService.refund(form);
            ra.addFlashAttribute("successMessage", "Hoan tien thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/clinical/invoices/" + id;
    }

    @GetMapping("/clinical/invoices/{id}/print")
    public String print(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("items", invoiceService.getInvoiceItems(invoice.getTreatmentSession().getId()));
        model.addAttribute("payments", paymentService.findByInvoice(id));
        return "clinical/invoice-print";
    }
}
