/**
 * Smart Dental - tien ich JS dung chung (sidebar toggle, alert auto-hide).
 */
document.addEventListener("DOMContentLoaded", function () {
    var toggleBtn = document.getElementById("sidebarToggle");
    var sidebar = document.getElementById("sidebar");

    var overlay = document.getElementById("sidebarOverlay");
    var sidebarStorageKey = "smartDentalSidebarCollapsed";

    function closeMobileSidebar() {
        if (sidebar) sidebar.classList.remove("show");
        if (overlay) overlay.classList.remove("show");
    }

    if (sidebar && window.innerWidth > 900) {
        try {
            if (localStorage.getItem(sidebarStorageKey) === "true") {
                sidebar.classList.add("collapsed");
            }
        } catch (ignored) {
            // localStorage can be unavailable in private contexts.
        }
    }

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener("click", function () {
            if (window.innerWidth <= 900) {
                var opened = sidebar.classList.toggle("show");
                if (overlay) overlay.classList.toggle("show", opened);
            } else {
                sidebar.classList.toggle("collapsed");
                try {
                    localStorage.setItem(sidebarStorageKey, sidebar.classList.contains("collapsed") ? "true" : "false");
                } catch (ignored) {
                    // no-op
                }
            }
        });
    }

    if (overlay) {
        overlay.addEventListener("click", closeMobileSidebar);
    }

    // Accordion menu: bam tieu de nhom de mo/dong; tu dong dong nhom khac
    var groups = Array.prototype.slice.call(document.querySelectorAll(".sidebar .menu-group"));
    function syncAccordionState() {
        groups.forEach(function (group) {
            var header = group.querySelector("[data-accordion]");
            if (header) {
                header.setAttribute("aria-expanded", group.classList.contains("open") ? "true" : "false");
            }
        });
    }
    syncAccordionState();
    groups.forEach(function (group) {
        var header = group.querySelector("[data-accordion]");
        if (!header) return;
        header.addEventListener("click", function () {
            var wasOpen = group.classList.contains("open");
            groups.forEach(function (g) { g.classList.remove("open"); });
            if (!wasOpen) group.classList.add("open");
            syncAccordionState();
        });
    });

    // Tren mobile, bam mot muc menu xong thi dong sidebar
    document.querySelectorAll(".sidebar .menu-list a").forEach(function (link) {
        link.addEventListener("click", function () {
            if (window.innerWidth <= 900) closeMobileSidebar();
        });
    });

    // Quick search only filters visible navigation/dashboard items on the client.
    var quickSearch = document.querySelector(".topbar-search input");
    if (quickSearch) {
        quickSearch.addEventListener("input", function () {
            var query = quickSearch.value.trim().toLowerCase();
            quickSearch.classList.toggle("is-searching", query.length > 0);

            document.querySelectorAll(".sidebar .menu-group").forEach(function (group) {
                var links = Array.prototype.slice.call(group.querySelectorAll(".menu-list li"));
                var anyVisible = false;
                links.forEach(function (li) {
                    var text = li.textContent.toLowerCase();
                    var match = !query || text.indexOf(query) !== -1;
                    li.classList.toggle("search-hidden", !match);
                    anyVisible = anyVisible || match;
                });
                group.classList.toggle("search-hidden", !!query && !anyVisible);
                if (query && anyVisible) group.classList.add("open");
            });
            syncAccordionState();

            document.querySelectorAll(".dash-card").forEach(function (card) {
                var match = !query || card.textContent.toLowerCase().indexOf(query) !== -1;
                card.classList.toggle("search-hidden", !match);
            });
        });

        quickSearch.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                quickSearch.value = "";
                quickSearch.dispatchEvent(new Event("input"));
                quickSearch.blur();
            }
        });
    }

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
            alertEl.classList.add("is-leaving");
            alertEl.style.opacity = "0";
            setTimeout(function () {
                alertEl.remove();
            }, 400);
        }, 4000);
    });

    // Lightweight submit feedback. It does not alter submitted data.
    document.addEventListener("submit", function (event) {
        var form = event.target;
        if (!form || form.hasAttribute("data-no-submit-feedback")) return;
        var submitter = form.querySelector("button[type='submit'], input[type='submit']");
        if (submitter) {
            submitter.classList.add("is-submitting");
            submitter.setAttribute("aria-busy", "true");
        }
    }, true);

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
