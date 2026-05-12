package com.example.ctu.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.example.ctu.service.CurrentUserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class WebPageController {

    private static final String AUTH_COOKIE = "AUTH_TOKEN";

    private final AuthService authService;
    private final FacultyRepository facultyRepository;
    private final CurrentUserService currentUserService;
    private final int otpTtlMinutes;

    public WebPageController(AuthService authService,
                             FacultyRepository facultyRepository,
                             CurrentUserService currentUserService,
                             @Value("${app.otp.ttl-minutes:5}") int otpTtlMinutes) {
        this.authService = authService;
        this.facultyRepository = facultyRepository;
        this.currentUserService = currentUserService;
        this.otpTtlMinutes = otpTtlMinutes;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        addCommonModel(model, authentication);
        model.addAttribute("facultyCount", facultyRepository.count());
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

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model, Authentication authentication) {
        addCommonModel(model, authentication);
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email,
                                       RedirectAttributes redirectAttributes) {
        try {
            authService.requestPasswordReset(email);
            redirectAttributes.addFlashAttribute("success", "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi. Hãy xác thực OTP để mở khóa trang đổi mật khẩu.");
            return "redirect:/reset-password?email=" + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8);
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String email,
                                    @RequestParam(required = false) String token,
                                    Model model,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        if (token != null && !token.isBlank()) {
            try {
                String verifiedEmail = authService.resolvePasswordResetEmail(token);
                addCommonModel(model, authentication);
                model.addAttribute("email", verifiedEmail);
                model.addAttribute("resetToken", token);
                model.addAttribute("otpVerified", true);
                return "reset-password";
            } catch (BadRequestException exception) {
                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                return "redirect:/forgot-password";
            }
        }

        if (email == null || email.isBlank()) {
            return "redirect:/forgot-password";
        }

        addCommonModel(model, authentication);
        model.addAttribute("email", email);
        model.addAttribute("otpVerified", false);
        return "reset-password";
    }

    @PostMapping("/reset-password/verify")
    public String verifyResetPasswordOtp(@RequestParam String email,
                                         @RequestParam String otp,
                                         RedirectAttributes redirectAttributes) {
        try {
            String token = authService.verifyPasswordResetOtp(email, otp);
            redirectAttributes.addFlashAttribute("success", "Xác thực OTP thành công. Bạn có thể đặt lại mật khẩu.");
            return "redirect:/reset-password?token=" + java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/reset-password?email=" + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        try {
            authService.resetPasswordWithToken(token, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/reset-password?token=" + java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
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
                                 @RequestParam String confirmPassword,
                                 @RequestParam Long facultyId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Store registration temporarily and send OTP (don't save to DB yet)
            authService.registerTemporarily(new AuthDtos.RegisterRequest(studentCode, fullName, email, password, confirmPassword, facultyId));
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Vui lòng nhập OTP được gửi đến email.");
            return "redirect:/verify-otp?email=" + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8);
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(required = false) String email, Model model, Authentication authentication) {
        if (email == null || email.isBlank()) {
            return "redirect:/register";
        }
        addCommonModel(model, authentication);
        model.addAttribute("email", email);
        model.addAttribute("otpTtlMinutes", otpTtlMinutes);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtpSubmit(@RequestParam String email,
                                  @RequestParam String otp,
                                  RedirectAttributes redirectAttributes) {
        try {
            authService.verify(new AuthDtos.VerifyRequest(email, otp));
            redirectAttributes.addFlashAttribute("success", "Xác thực OTP thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/verify-otp?email=" + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        if (isAnonymous(authentication)) {
            return "redirect:/login";
        }

        addCommonModel(model, authentication);
        var user = currentUserService.requireCurrentUser();
        model.addAttribute("profile", new AuthDtos.ProfileResponse(
                user.getStudentCode(),
                user.getFullName(),
                user.getEmail(),
                user.getFaculty().getName(),
                user.getRole(),
                user.isVerified()
        ));
        model.addAttribute("selectedFacultyId", user.getFaculty().getId());
        model.addAttribute("faculties", facultyRepository.findAllByOrderByNameAsc());
        return "profile";
    }

    @PostMapping("/profile/update")
    public String profileUpdateSubmit(@RequestParam String fullName,
                                      @RequestParam Long facultyId,
                                      RedirectAttributes redirectAttributes) {
        try {
            var currentUser = currentUserService.requireCurrentUser();
            authService.updateProfile(currentUser.getId(), new AuthDtos.UpdateProfileRequest(fullName, facultyId));
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePasswordSubmit(@RequestParam String currentPassword,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            var currentUser = currentUserService.requireCurrentUser();
            authService.changePassword(currentUser.getId(), new AuthDtos.ChangePasswordRequest(currentPassword, newPassword, confirmPassword));
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công.");
        } catch (BadRequestException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Delete the JWT cookie with all necessary attributes
        Cookie cookie = new Cookie(AUTH_COOKIE, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(-1);  // Negative maxAge ensures immediate deletion
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        
        // Alternative: Also set cookie value to empty string for extra safety
        Cookie deleteCookie = new Cookie(AUTH_COOKIE, "");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);
        
        // Clear the security context to ensure immediate logout
        SecurityContextHolder.clearContext();
        
        // Prevent caching of the logout response
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        return "redirect:/";
    }

    private void addCommonModel(Model model, Authentication authentication) {
        model.addAttribute("currentUser", isAnonymous(authentication) ? null : authentication.getName());
        model.addAttribute("currentRole", authentication == null || authentication.getAuthorities().isEmpty()
                ? null
                : authentication.getAuthorities().iterator().next().getAuthority());
    }

    private boolean isAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken;
    }
}