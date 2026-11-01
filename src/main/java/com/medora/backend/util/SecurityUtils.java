package com.medora.backend.util;

import com.medora.backend.entity.User;
import com.medora.backend.service.CustomUserDetailsService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final CustomUserDetailsService userDetailsService;

    public SecurityUtils(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void validateDepartmentAccess(String requestedDepartment) {
        if (requestedDepartment == null || requestedDepartment.equalsIgnoreCase("All")) {
            return; // Usually means global access requested, let role-based security handle it (e.g. only ADMIN)
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = authentication.getName();
        User currentUser = userDetailsService.loadUserEntityByUsername(username);

        // Admins have access to everything
        if (currentUser.getRole().equals("ADMIN")) {
            return;
        }

        // Users can only access their own department
        if (currentUser.getDepartment() == null || !currentUser.getDepartment().getName().equalsIgnoreCase(requestedDepartment)) {
            throw new AccessDeniedException("Access denied: You do not have permission to view or modify data for department " + requestedDepartment);
        }
    }
}
