package com.smartdental.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Gom cac flash attribute dang "editXxxForm-{id}" thanh mot map "editForms"
 * de Thymeleaf truy cap duoc ma khong can dung doi tuong "#request" (da bi
 * vo hieu hoa mac dinh tu Thymeleaf 3.1).
 */
public class EditFormFlashInterceptor implements HandlerInterceptor {

    private static final String SUFFIX = "Form";

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                            @NonNull Object handler, ModelAndView modelAndView) {
        if (modelAndView == null) {
            return;
        }
        Map<String, Object> editForms = new HashMap<>();
        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (name.contains(SUFFIX + "-")) {
                editForms.put(name, request.getAttribute(name));
            }
        }
        modelAndView.addObject("editForms", Collections.unmodifiableMap(editForms));
    }
}
