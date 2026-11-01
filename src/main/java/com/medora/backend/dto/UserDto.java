package com.medora.backend.dto;

public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private String role;
    private String departmentName;
    private Long departmentId;
    private String departmentCode;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private Boolean active = true;
    private String status = "ACTIVE";

    public UserDto() {}

    public UserDto(Long id, String fullName, String email, String username, String role, String departmentName) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.role = role;
        this.departmentName = departmentName;
    }

    public UserDto(Long id, String fullName, String email, String username, String role, String departmentName, Long departmentId, String departmentCode, String phoneNumber, String dateOfBirth, String gender) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.role = role;
        this.departmentName = departmentName;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return id; } // Alias for Phase 1 contract
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDepartment() { return departmentName; } // Alias for Phase 1 contract
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}


