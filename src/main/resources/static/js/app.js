/**
 * Smart Dental - tien ich JS dung chung (sidebar toggle, alert auto-hide).
 */
document.addEventListener("DOMContentLoaded", function () {
    var toggleBtn = document.getElementById("sidebarToggle");
    var sidebar = document.getElementById("sidebar");

    var overlay = document.getElementById("sidebarOverlay");

    function closeMobileSidebar() {
        if (sidebar) sidebar.classList.remove("show");
        if (overlay) overlay.classList.remove("show");
    }

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener("click", function () {
            if (window.innerWidth <= 900) {
                var opened = sidebar.classList.toggle("show");
                if (overlay) overlay.classList.toggle("show", opened);
            } else {
                sidebar.classList.toggle("collapsed");
            }
        });
    }

    if (overlay) {
        overlay.addEventListener("click", closeMobileSidebar);
    }

    // Accordion menu: bam tieu de nhom de mo/dong; tu dong dong nhom khac
    var groups = Array.prototype.slice.call(document.querySelectorAll(".sidebar .menu-group"));
    groups.forEach(function (group) {
        var header = group.querySelector("[data-accordion]");
        if (!header) return;
        header.addEventListener("click", function () {
            var wasOpen = group.classList.contains("open");
            groups.forEach(function (g) { g.classList.remove("open"); });
            if (!wasOpen) group.classList.add("open");
        });
    });

    // Tren mobile, bam mot muc menu xong thi dong sidebar
    document.querySelectorAll(".sidebar .menu-list a").forEach(function (link) {
        link.addEventListener("click", function () {
            if (window.innerWidth <= 900) closeMobileSidebar();
        });
    });

    // Ngay hien tai tren hero dashboard
    var heroDate = document.getElementById("heroDate");
    if (heroDate) {
        var days = ["Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"];
        var now = new Date();
        var dd = ("0" + now.getDate()).slice(-2);
        var mm = ("0" + (now.getMonth() + 1)).slice(-2);
        heroDate.textContent = days[now.getDay()] + ", " + dd + "/" + mm + "/" + now.getFullYear();
    }

    // Tu dong an thong bao thanh cong/loi sau vai giay
    document.querySelectorAll(".alert").forEach(function (alertEl) {
        setTimeout(function () {
            alertEl.style.transition = "opacity 0.4s ease";
            alertEl.style.opacity = "0";
            setTimeout(function () {
                alertEl.remove();
            }, 400);
        }, 4000);
    });

    // Tu dong mo lai modal khi co loi nghiep vu sau khi submit form (PRG pattern)
    var reopenId = window.__reopenModal || window.__reopenModal2;
    if (reopenId && window.AppModal) {
        var template = document.getElementById(reopenId);
        if (template) {
            var clone = document.importNode(template.content, true);
            window.AppModal.open(template.getAttribute("data-modal-title") || "", clone);
        }
    }
});

/**
 * Sinh mat khau ngau nhien (8 ky tu, co chu hoa, chu thuong, chu so)
 * va dien vao cac truong mat khau trong form chua nut bam.
 */
function smartDentalGeneratePassword(buttonEl) {
    var lower = "abcdefghijklmnopqrstuvwxyz";
    var upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var digits = "0123456789";
    var all = lower + upper + digits;
    var pwd = upper.charAt(Math.floor(Math.random() * upper.length))
        + lower.charAt(Math.floor(Math.random() * lower.length))
        + digits.charAt(Math.floor(Math.random() * digits.length));
    for (var i = 0; i < 7; i++) {
        pwd += all.charAt(Math.floor(Math.random() * all.length));
    }

    var form = buttonEl.closest("form");
    if (!form) {
        return;
    }
    var pwdField = form.querySelector("[name='password'], [name='newPassword']");
    var confirmField = form.querySelector("[name='confirmPassword']");
    var displayField = form.querySelector("[data-generated-password-display]");

    if (pwdField) {
        pwdField.value = pwd;
        pwdField.type = "text";
    }
    if (confirmField) {
        confirmField.value = pwd;
        confirmField.type = "text";
    }
    if (displayField) {
        displayField.textContent = "Mật khẩu vừa tạo: " + pwd;
        displayField.style.display = "block";
    }
}
