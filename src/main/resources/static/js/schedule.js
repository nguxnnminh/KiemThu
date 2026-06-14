/**
 * Smart Dental - tien ich JS cho man hinh quan ly lich kham (Nhom 2).
 * - Loc danh sach ghe theo phong duoc chon trong form dang ky lich truc bac si (UC2.3).
 * - Hien thi preview phong/ghe (chi xem) cho form dat lich kham (UC2.4) dua tren
 *   bac si + ngay kham + ca kham da chon, tu lich truc da duoc duyet.
 */
(function () {
    function filterChairOptions(roomSelect) {
        var form = roomSelect.closest("form");
        if (!form) {
            return;
        }
        var chairSelect = form.querySelector("[data-chair-select]");
        if (!chairSelect) {
            return;
        }
        var roomId = roomSelect.value;
        var selectedValue = chairSelect.value;
        var hasVisibleSelected = false;

        Array.prototype.forEach.call(chairSelect.options, function (option) {
            if (!option.value) {
                option.hidden = false;
                return;
            }
            var optionRoomId = option.getAttribute("data-room-id");
            var visible = !roomId || optionRoomId === roomId;
            option.hidden = !visible;
            if (visible && option.value === selectedValue) {
                hasVisibleSelected = true;
            }
        });

        if (!hasVisibleSelected) {
            chairSelect.value = "";
        }
    }

    function initRegistrationForm(form) {
        var roomSelect = form.querySelector("[data-chair-room-select]");
        if (!roomSelect) {
            return;
        }
        filterChairOptions(roomSelect);
        roomSelect.addEventListener("change", function () {
            filterChairOptions(roomSelect);
        });
    }

    // Loc ca lam viec theo THU cua ngay da chon (WEEKDAY/WEEKEND/ALL).
    // Logic khop voi WorkShiftDayType.appliesTo: WEEKEND = T7/CN, WEEKDAY = T2-T6, ALL = moi ngay.
    function filterShiftsByDate(form) {
        var dateInput = form.querySelector("[data-appointment-date]");
        var shiftSelect = form.querySelector("[data-shift-select]");
        if (!dateInput || !shiftSelect) {
            return;
        }
        var value = dateInput.value;
        if (!value) {
            // Chua chon ngay: hien tat ca ca
            Array.prototype.forEach.call(shiftSelect.options, function (option) {
                option.hidden = false;
                option.disabled = false;
            });
            return;
        }
        var parts = value.split("-");
        var d = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
        var dow = d.getDay();
        var weekend = dow === 0 || dow === 6;
        var selectedHidden = false;

        Array.prototype.forEach.call(shiftSelect.options, function (option) {
            if (!option.value) {
                option.hidden = false;
                option.disabled = false;
                return;
            }
            var dayType = option.getAttribute("data-day-type") || "ALL";
            var applies = dayType === "ALL"
                || (dayType === "WEEKEND" && weekend)
                || (dayType === "WEEKDAY" && !weekend);
            option.hidden = !applies;
            option.disabled = !applies;
            if (!applies && option.selected) {
                selectedHidden = true;
            }
        });

        if (selectedHidden) {
            shiftSelect.value = "";
            var arrivalInput = form.querySelector("[data-arrival-time]");
            if (arrivalInput) arrivalInput.value = "";
        }
    }

    function updateAppointmentPreview(form) {
        var preview = form.querySelector("[data-room-chair-preview]");
        var previewText = form.querySelector("[data-room-chair-preview-text]");
        if (!preview || !previewText) {
            return;
        }
        var doctorSelect = form.querySelector("[name='doctorId']");
        var dateInput = form.querySelector("[data-appointment-date]");
        var shiftSelect = form.querySelector("[data-shift-select]");

        var doctorId = doctorSelect ? doctorSelect.value : "";
        var date = dateInput ? dateInput.value : "";
        var workShiftId = shiftSelect ? shiftSelect.value : "";

        if (!doctorId || !date || !workShiftId) {
            preview.style.display = "none";
            previewText.value = "";
            return;
        }

        preview.style.display = "";
        previewText.value = "Dang tai...";

        var url = "/schedule/appointments/confirmed-shift?doctorId=" + encodeURIComponent(doctorId)
            + "&date=" + encodeURIComponent(date) + "&workShiftId=" + encodeURIComponent(workShiftId);

        fetch(url, { headers: { "X-Requested-With": "XMLHttpRequest" } })
            .then(function (response) {
                return response.ok ? response.json() : null;
            })
            .then(function (data) {
                if (data && data.room) {
                    previewText.value = data.room + " / " + (data.chair || "-");
                } else {
                    previewText.value = "Bac si chua co lich truc duoc duyet cho ngay va ca kham da chon.";
                }
            })
            .catch(function () {
                previewText.value = "";
            });
    }

    function initAppointmentForm(form) {
        var shiftSelect = form.querySelector("[data-shift-select]");
        var dateInput = form.querySelector("[data-appointment-date]");
        var doctorSelect = form.querySelector("[name='doctorId']");
        if (!shiftSelect && !dateInput) {
            return;
        }

        if (shiftSelect) {
            shiftSelect.addEventListener("change", function () {
                var selected = shiftSelect.options[shiftSelect.selectedIndex];
                var arrivalInput = form.querySelector("[data-arrival-time]");
                // Mac dinh gio den = gio bat dau cua ca; rang buoc trong khung gio ca.
                if (selected && arrivalInput) {
                    var startTime = selected.getAttribute("data-start-time");
                    var endTime = selected.getAttribute("data-end-time");
                    if (startTime) {
                        arrivalInput.min = startTime;
                        if (!arrivalInput.value) {
                            arrivalInput.value = startTime;
                        }
                    }
                    if (endTime) {
                        arrivalInput.max = endTime;
                    }
                }
                updateAppointmentPreview(form);
            });
        }
        if (dateInput) {
            dateInput.addEventListener("change", function () {
                filterShiftsByDate(form);
                updateAppointmentPreview(form);
            });
        }
        if (doctorSelect) {
            doctorSelect.addEventListener("change", function () {
                updateAppointmentPreview(form);
            });
        }

        filterShiftsByDate(form);
        updateAppointmentPreview(form);
    }

    function initForm(form) {
        initRegistrationForm(form);
        initAppointmentForm(form);
    }

    document.addEventListener("change", function (e) {
        if (e.target && e.target.matches("[data-chair-room-select]")) {
            filterChairOptions(e.target);
        }
    });

    // Khi modal duoc mo (template duoc chen vao DOM), khoi tao form ben trong.
    document.addEventListener("click", function (e) {
        if (e.target.closest("[data-modal-target]")) {
            setTimeout(function () {
                document.querySelectorAll(".modal-form").forEach(initForm);
            }, 0);
        }
    });

    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll(".modal-form").forEach(initForm);
    });
})();
