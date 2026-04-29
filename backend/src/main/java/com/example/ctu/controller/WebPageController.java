package com.example.ctu.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.service.AuthService;
import com.example.ctu.service.LecturerService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class WebPageController {

    private static final String AUTH_COOKIE = "AUTH_TOKEN";

    private final LecturerService lecturerService;
    private final AuthService authService;
    private final FacultyRepository facultyRepository;

    public WebPageController(LecturerService lecturerService, AuthService authService, FacultyRepository facultyRepository) {
        this.lecturerService = lecturerService;
        this.authService = authService;
        this.facultyRepository = facultyRepository;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        addCommonModel(model, authentication);
        model.addAttribute("lecturers", lecturerService.list(null, null).stream().limit(6).toList());
        return "home";
    }

    @GetMapping("/login")
    public String loginPage(Model model, Authentication authentication) {
        addCommonModel(model, authentication);
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String email,
                              @RequestParam String password,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        try {
            AuthDtos.AuthResponse authResponse = authService.login(new AuthDtos.LoginRequest(email, password));
            Cookie cookie = new Cookie(AUTH_COOKIE, authResponse.token());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công.");
            return "redirect:/";
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model, Authentication authentication) {
        addCommonModel(model, authentication);
        model.addAttribute("faculties", facultyRepository.findAllByOrderByNameAsc());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam String studentCode,
                                 @RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam Long facultyId,
                                 RedirectAttributes redirectAttributes) {
        try {
            authService.register(new AuthDtos.RegisterRequest(studentCode, fullName, email, password, facultyId));
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(AUTH_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }

    private void addCommonModel(Model model, Authentication authentication) {
        model.addAttribute("currentUser", authentication == null ? null : authentication.getName());
        model.addAttribute("currentRole", authentication == null || authentication.getAuthorities().isEmpty()
                ? null
                : authentication.getAuthorities().iterator().next().getAuthority());
    }
}