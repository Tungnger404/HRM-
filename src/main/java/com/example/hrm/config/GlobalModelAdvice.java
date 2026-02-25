package com.example.hrm.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void addCommonAttributes(HttpServletRequest request, Model model) {
        String uri = request.getRequestURI();
        model.addAttribute("currentUri", uri);

        String sidebar = resolveSidebar(uri);
        if (sidebar != null) {
            model.addAttribute("sidebar", sidebar);
        }
    }

    private String resolveSidebar(String uri) {
        if (uri.startsWith("/hr/") || uri.equals("/hr")) {
            return "sidebar-hr.html";
        }
        if (uri.startsWith("/admin/") || uri.equals("/admin")) {
            return "sidebar-admin.html";
        }
        if (uri.startsWith("/manager/") || uri.equals("/manager")) {
            return "sidebar-manager.html";
        }
        if (uri.startsWith("/employee/") || uri.equals("/employee")) {
            return "sidebar-employee.html";
        }
        if (uri.startsWith("/bank/") || uri.equals("/bank")) {
            return "sidebar-bank.html";
        }
        if (uri.startsWith("/employees/") || uri.equals("/employees")) {
            return "sidebar-hr.html";
        }
        if (uri.startsWith("/recruitment-request/")) {
            return "sidebar-hr.html";
        }
        return null;
    }
}
