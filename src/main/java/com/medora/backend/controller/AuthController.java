package com.medora.backend.controller;

import com.medora.backend.config.JwtTokenProvider;
import com.medora.backend.dto.AuthResponse;
import com.medora.backend.dto.LoginRequest;
import com.medora.backend.dto.RegisterRequest;
import com.medora.backend.dto.UserDto;
import com.medora.backend.entity.Department;
import com.medora.backend.entity.User;
import com.medora.backend.repository.DepartmentRepository;
import com.medora.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.medora.backend.entity.RevokedToken;
import com.medora.backend.repository.RevokedTokenRepository;
import com.medora.backend.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private AuditService auditService;

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    private UserDto convertToUserDto(User user) {
        String deptName = user.getDepartment() != null ? user.getDepartment().getName() : (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN") ? "All Departments" : "Unassigned");
        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        String deptCode = user.getDepartment() != null ? user.getDepartment().getCode() : "NONE";

        UserDto dto = new UserDto(
            user.getId(), 
            user.getFullName(), 
            user.getEmail(), 
            user.getUsername(), 
            user.getRole(), 
            deptName,
            deptId,
            deptCode,
            user.getPhoneNumber(),
            user.getDateOfBirth(),
            user.getGender()
        );
        dto.setActive(user.getActive() != null ? user.getActive() : true);
        dto.setStatus(user.getStatus() != null ? user.getStatus() : "ACTIVE");
        return dto;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized token or session expired."));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User not found in system database."));
        }

        return ResponseEntity.ok(convertToUserDto(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
            
            auditService.logAction("LOGIN_SUCCESS", loginRequest.getUsername(), "User logged in successfully", request.getRemoteAddr());

            return ResponseEntity.ok(new AuthResponse(jwt, convertToUserDto(user)));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            auditService.logAction("LOGIN_FAILED", loginRequest.getUsername(), "Invalid credentials", request.getRemoteAddr());
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password. Please check your credentials or register a new account."));
        } catch (Exception e) {
            auditService.logAction("LOGIN_ERROR", loginRequest.getUsername(), "Authentication error: " + e.getMessage(), request.getRemoteAddr());
            return ResponseEntity.status(401).body(Map.of("message", "Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, Authentication authentication) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);
            revokedTokenRepository.save(new RevokedToken(jwt));
            
            String username = authentication != null ? authentication.getName() : "unknown";
            auditService.logAction("LOGOUT", username, "User logged out and token blacklisted", request.getRemoteAddr());
            
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "No valid token found in request"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // 1. Password confirmation check
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match."));
        }

        String normalizedRole = (registerRequest.getRole() != null) ? registerRequest.getRole().toUpperCase() : "";

        // 2. Strict ADMIN self-registration block
        if ("ADMIN".equals(normalizedRole)) {
            return ResponseEntity.badRequest().body(Map.of("message", "ADMIN role cannot be self-registered. Please contact an existing administrator."));
        }

        // Validate allowed user roles for Phase 1
        List<String> allowedRoles = List.of("PATIENT", "DOCTOR", "NURSE", "PARAMEDICAL_STAFF", "AMBULANCE_DRIVER", "STAFF", "DEPARTMENT_MANAGER");
        if (!allowedRoles.contains(normalizedRole)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid role selected. Supported roles are PATIENT, DOCTOR, NURSE, PARAMEDICAL_STAFF, AMBULANCE_DRIVER."));
        }

        // 3. Department requirement check (Mandatory for DOCTOR, NURSE, PARAMEDICAL_STAFF, DEPARTMENT_MANAGER, STAFF)
        boolean departmentRequired = List.of("DOCTOR", "NURSE", "PARAMEDICAL_STAFF", "DEPARTMENT_MANAGER", "STAFF").contains(normalizedRole);
        Department dept = null;
        if (departmentRequired) {
            if (registerRequest.getDepartmentId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Department selection is mandatory for " + normalizedRole + " accounts."));
            }
            dept = departmentRepository.findById(registerRequest.getDepartmentId()).orElse(null);
            if (dept == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Selected department was not found in the system."));
            }
        } else if (registerRequest.getDepartmentId() != null) {
            // Optional department for PATIENT / AMBULANCE_DRIVER
            dept = departmentRepository.findById(registerRequest.getDepartmentId()).orElse(null);
        }

        // 4. Duplicate checks
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username '" + registerRequest.getUsername() + "' is already taken. Please choose another username."));
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email address '" + registerRequest.getEmail() + "' is already registered. Please log in or use another email."));
        }

        User user = new User(
                registerRequest.getFullName(),
                registerRequest.getEmail(),
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                normalizedRole,
                dept
        );
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setDateOfBirth(registerRequest.getDateOfBirth());
        user.setGender(registerRequest.getGender());
        user.setActive(true);
        user.setStatus("ACTIVE");

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "An account with this email or username already exists. Please try logging in."));
        }

        return ResponseEntity.ok(Map.of("message", "User account registered successfully! You can now log in."));
    }

}
