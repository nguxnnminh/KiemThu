/**
 * Smart Dental - modal popup dung chung.
 *
 * Cach dung pho bien (cac phan sau se tai su dung):
 *
 * 1) Modal tu mot <template> co san trong trang:
 *    <template id="formAddXxx"> ... noi dung form ... </template>
 *    <button data-modal-target="formAddXxx" data-modal-title="Them moi">Them</button>
 *
 * 2) Modal xac nhan don gian (vi du khoa/mo khoa, huy):
 *    <button data-confirm-form="form-id" data-confirm-title="Xac nhan"
 *            data-confirm-message="Ban co chac chan?">Khoa</button>
 *    <form id="form-id" method="post" th:action="@{...}" style="display:none">...</form>
 */
(function () {
    var overlay = document.getElementById("appModalOverlay");
    var modalTitle = document.getElementById("appModalTitle");
    var modalBody = document.getElementById("appModalBody");
    var closeBtn = document.getElementById("appModalClose");
    var lastActiveElement = null;

    if (!overlay) {
        return;
    }

    function openModal(title, contentNode) {
        lastActiveElement = document.activeElement;
        modalTitle.textContent = title || "";
        modalBody.innerHTML = "";
        if (contentNode) {
            modalBody.appendChild(contentNode);
        }
        overlay.classList.add("show");
        document.body.style.overflow = "hidden";
        window.setTimeout(function () {
            var focusable = modalBody.querySelector("input:not([type='hidden']):not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), a[href]");
            (focusable || closeBtn).focus();
        }, 0);
    }

    function closeModal() {
        overlay.classList.remove("show");
        modalBody.innerHTML = "";
        document.body.style.overflow = "";
        if (lastActiveElement && typeof lastActiveElement.focus === "function") {
            lastActiveElement.focus();
        }
    }

    window.AppModal = {
        open: openModal,
        close: closeModal
    };

    closeBtn.addEventListener("click", closeModal);

    overlay.addEventListener("click", function (e) {
        if (e.target === overlay) {
            closeModal();
        }
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && overlay.classList.contains("show")) {
            closeModal();
        }
    });

    // Mo modal tu template co san (them/sua)
    document.addEventListener("click", function (e) {
        var trigger = e.target.closest("[data-modal-target]");
        if (trigger) {
            var templateId = trigger.getAttribute("data-modal-target");
            var template = document.getElementById(templateId);
            if (template) {
                var clone = document.importNode(template.content, true);
                openModal(trigger.getAttribute("data-modal-title") || "", clone);
            }
            return;
        }

        // Nut huy/dong trong modal
        var closeTrigger = e.target.closest("[data-modal-close]");
        if (closeTrigger) {
            closeModal();
            return;
        }

        // Modal xac nhan: khoa/mo khoa, huy, duyet, tu choi...
        var confirmTrigger = e.target.closest("[data-confirm-form]");
        if (confirmTrigger) {
            var formId = confirmTrigger.getAttribute("data-confirm-form");
            var targetForm = document.getElementById(formId);
            if (!targetForm) {
                return;
            }
            var title = confirmTrigger.getAttribute("data-confirm-title") || "Xác nhận";
            var message = confirmTrigger.getAttribute("data-confirm-message") || "Bạn có chắc chắn muốn thực hiện thao tác này?";
            var needReason = confirmTrigger.getAttribute("data-confirm-reason") === "true";

            var wrapper = document.createElement("div");
            var msgEl = document.createElement("p");
            msgEl.textContent = message;
            wrapper.appendChild(msgEl);

            if (needReason) {
                var group = document.createElement("div");
                group.className = "form-group";
                var label = document.createElement("label");
                label.textContent = "Lý do";
                var textarea = document.createElement("textarea");
                textarea.className = "form-control";
                textarea.name = "reason";
                textarea.required = true;
                group.appendChild(label);
                group.appendChild(textarea);
                wrapper.appendChild(group);
            }

            var footer = document.createElement("div");
            footer.className = "modal-footer";

            var cancelBtn = document.createElement("button");
            cancelBtn.type = "button";
            cancelBtn.className = "btn btn-secondary";
            cancelBtn.textContent = "Hủy";
            cancelBtn.addEventListener("click", closeModal);

            var confirmBtn = document.createElement("button");
            confirmBtn.type = "button";
            confirmBtn.className = "btn btn-primary";
            confirmBtn.textContent = "Xác nhận";
            confirmBtn.addEventListener("click", function () {
                if (needReason) {
                    var reasonField = wrapper.querySelector("textarea[name='reason']");
                    if (!reasonField.value.trim()) {
                        reasonField.focus();
                        return;
                    }
                    var hidden = document.createElement("input");
                    hidden.type = "hidden";
                    hidden.name = "reason";
                    hidden.value = reasonField.value;
                    targetForm.appendChild(hidden);
                }
                targetForm.submit();
            });

            footer.appendChild(cancelBtn);
            footer.appendChild(confirmBtn);
            wrapper.appendChild(footer);

            openModal(title, wrapper);
        }
    });
})();
