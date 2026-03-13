package com.example.hrm.controller;

import com.example.hrm.entity.User;
import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.AvatarStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileAvatarController {

    private final UserRepository userRepository;
    private final AvatarStorageService avatarStorageService;

    public ProfileAvatarController(UserRepository userRepository,
                                   AvatarStorageService avatarStorageService) {
        this.userRepository = userRepository;
        this.avatarStorageService = avatarStorageService;
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile avatarFile,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
                redirectAttributes.addFlashAttribute("err", "Not authenticated");
                return "redirect:/login";
            }

            String principal = auth.getName();

            Optional<User> userOpt = userRepository.findByUsername(principal);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(principal);
            }

            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("err", "User not found");
                return "redirect:/login";
            }

            User user = userOpt.get();

            String oldAvatar = user.getAvatarPath();
            String newAvatarPath = avatarStorageService.storeAvatar(avatarFile, user.getUserId());

            user.setAvatarPath(newAvatarPath);
            userRepository.save(user);

            avatarStorageService.deleteAvatarIfExists(oldAvatar);

            redirectAttributes.addFlashAttribute("success", "Upload avatar successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:" + resolveProfileUrl(auth);
    }

    private String resolveProfileUrl(Authentication auth) {
        if (auth == null) {
            return "/login";
        }

        boolean isHr = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR"));
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isHr) return "/hr/profile";
        if (isManager) return "/manager/profile";
        if (isEmployee) return "/employee/profile";
        if (isAdmin) return "/admin/profile";

        return "/profile";
    }
}