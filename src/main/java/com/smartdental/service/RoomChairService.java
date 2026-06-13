package com.smartdental.service;

import com.smartdental.dto.form.ChairForm;
import com.smartdental.dto.form.RoomForm;
import com.smartdental.entity.Chair;
import com.smartdental.entity.Room;
import com.smartdental.enums.CodePrefix;
import com.smartdental.enums.RoomStatus;
import com.smartdental.exception.BusinessException;
import com.smartdental.repository.ChairRepository;
import com.smartdental.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Nghiep vu quan ly phong va ghe nha khoa (UC2.2b - Quan ly phong & ghe).
 */
@Service
@RequiredArgsConstructor
public class RoomChairService {

    private final RoomRepository roomRepository;
    private final ChairRepository chairRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final AuditLogService auditLogService;

    // ---------- Phong ----------

    @Transactional(readOnly = true)
    public Page<Room> searchRooms(String keyword, RoomStatus status, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return roomRepository.search(kw, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Room> findActiveRooms() {
        return roomRepository.findByStatus(RoomStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Chair> findActiveChairs() {
        return chairRepository.findByStatusFetchRoom(RoomStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay phong kham."));
    }

    @Transactional
    public Room createRoom(RoomForm form) {
        validateRoom(form, null);

        Room room = new Room();
        room.setRoomCode(codeGeneratorService.nextCode(CodePrefix.ROOM));
        applyRoomForm(room, form);

        roomRepository.save(room);
        auditLogService.log("CREATE_ROOM", "Room", room.getRoomCode(), "Tao phong kham " + room.getName());
        return room;
    }

    @Transactional
    public Room updateRoom(RoomForm form) {
        Room room = getRoomById(form.getId());
        validateRoom(form, room);

        applyRoomForm(room, form);
        roomRepository.save(room);
        auditLogService.log("UPDATE_ROOM", "Room", room.getRoomCode(), "Cap nhat phong kham " + room.getName());
        return room;
    }

    @Transactional
    public void deactivateRoom(Long id) {
        Room room = getRoomById(id);
        if (chairRepository.countByRoomIdAndStatus(id, RoomStatus.ACTIVE) > 0) {
            throw new BusinessException("Khong the ngung su dung phong con ghe dang hoat dong.");
        }
        room.setStatus(RoomStatus.INACTIVE);
        roomRepository.save(room);
        auditLogService.log("DEACTIVATE_ROOM", "Room", room.getRoomCode(), "Ngung su dung phong kham " + room.getName());
    }

    @Transactional
    public void activateRoom(Long id) {
        Room room = getRoomById(id);
        room.setStatus(RoomStatus.ACTIVE);
        roomRepository.save(room);
        auditLogService.log("ACTIVATE_ROOM", "Room", room.getRoomCode(), "Kich hoat lai phong kham " + room.getName());
    }

    private void applyRoomForm(Room room, RoomForm form) {
        room.setName(form.getName().trim());
        room.setDescription(form.getDescription());
        if (form.getStatus() != null) {
            room.setStatus(form.getStatus());
        }
    }

    private void validateRoom(RoomForm form, Room existing) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Vui long nhap ten phong kham.");
        }
        Long currentId = existing == null ? null : existing.getId();
        if (currentId == null) {
            if (roomRepository.existsByNameIgnoreCase(form.getName().trim())) {
                throw new BusinessException("Ten phong kham da ton tai.");
            }
        } else if (roomRepository.existsByNameIgnoreCaseAndIdNot(form.getName().trim(), currentId)) {
            throw new BusinessException("Ten phong kham da ton tai.");
        }
    }

    // ---------- Ghe ----------

    @Transactional(readOnly = true)
    public Page<Chair> searchChairs(String keyword, Long roomId, RoomStatus status, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return chairRepository.search(kw, roomId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Chair getChairById(Long id) {
        return chairRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Khong tim thay ghe nha khoa."));
    }

    @Transactional
    public Chair createChair(ChairForm form) {
        Room room = getRoomById(form.getRoomId());
        validateChair(form, null);

        Chair chair = new Chair();
        chair.setChairCode(codeGeneratorService.nextCode(CodePrefix.CHAIR));
        chair.setRoom(room);
        applyChairForm(chair, form);

        chairRepository.save(chair);
        auditLogService.log("CREATE_CHAIR", "Chair", chair.getChairCode(),
                "Tao ghe " + chair.getName() + " trong phong " + room.getName());
        return chair;
    }

    @Transactional
    public Chair updateChair(ChairForm form) {
        Chair chair = getChairById(form.getId());
        Room room = getRoomById(form.getRoomId());
        validateChair(form, chair);

        chair.setRoom(room);
        applyChairForm(chair, form);
        chairRepository.save(chair);
        auditLogService.log("UPDATE_CHAIR", "Chair", chair.getChairCode(), "Cap nhat ghe " + chair.getName());
        return chair;
    }

    @Transactional
    public void deactivateChair(Long id) {
        Chair chair = getChairById(id);
        chair.setStatus(RoomStatus.INACTIVE);
        chairRepository.save(chair);
        auditLogService.log("DEACTIVATE_CHAIR", "Chair", chair.getChairCode(), "Ngung su dung ghe " + chair.getName());
    }

    @Transactional
    public void activateChair(Long id) {
        Chair chair = getChairById(id);
        chair.setStatus(RoomStatus.ACTIVE);
        chairRepository.save(chair);
        auditLogService.log("ACTIVATE_CHAIR", "Chair", chair.getChairCode(), "Kich hoat lai ghe " + chair.getName());
    }

    private void applyChairForm(Chair chair, ChairForm form) {
        chair.setName(form.getName().trim());
        chair.setDescription(form.getDescription());
        if (form.getStatus() != null) {
            chair.setStatus(form.getStatus());
        }
    }

    private void validateChair(ChairForm form, Chair existing) {
        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Vui long nhap ten ghe.");
        }
        if (form.getRoomId() == null) {
            throw new BusinessException("Vui long chon phong kham.");
        }
        Long currentId = existing == null ? null : existing.getId();
        if (currentId == null) {
            if (chairRepository.existsByNameIgnoreCaseAndRoomId(form.getName().trim(), form.getRoomId())) {
                throw new BusinessException("Ten ghe da ton tai trong phong nay.");
            }
        } else if (chairRepository.existsByNameIgnoreCaseAndRoomIdAndIdNot(form.getName().trim(), form.getRoomId(), currentId)) {
            throw new BusinessException("Ten ghe da ton tai trong phong nay.");
        }
    }
}
