package com.smartdental.controller;

import com.smartdental.dto.form.ChairForm;
import com.smartdental.dto.form.RoomForm;
import com.smartdental.entity.Chair;
import com.smartdental.entity.Room;
import com.smartdental.enums.RoomStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.service.RoomChairService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * UC2.2b - Quan ly danh muc phong & ghe.
 */
@Controller
@RequiredArgsConstructor
public class RoomChairController {

    private static final int PAGE_SIZE = 10;

    private final RoomChairService roomChairService;

    @GetMapping("/schedule/rooms")
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long roomId,
                        @RequestParam(required = false) RoomStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<Room> rooms = roomChairService.searchRooms(keyword, status, pageable);
        Page<Chair> chairs = roomChairService.searchChairs(keyword, roomId, status, pageable);

        model.addAttribute("rooms", rooms);
        model.addAttribute("chairs", chairs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("roomFilter", roomId);
        model.addAttribute("statusFilter", status);
        model.addAttribute("statuses", RoomStatus.values());
        model.addAttribute("activeRooms", roomChairService.findActiveRooms());

        if (!model.containsAttribute("roomForm")) {
            model.addAttribute("roomForm", new RoomForm());
        }
        if (!model.containsAttribute("chairForm")) {
            model.addAttribute("chairForm", new ChairForm());
        }
        return "schedule/rooms";
    }

    // ---------- Phong ----------

    @PostMapping("/schedule/rooms")
    public String createRoom(@ModelAttribute RoomForm form, RedirectAttributes ra) {
        try {
            roomChairService.createRoom(form);
            ra.addFlashAttribute("successMessage", "Them phong kham thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addRoomModal");
            ra.addFlashAttribute("roomForm", form);
        }
        return "redirect:/schedule/rooms";
    }

    @PostMapping("/schedule/rooms/{id}")
    public String updateRoom(@PathVariable Long id, @ModelAttribute RoomForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            roomChairService.updateRoom(form);
            ra.addFlashAttribute("successMessage", "Cap nhat phong kham thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editRoomModal-" + id);
            ra.addFlashAttribute("editRoomForm-" + id, form);
        }
        return "redirect:/schedule/rooms";
    }

    @PostMapping("/schedule/rooms/{id}/deactivate")
    public String deactivateRoom(@PathVariable Long id, RedirectAttributes ra) {
        try {
            roomChairService.deactivateRoom(id);
            ra.addFlashAttribute("successMessage", "Da ngung su dung phong kham.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/rooms";
    }

    @PostMapping("/schedule/rooms/{id}/activate")
    public String activateRoom(@PathVariable Long id, RedirectAttributes ra) {
        try {
            roomChairService.activateRoom(id);
            ra.addFlashAttribute("successMessage", "Da kich hoat lai phong kham.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/rooms";
    }

    // ---------- Ghe ----------

    @PostMapping("/schedule/chairs")
    public String createChair(@ModelAttribute ChairForm form, RedirectAttributes ra) {
        try {
            roomChairService.createChair(form);
            ra.addFlashAttribute("successMessage", "Them ghe nha khoa thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "addChairModal");
            ra.addFlashAttribute("chairForm", form);
        }
        return "redirect:/schedule/rooms";
    }

    @PostMapping("/schedule/chairs/{id}")
    public String updateChair(@PathVariable Long id, @ModelAttribute ChairForm form, RedirectAttributes ra) {
        form.setId(id);
        try {
            roomChairService.updateChair(form);
            ra.addFlashAttribute("successMessage", "Cap nhat ghe nha khoa thanh cong.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("reopenModal", "editChairModal-" + id);
            ra.addFlashAttribute("editChairForm-" + id, form);
        }
        return "redirect:/schedule/rooms";
    }

    @PostMapping("/schedule/chairs/{id}/deactivate")
    public String deactivateChair(@PathVariable Long id, RedirectAttributes ra) {
        try {
            roomChairService.deactivateChair(id);
            ra.addFlashAttribute("successMessage", "Da ngung su dung ghe nha khoa.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/rooms";
    }

    @PostMapping("/schedule/chairs/{id}/activate")
    public String activateChair(@PathVariable Long id, RedirectAttributes ra) {
        try {
            roomChairService.activateChair(id);
            ra.addFlashAttribute("successMessage", "Da kich hoat lai ghe nha khoa.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/schedule/rooms";
    }
}
